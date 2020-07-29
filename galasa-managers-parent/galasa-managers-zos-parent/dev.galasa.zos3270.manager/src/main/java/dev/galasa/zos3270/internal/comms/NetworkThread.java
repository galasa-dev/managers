/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.internal.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.IDatastreamListener;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.IDatastreamListener.DatastreamDirection;
import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.CommandWriteStructured;
import dev.galasa.zos3270.internal.datastream.OrderEraseUnprotectedToAddress;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.internal.datastream.OrderRepeatToAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetAttribute;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderStartFieldExtended;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.StructuredField;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.spi.Terminal;

public class NetworkThread extends Thread {

    public static final byte  DT_3270_DATA    = 0;
    public static final byte  DT_SCS_DATA     = 1;
    public static final byte  DT_RESPONSE     = 2;
    public static final byte  DT_BIND_IMAGE   = 3;
    public static final byte  DT_UNBIND       = 4;
    public static final byte  DT_NVT_DATA     = 5;
    public static final byte  DT_REQUEST      = 6;
    public static final byte  DT_SSCP_LU_DATA = 7;
    public static final byte  DT_PRINT_EOJ    = 8;

    private final InputStream inputStream;
    private final Screen      screen;
    private final Network     network;
    private final Terminal    terminal;

    private boolean           endOfStream     = false;

    private static Log               logger          = LogFactory.getLog(NetworkThread.class);

    public NetworkThread(Terminal terminal, Screen screen, Network network, InputStream inputStream) {
        this.screen = screen;
        this.network = network;
        this.inputStream = inputStream;
        this.terminal = terminal;
    }

    @Override
    public void run() {
        logger.trace("Starting network thread on terminal " + terminal.getId());

        while (!endOfStream) {
            try {
                processMessage(inputStream);
            } catch (NetworkException e) {
                logger.error("Problem with Network Thread", e);
                break;
            } catch (IOException e) {
                if (e.getMessage().contains("Socket closed")) {
                    break;
                }
                logger.error("Problem with Network Thread", e);
                break;
            }
        }
        try {
            screen.networkClosed();
        } catch (TerminalInterruptedException e) {
            logger.error("Problem locking keyboard on network close",e);
        }

        logger.trace("Ending network thread on terminal " + terminal.getId());
        terminal.networkClosed();
    }

    public void processMessage(InputStream messageStream) throws IOException, NetworkException {
        byte[] header = new byte[1];
        int length = messageStream.read(header);
        if (length == -1) {
            endOfStream = true;
            return;
        }
        if (length != 1) {
            throw new NetworkException("Missing first byte of the telnet 3270 header");
        }

        //In the middle of the DT_3270_DATA stream we can receive IAC DO TIMING_MARK requests
        if(header[0]  == Network.IAC) {
            byte[] remainingHeader = new byte[2];
            if (messageStream.read(remainingHeader) != 2) {
                throw new NetworkException("Missing remaining 2 bytes of the telnet 3270 IAC header");
            }
            //respond with DON'T_TIMING_MARK
            if(remainingHeader[0] == Network.DO && remainingHeader[1] == Network.TIMING_MARK){
                byte [] response = new byte[3];
                response[0] = Network.IAC;
                response[1] = Network.DONT;    
                response[2] = Network.TIMING_MARK;
                network.sendDatastream(response);
            }else{
                throw new NetworkException("In IAC request not supported, Command was: " + remainingHeader[0] + " " + remainingHeader[1]);
            }
            return;
        }

        if (header[0] == DT_3270_DATA) {
            byte[] remainingHeader = new byte[4];
            if (messageStream.read(remainingHeader) != 4) {
                throw new NetworkException("Missing remaining 4 byte of the telnet 3270 header");
            }

            ByteBuffer buffer = readTerminatedMessage(messageStream);

            Inbound3270Message inbound3270Message = process3270Data(buffer);
            this.screen.processInboundMessage(inbound3270Message);
            return;
        } 

        throw new NetworkException("TN3270E message Data-Type " + header[0] + " is unsupported");
    }

