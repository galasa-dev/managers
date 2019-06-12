package test.zos3270.network;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import dev.voras.common.zos3270.internal.comms.Network;
import dev.voras.common.zos3270.internal.comms.NetworkThread;
import dev.voras.common.zos3270.spi.NetworkException;

public class Message3270Test {

	@Test
	public void testGoldenPathTerminatedMessage() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(0);
		baos.write(0);
		baos.write(0);
		baos.write(0);
		baos.write(0);
		baos.write(Network.IAC);
		baos.write(Network.EOR);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		ByteBuffer buffer = NetworkThread.readTerminatedMessage(bais);

		Assert.assertEquals("Should contain 5 zeros", 5, buffer.remaining());
		Assert.assertEquals("Should be all zeros", 0, buffer.get());
		Assert.assertEquals("Should be all zeros", 0, buffer.get());
		Assert.assertEquals("Should be all zeros", 0, buffer.get());
		Assert.assertEquals("Should be all zeros", 0, buffer.get());
		Assert.assertEquals("Should be all zeros", 0, buffer.get());
	}

	@Test
	public void testEmbeddedFF() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(0);
		baos.write(-1);
		baos.write(-1);
		baos.write(0);
		baos.write(Network.IAC);
		baos.write(Network.EOR);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		ByteBuffer buffer = NetworkThread.readTerminatedMessage(bais);

		Assert.assertEquals("Should contain 3 bytes", 3, buffer.remaining());
		Assert.assertEquals("Should be 00 FF 00", 0, buffer.get());
		Assert.assertEquals("Should be 00 FF 00", -1, buffer.get());
		Assert.assertEquals("Should be 00 FF 00", 0, buffer.get());

	}

	@Test
	public void testShortenedMessage() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(0);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		try {
			NetworkThread.readTerminatedMessage(bais);
			fail("Should have thrown an exception saying terminated early");
		} catch(NetworkException e) {
			Assert.assertEquals("Exception thrown is not correct", "3270 message did not terminate with IAC EOR", e.getMessage());
		}

	}

	@Test
	public void testMidSE() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(0);
		baos.write(Network.EOR);
		baos.write(0);
		baos.write(Network.IAC);
		baos.write(Network.EOR);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		ByteBuffer buffer = NetworkThread.readTerminatedMessage(bais);

		Assert.assertEquals("Should contain 3 bytes", 3, buffer.remaining());
		Assert.assertEquals("Should be 00 EF 00", 0, buffer.get());
		Assert.assertEquals("Should be 00 EF 00", -17, buffer.get());
		Assert.assertEquals("Should be 00 EF 00", 0, buffer.get());
	}

}
