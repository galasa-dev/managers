package io.ejat.zos3270.internal.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;

import io.ejat.zos3270.internal.datastream.CommandCode;
import io.ejat.zos3270.internal.datastream.Order;
import io.ejat.zos3270.internal.datastream.OrderInsertCursor;
import io.ejat.zos3270.internal.datastream.OrderRepeatToAddress;
import io.ejat.zos3270.internal.datastream.OrderSetBufferAddress;
import io.ejat.zos3270.internal.datastream.OrderStartField;
import io.ejat.zos3270.internal.datastream.OrderText;
import io.ejat.zos3270.internal.datastream.WriteControlCharacter;
import io.ejat.zos3270.spi.DatastreamException;
import io.ejat.zos3270.spi.NetworkException;

public class NetworkThread {

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
	private final OutputStream outputStream;

	public NetworkThread(InputStream inputStream, OutputStream outputStream) {
		this.inputStream  = inputStream;
		this.outputStream = outputStream;
	}


	public static void processMessage(InputStream messageStream) throws IOException, NetworkException {
		byte[] header = new byte[5];
		if (messageStream.read(header) != 5) {
			throw new NetworkException("Missing 5 bytes of the telnet 3270 header");
		}

		if (header[0] == DT_3270_DATA) {
			process3270Data(messageStream);
		} else {
			throw new NetworkException("TN3270E message Data-Type " + header[0] + " is unsupported");	
		}
	}


	public static List<Order> process3270Data(InputStream messageStream) throws IOException, NetworkException {
		ByteBuffer buffer = readTerminatedMessage(messageStream);

		CommandCode commandCode = CommandCode.getCommandCode(buffer.get());
		WriteControlCharacter writeControlCharacter = new WriteControlCharacter(buffer.get());

		OrderText orderText = null;

		ArrayList<Order> orders = new ArrayList<>();
		while(buffer.remaining() > 0) {
			byte orderByte = buffer.get();

			if (orderByte >= 0x00 && orderByte <= 0x3f) {
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
			throw new NetworkException("3270 message did not terminate with IAC SE");
		}

		byte[] bytes = byteArrayOutputStream.toByteArray();

		return ByteBuffer.wrap(bytes);
	}

}
