package io.ejat.zos3270.internal.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import io.ejat.zos3270.spi.NetworkException;

public class Network /* extends Thread */{

	public static final byte IAC = -1;

	public static final byte DO = -3;
	public static final byte WILL = -5;
	public static final byte SB = -6;
	public static final byte SE = -16;

	public static final byte CONNECT = 1;
	public static final byte DEVICE_TYPE = 2;
	public static final byte RESPONSES = 2;
	public static final byte FUNCTIONS = 3;
	public static final byte IS = 4;
	public static final byte REQUEST = 7;
	public static final byte SEND = 8;
	public static final byte TN3270E = 40;


	public static final Charset ascii7 = Charset.forName("us-ascii");


	public void negotiate(InputStream inputStream, OutputStream outputStream) throws NetworkException {
		try {
			expect(inputStream, IAC, DO, TN3270E);

			outputStream.write(IAC);
			outputStream.write(WILL);
			outputStream.write(TN3270E);
			outputStream.flush();

			expect(inputStream, IAC, SB, TN3270E, SEND, DEVICE_TYPE, IAC, SE);

			byte[] deviceType = "IBM-3278-2".getBytes(ascii7);

			outputStream.write(IAC);
			outputStream.write(SB);
			outputStream.write(TN3270E);
			outputStream.write(DEVICE_TYPE);
			outputStream.write(REQUEST);
			outputStream.write(deviceType);
			outputStream.write(IAC);
			outputStream.write(SE);
			outputStream.flush();

			expect(inputStream, IAC, SB, TN3270E, DEVICE_TYPE, IS);
			expect(inputStream, deviceType);
			expect(inputStream, CONNECT);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] data = new byte[1];
			while(true) {
				int length = inputStream.read(data);
				if (length != 1) {
					throw new NetworkException("Negotiation terminated early, attempting to extract LU Name");
				}

				if (data[0] == IAC) {
					break;
				}

				baos.write(data);
			}
			expect(inputStream, SE);

			new String(baos.toByteArray(), ascii7);

			outputStream.write(IAC);
			outputStream.write(SB);
			outputStream.write(TN3270E);
			outputStream.write(FUNCTIONS);
			outputStream.write(REQUEST);
			outputStream.write(IAC);
			outputStream.write(SE);
			outputStream.flush();

			expect(inputStream, IAC, SB, TN3270E, FUNCTIONS, IS, IAC, SE);

		} catch(IOException e) {
			throw new NetworkException("IOException during terminal negotiation", e);
		}
	}


	public static void expect(InputStream inputStream, byte... expected) throws IOException, NetworkException {
		byte[] received = new byte[expected.length];

		int length = inputStream.read(received);

		if (length != expected.length) {
			throw new NetworkException("Expected " + expected.length + " but received only " + length + " bytes"); 
		}

		if (!Arrays.equals(expected, received)) {
			String expectedString = Hex.encodeHexString(expected);
			String receivedString = Hex.encodeHexString(received);
			throw new NetworkException("Expected " + expectedString + " but received " + receivedString); 
		}
	}

}
