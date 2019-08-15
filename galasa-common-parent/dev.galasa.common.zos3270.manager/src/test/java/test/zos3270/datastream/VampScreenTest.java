package test.zos3270.datastream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import dev.galasa.common.zos3270.internal.comms.NetworkThread;
import dev.galasa.common.zos3270.internal.datastream.Order;
import dev.galasa.common.zos3270.spi.NetworkException;

public class VampScreenTest {
	
	@Test
	public void testVampScreen() throws IOException, DecoderException, NetworkException {
		URL vampFile = getClass().getClassLoader().getResource("vampstream.txt");
		String vampHex = IOUtils.toString(vampFile.openStream(), "utf-8");
		byte[] stream = Hex.decodeHex(vampHex);
		ByteArrayInputStream bais = new ByteArrayInputStream(stream);
		
		NetworkThread networkThread = new NetworkThread(null, null, null);
		
		List<Order> orders = networkThread.process3270Data(bais).getOrders();
		Assert.assertEquals("Count of orders is incorrect",  225, orders.size());
	}

}
