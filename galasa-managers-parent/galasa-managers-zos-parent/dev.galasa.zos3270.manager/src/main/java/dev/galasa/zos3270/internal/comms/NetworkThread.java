package dev.galasa.zos3270.internal.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.internal.datastream.CommandCode;
import dev.galasa.zos3270.internal.datastream.CommandWriteStructured;
import dev.galasa.zos3270.internal.datastream.Order;
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

public class NetworkThread extends Thread {

    public static final byte DT_3270_DATA    = 0;
    public static final byte DT_SCS_DATA     = 1;
    public static final byte DT_RESPONSE     = 2;
    public static final byte DT_BIND_IMAGE   = 3;
    public static final byte DT_UNBIND       = 4;
    public static final byte DT_NVT_DATA     = 5;
    public static final byte DT_REQUEST      = 6;
    public static final byte DT_SSCP_LU_DATA = 7;
    public static final byte DT_PRINT_EOJ    = 8;

    private final InputStream inputStream;
    private final Screen      screen;
    private final Network     network;

    private boolean endOfStream = false;

    private Log logger = LogFactory.getLog(getClass());

    public NetworkThread(Screen screen, Network network, InputStream inputStream) {
        this.screen       = screen;
        this.network      = network;
        this.inputStream  = inputStream;
    }

    @Override
    public void run() {

        while(!endOfStream) {
            try {
                processMessage(inputStream);
            } catch (NetworkException e) {
                logger.error("Problem with Network Thread", e);
                network.close();
                return;
            } catch (IOException e) {
                if (e.getMessage().contains("Socket closed")) {
                    return;
                }
                logger.error("Problem with Network Thread", e);
                network.close();
                return;
            }
        }

        network.close();
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

        if (header[0] == DT_3270_DATA) {
            byte[] remainingHeader = new byte[4];
            if (messageStream.read(remainingHeader) != 4) {
                throw new NetworkException("Missing remaining 4 byte of the telnet 3270 header");
            }
            
            ByteBuffer buffer = readTerminatedMessage(messageStream);
            
            Inbound3270Message inbound3270Message = process3270Data(buffer);
            this.screen.processInboundMessage(inbound3270Message);
        } else {
            throw new NetworkException("TN3270E message Data-Type " + header[0] + " is unsupported");	
        }
    }


    public Inbound3270Message process3270Data(ByteBuffer buffer) throws NetworkException {

        String hex = new String(Hex.encodeHex(buffer.array()));
        logger.trace("inbound=" + hex);

        CommandCode commandCode = CommandCode.getCommandCode(buffer.get()); 
        if (commandCode instanceof CommandWriteStructured) {
            return processStructuredFields((CommandWriteStructured)commandCode, buffer);
        } else {
            return process3270Datastream(commandCode, buffer);
        }
    }


    public static Inbound3270Message process3270Datastream(CommandCode commandCode, ByteBuffer buffer) throws DatastreamException {
        WriteControlCharacter writeControlCharacter = new WriteControlCharacter(buffer.get());

        List<Order> orders = processOrders(buffer);
        
        return new Inbound3270Message(commandCode, writeControlCharacter, orders);
    }
    
    public static List<Order> processOrders(ByteBuffer buffer) throws DatastreamException {
        OrderText orderText = null;

        ArrayList<Order> orders = new ArrayList<>();
        while(buffer.remaining() > 0) {
            byte orderByte = buffer.get();

            if (orderByte > 0x00 && orderByte <= 0x3f) {
                orderText = null;

                Order order = null;
                switch(orderByte) {
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
                    default:
                        String byteHex = Hex.encodeHexString(new byte[] {orderByte});
                        throw new DatastreamException("Unrecognised order byte 0x" + byteHex);
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

    public static Inbound3270Message processStructuredFields(CommandWriteStructured commandCode, ByteBuffer buffer) throws NetworkException {
        ArrayList<StructuredField> structuredFields = new ArrayList<>();

        while(buffer.remaining() > 0) {
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
        while(messageStream.read(b) == 1) {
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
