/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.internal.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.spi.NetworkException;

public class Network {

    public static final byte    IAC             = -1;

    public static final byte    DONT            = -2;
    public static final byte    DO              = -3;
    public static final byte    WONT            = -4;
    public static final byte    WILL            = -5;
    public static final byte    SB              = -6;
    public static final byte    SE              = -16;
    public static final byte    EOR             = -17;

    public static final byte    ASSOCIATE       = 0;
    public static final byte    CONNECT         = 1;
    public static final byte    DEVICE_TYPE     = 2;
    public static final byte    RESPONSES       = 2;
    public static final byte    FUNCTIONS       = 3;
    public static final byte    IS              = 4;
    public static final byte    REASON          = 5;
    public static final byte    REJECT          = 6;
    public static final byte    TIMING_MARK     = 6;
    public static final byte    REQUEST         = 7;
    public static final byte    SEND            = 8;
    public static final byte    TN3270E         = 40;

    public static final byte    CONN_PARTNER     = 0;
    public static final byte    DEVICE_IN_USE    = 1;
    public static final byte    INV_ASSOCIATE    = 2;
    public static final byte    INV_NAME         = 3;
    public static final byte    INV_DEVICE_TYPE  = 4;
    public static final byte    TYPE_NAME_ERROR  = 5;
    public static final byte    UNKNOWN_ERROR    = 6;
    public static final byte    UNSUPPORTED_REQ  = 7;

    public static final Charset ascii7          = Charset.forName("us-ascii");

    private final Log           logger          = LogFactory.getLog(getClass());

    private final String        host;
    private final int           port;
    private final boolean       ssl;

    private Socket              socket;
    private OutputStream        outputStream;
    private InputStream         inputStream;

    private KeepAlive           keepAlive;
    private Instant             lastSend        = Instant.now();

    public Network(String host, int port) {
        this(host, port, false);
    }

    public Network(String host, int port, boolean ssl) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
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
            
            this.keepAlive = new KeepAlive();
            this.keepAlive.start();

            return true;
        } catch (Exception e) {
            throw new NetworkException("Unable to connect to Telnet server", e);
        } finally {
            if (newSocket != null) {
                try {
                    newSocket.close();
                } catch (IOException e) {
                    logger.error("Failed to close the socket", e);
                }
            }
        }
    }

    public boolean isConnected() {
        return (this.socket != null);
    }

    public Socket createSocket() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        Socket newSocket = null;
        if (!ssl) {
            newSocket = new Socket(this.host, this.port);
        } else {

            boolean ibmJdk = System.getProperty("java.vendor").contains("IBM");
            SSLContext sslContext;
            if (ibmJdk) {
                sslContext = SSLContext.getInstance("SSL_TLSv2");
            } else {
                sslContext = SSLContext.getInstance("TLSv1.2");
            }
            sslContext.init(null, new TrustManager[] { new TrustAllCerts() }, new java.security.SecureRandom());
            newSocket = sslContext.getSocketFactory().createSocket(this.host, this.port);
            ((SSLSocket) newSocket).startHandshake();
        }
        newSocket.setTcpNoDelay(true);
        newSocket.setKeepAlive(true);

        return newSocket;
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Failed to close the socket", e);
            }
            socket = null;
            inputStream = null;
            outputStream = null;
            
            this.keepAlive.shutdown = true;
            this.keepAlive.interrupt();
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

            expect(inputStream, IAC, SB, TN3270E, DEVICE_TYPE);

            int byteIs = inputStream.read();
            if (byteIs == -1) {
                throw new NetworkException("Negotiation terminated early, attempting to get IS");
            }

            if (byteIs != IS) {
                if (byteIs == REJECT) {
                    rejectedDeviceType(inputStream);
                } else {
                    throw new NetworkException("Unexpected byte for IAC SB TN3270E DEVICE_TYPE " + byteIs);
                }
            }


            baos = new ByteArrayOutputStream();
            byte[] data = new byte[1];
            while (true) {
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
            if (!"IBM-3278-2".equals(returnedDeviceType) && !"IBM-3278-2-E".equals(returnedDeviceType)) {
                throw new NetworkException("Negotiation returned unsupported devicetype '" + returnedDeviceType + "'");
            }

            baos = new ByteArrayOutputStream();
            data = new byte[1];
            while (true) {
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

        } catch (IOException e) {
            throw new NetworkException("IOException during terminal negotiation", e);
        }
    }

    private void rejectedDeviceType(InputStream inputStream) throws NetworkException, IOException {
        expect(inputStream, REASON);

        int reasonCode = inputStream.read();

        if (reasonCode == -1) {
            throw new NetworkException("Missing reason code for rejected device type");
        }

        switch(reasonCode) {
            case CONN_PARTNER:
                throw new NetworkException("Device negotiation failed due to CONN_PARTNER");
            case DEVICE_IN_USE:
                throw new NetworkException("Device negotiation failed due to DEVICE_IN_USE");
            case INV_ASSOCIATE:
                throw new NetworkException("Device negotiation failed due to INV_ASSOCIATE");
            case INV_NAME:
                throw new NetworkException("Device negotiation failed due to INV_NAME");
            case INV_DEVICE_TYPE :
                throw new NetworkException("Device negotiation failed due to INV_DEVICE_TYPE");
            case TYPE_NAME_ERROR:
                throw new NetworkException("Device negotiation failed due to TYPE_NAME_ERROR");
            case UNKNOWN_ERROR:
                throw new NetworkException("Device negotiation failed due to UNKNOWN_ERROR");
            case UNSUPPORTED_REQ:
                throw new NetworkException("Device negotiation failed due to UNSUPPORTED_REQ");
            default:
                throw new NetworkException("Unrecognised reason code for rejected device type =" + reasonCode);
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
        synchronized(outputStream) {
            try {
                byte[] header = new byte[] { 0, 0, 0, 0, 0 };
                byte[] trailer = new byte[] { (byte) 0xff, (byte) 0xef };

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(header);
                baos.write(outboundDatastream);
                baos.write(trailer);
                outputStream.write(baos.toByteArray());
                outputStream.flush();
                
                this.lastSend = Instant.now();
            } catch (IOException e) {
                throw new NetworkException("Unable to write outbound datastream", e);
            }
        }
    }

    private void sendKeepAlive() {
        if (this.outputStream == null) {
            return;
        }
        
        if (this.lastSend.plus(10, ChronoUnit.MINUTES).isAfter(Instant.now())) {
            return;
        }

        synchronized(this.outputStream) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(IAC);
                baos.write(DO);
                baos.write(TIMING_MARK);
                outputStream.write(baos.toByteArray());
                outputStream.flush();
                this.lastSend = Instant.now();
            } catch(Exception e) {
                logger.error("Failed to write DO TIMING MARK",e);
            }
        }
    }

    public String getHostPort() {
        return this.host + ":" + Integer.toString(this.port);
    }

    private static class TrustAllCerts implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // TODO Add functionality for the Certificate management
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // TODO Add functionality for the Certificate management
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

    private class KeepAlive extends Thread {
        
        private boolean shutdown = false;

        public KeepAlive() {
            setName("3270 keep alive");
        }

        @Override
        public void run() {
            while(!shutdown) {
                sendKeepAlive();
                
                try {
                    Thread.sleep(5000);
                } catch(Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