    public Inbound3270Message process3270Data(ByteBuffer buffer) throws NetworkException {

        if (logger.isTraceEnabled() || !this.screen.getDatastreamListeners().isEmpty()) {
            String hex = new String(Hex.encodeHex(buffer.array()));
            if (logger.isTraceEnabled()) {
                logger.trace("inbound=" + hex);
            }

            for(IDatastreamListener listener : this.screen.getDatastreamListeners()) {
                listener.datastreamUpdate(DatastreamDirection.INBOUND, hex);
            }
        }

        AbstractCommandCode commandCode = AbstractCommandCode.getCommandCode(buffer.get());
        if (commandCode instanceof CommandWriteStructured) {
            return processStructuredFields((CommandWriteStructured) commandCode, buffer);
        } else {
            return process3270Datastream(commandCode, buffer);
        }
    }

    public static Inbound3270Message process3270Datastream(AbstractCommandCode commandCode, ByteBuffer buffer)
            throws DatastreamException {

        if (!buffer.hasRemaining()) {
            return new Inbound3270Message(commandCode, null, null);
        }

        WriteControlCharacter writeControlCharacter = new WriteControlCharacter(buffer.get());

        List<AbstractOrder> orders = processOrders(buffer);

        return new Inbound3270Message(commandCode, writeControlCharacter, orders);
    }

    public static List<AbstractOrder> processOrders(ByteBuffer buffer) throws DatastreamException {
        OrderText orderText = null;

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        while (buffer.remaining() > 0) {
            byte orderByte = buffer.get();

            if (orderByte > 0x00 && orderByte <= 0x3f) {
                orderText = null;

                AbstractOrder order = null;
                switch (orderByte) {
                    case OrderSetBufferAddress.ID:
                        order = new OrderSetBufferAddress(buffer);
                        break;
                    case OrderRepeatToAddress.ID:
                        order = new OrderRepeatToAddress(buffer);
                        break;
                    case OrderStartField.ID:
                        order = new OrderStartField(buffer);
                        break;
                    case OrderStartFieldExtended.ID:
                        order = new OrderStartFieldExtended(buffer);
                        break;
                    case OrderSetAttribute.ID:
                        order = new OrderSetAttribute(buffer);
                        break;
                    case OrderInsertCursor.ID:
                        order = new OrderInsertCursor();
                        break;
                    case OrderEraseUnprotectedToAddress.ID:
                        order = new OrderEraseUnprotectedToAddress(buffer);
                        break;
                    default:
                        String byteHex = Hex.encodeHexString(new byte[] { orderByte });
                        logger.trace("Invalid byte detected in datastream, unrecognised byte order or text byte - 0x" + byteHex);
                        order = new OrderText(" ");
                }
                orders.add(order);
            } else {
                if (orderText == null) {
                    orderText = new OrderText();
                    orders.add(orderText);
                }
                orderText.append(orderByte);
            }
        }
        return orders;
    }

    public static Inbound3270Message processStructuredFields(CommandWriteStructured commandCode, ByteBuffer buffer)
            throws NetworkException {
        ArrayList<StructuredField> structuredFields = new ArrayList<>();

        while (buffer.remaining() > 0) {
            int length = buffer.getShort();
            if (length == 0 && buffer.remaining() != 0) {
                throw new NetworkException("SF with length of zero was not the last SF in the buffer");
            }
            byte[] sfData = new byte[length - 2];
            buffer.get(sfData);

            structuredFields.add(StructuredField.getStructuredField(sfData));
        }

        return new Inbound3270Message(commandCode, structuredFields);
    }

    public static ByteBuffer readTerminatedMessage(InputStream messageStream) throws IOException, NetworkException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] b = new byte[1];
        boolean lastByteFF = false;
        boolean terminated = false;
        while (messageStream.read(b) == 1) {
            if (b[0] == Network.IAC) {
                if (lastByteFF) {
                    byteArrayOutputStream.write(b);
                    lastByteFF = false;
                } else {
                    lastByteFF = true;
                }
            } else {
                if (b[0] == Network.EOR && lastByteFF) {
                    terminated = true;
                    break;
                }

                byteArrayOutputStream.write(b);
            }
        }

        if (!terminated) {
            throw new NetworkException("3270 message did not terminate with IAC EOR");
        }

        byte[] bytes = byteArrayOutputStream.toByteArray();

        return ByteBuffer.wrap(bytes);
    }

}
