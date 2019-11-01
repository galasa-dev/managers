/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package test.zos3270.network;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Screen;

public class Network3270Test {

    @Test
    public void testProcessMessage() throws NetworkException, IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(AbstractCommandCode.ERASE_WRITE);
        baos.write(0x00);
        baos.write(OrderInsertCursor.ID);
        baos.write(Network.IAC);
        baos.write(Network.EOR);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        NetworkThread networkThread = new NetworkThread(new Screen(), null, bais);
        networkThread.processMessage(bais);

        Assert.assertTrue("Will test the screen at this point, later", true);
    }

    @Test
    public void testShortHeader() throws NetworkException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x00);
        baos.write(0x00);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {
            NetworkThread networkThread = new NetworkThread(null, null, bais);
            networkThread.processMessage(bais);
            fail("Should have thrown an error because header < 5");
        } catch (NetworkException e) {
            Assert.assertEquals("Error message incorrect", "Missing remaining 4 byte of the telnet 3270 header",
                    e.getMessage());
        }

    }

    @Test
    public void testUnknownHeader() throws NetworkException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0xff);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {
            NetworkThread networkThread = new NetworkThread(null, null, bais);
            networkThread.processMessage(bais);
            fail("Should have thrown an error because unknown error");
        } catch (NetworkException e) {
            Assert.assertEquals("Error message incorrect", "TN3270E message Data-Type -1 is unsupported",
                    e.getMessage());
        }

    }

}
