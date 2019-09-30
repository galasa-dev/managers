package dev.galasa.common.zos3270.internal.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Hex;

import dev.galasa.common.zos3270.spi.NetworkException;

public class Network /* extends Thread */{

	public static final byte IAC = -1;

	public static final byte DO = -3;
	public static final byte WILL = -5;
	public static final byte SB = -6;
	public static final byte SE = -16;
	public static final byte EOR = -17;

	public static final byte ASSOCIATE = 0;
	public static final byte CONNECT = 1;
	public static final byte DEVICE_TYPE = 2;
	public static final byte RESPONSES = 2;
	public static final byte FUNCTIONS = 3;
	public static final byte IS = 4;
	public static final byte INV_DEVICE_TYPE = 4;
	public static final byte REASON = 5;
	public static final byte REJECT = 6;
	public static final byte REQUEST = 7;
	public static final byte SEND = 8;
	public static final byte TN3270E = 40;


	public static final Charset ascii7 = Charset.forName("us-ascii");

	private final String  host;
	private final int     port;
	private final boolean ssl;

	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;

	public Network(String host, int port) {
		this(host, port, false);
	}

	public Network(String host, int port, boolean ssl) {
		this.host = host;
		this.port = port;
		this.ssl  = ssl;
	}

	public boolean connectClient() throws NetworkException {
		if (socket != null) {
			if (socket.isConnected()) {
				return true;
			}

			close();
		}

		Socket newSocket = null;
		try {
			newSocket = createSocket();
			newSocket.setTcpNoDelay(true);
			newSocket.setKeepAlive(true);

			InputStream newInputStream = newSocket.getInputStream();
			OutputStream newOutputStream = newSocket.getOutputStream();

			negotiate(newInputStream, newOutputStream);

			this.socket = newSocket;
			this.outputStream = newOutputStream;
			this.inputStream = newInputStream;
			newSocket = null;

			return true;
		} catch(Exception e) {
			throw new NetworkException("Unable to connect to Telnet server", e);
		} finally {
			if (newSocket != null) {
				try {
					newSocket.close();
				} catch (IOException e) { //NOSONAR
				}
			}
		}
	}

	public Socket createSocket() throws Exception {
		Socket newSocket = null;
		if (!ssl) {
			newSocket = new Socket(this.host, this.port); //NOSONAR
		} else {
			
			boolean ibmJdk = System.getProperty("java.vendor").contains("IBM");
			SSLContext sslContext;
			if(ibmJdk) {
				sslContext = SSLContext.getInstance("SSL_TLSv2"); //NOSONAR
			} else {
				sslContext = SSLContext.getInstance("TLSv1.2");
			}
			sslContext.init(null, new TrustManager[] {new TrustAllCerts()}, new java.security.SecureRandom()); 
			newSocket = sslContext.getSocketFactory().createSocket(this.host, this.port); //NOSONAR
			((SSLSocket)newSocket).startHandshake();
		}
		newSocket.setTcpNoDelay(true);
		newSocket.setKeepAlive(true);

		return newSocket;
	}

	public void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) { //NOSONAR
			}
			socket = null;
			inputStream = null;
			outputStream = null;
		}
	}

	public InputStream getInputStream() {
		return this.inputStream;
	}

	public void negotiate(InputStream inputStream, OutputStream outputStream) throws NetworkException {
		try {
			expect(inputStream, IAC, DO, TN3270E);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(IAC);
			baos.write(WILL);
			baos.write(TN3270E);
			outputStream.write(baos.toByteArray());
			outputStream.flush();

			expect(inputStream, IAC, SB, TN3270E, SEND, DEVICE_TYPE, IAC, SE);

			byte[] deviceType = "IBM-3278-2-E".getBytes(ascii7);

			baos = new ByteArrayOutputStream();
			baos.write(IAC);
			baos.write(SB);
			baos.write(TN3270E);
			baos.write(DEVICE_TYPE);
			baos.write(REQUEST);
			baos.write(deviceType);
			baos.write(IAC);
			baos.write(SE);
			outputStream.write(baos.toByteArray());
			outputStream.flush();

			expect(inputStream, IAC, SB, TN3270E, DEVICE_TYPE, IS);

			baos = new ByteArrayOutputStream();
			byte[] data = new byte[1];
			while(true) {
				int length = inputStream.read(data);
				if (length != 1) {
					throw new NetworkException("Negotiation terminated early, attempting to extract device type");
				}

				if (data[0] == CONNECT) {
					break;
				}

				baos.write(data);
			}
			
			String returnedDeviceType = new String(baos.toByteArray(), ascii7);
			if ("IBM-3278-2".equals(returnedDeviceType) 
					|| "IBM-3278-2-E".equals(returnedDeviceType)) {
			} else {
				throw new NetworkException("Negotiation returned unsupported devicetype '" + returnedDeviceType + "'");
			}

			baos = new ByteArrayOutputStream();
			data = new byte[1];
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

			baos = new ByteArrayOutputStream();
			baos.write(IAC);
			baos.write(SB);
			baos.write(TN3270E);
			baos.write(FUNCTIONS);
			baos.write(REQUEST);
			baos.write(IAC);
			baos.write(SE);
			outputStream.write(baos.toByteArray());
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

	public void sendDatastream(byte[] outboundDatastream) throws NetworkException {
		sendDatastream(outputStream, outboundDatastream);
	}

	public void sendDatastream(OutputStream outputStream, byte[] outboundDatastream) throws NetworkException {
		try {
			byte[] header = new byte[] {0,0,0,0,0};
			byte[] trailer = new byte[] {(byte) 0xff,(byte) 0xef};

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(header);
			baos.write(outboundDatastream);
			baos.write(trailer);
			outputStream.write(baos.toByteArray());
			outputStream.flush();
		} catch(IOException e) {
			throw new NetworkException("Unable to write outbound datastream", e);
		}

	}

	public String getHostPort() {
		return this.host + ":" + Integer.toString(this.port);
	}
	
	
	private static class TrustAllCerts implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {  // NOSONAR TODO proper certificate handling
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) { // NOSONAR TODO proper certificate handling
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
		
	}

}
