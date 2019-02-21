package test.zos3270.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zos3270.internal.comms.Network;
import io.ejat.zos3270.internal.comms.NetworkThread;
import io.ejat.zos3270.internal.datastream.CommandCode;
import io.ejat.zos3270.internal.datastream.OrderInsertCursor;
import io.ejat.zos3270.spi.NetworkException;

public class Network3270Test {
	
	@Test
	public void testProcessMessage() throws NetworkException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(0x00);
		baos.write(0x00);
		baos.write(0x00);
		baos.write(0x00);
		baos.write(0x00);
		baos.write(CommandCode.ERASE_WRITE);
		baos.write(0x00);
		baos.write(OrderInsertCursor.ID);
		baos.write(Network.IAC);
		baos.write(Network.EOR);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		
		NetworkThread.processMessage(bais);
		
		Assert.assertTrue("Will test the screen at this point, later",true);
		
	}

}
