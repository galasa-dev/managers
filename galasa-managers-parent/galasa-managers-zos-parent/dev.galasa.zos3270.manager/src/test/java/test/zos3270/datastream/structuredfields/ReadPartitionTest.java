/*
 * Copyright (c) 2019 IBM Corporation.
 */
package test.zos3270.datastream.structuredfields;

import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.CommandWriteStructured;
import dev.galasa.zos3270.internal.datastream.StructuredField;
import dev.galasa.zos3270.internal.datastream.StructuredFieldReadPartition;
import dev.galasa.zos3270.spi.NetworkException;

public class ReadPartitionTest {

	@Test
	public void testGoldenPath() throws NetworkException {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putShort((short) 5);
		buffer.put(StructuredField.SF_READ_PARTITION);
		buffer.put((byte)-1);
		buffer.put(StructuredFieldReadPartition.QUERY);
		buffer.flip();

		Inbound3270Message inbound = NetworkThread.processStructuredFields(new CommandWriteStructured(), buffer);

		List<StructuredField> fields = inbound.getStructuredFields();
		Assert.assertEquals("Count of fields", 1, fields.size());
		Assert.assertTrue("Not a Read Partition", fields.get(0) instanceof StructuredFieldReadPartition);

		StructuredFieldReadPartition sfrp = (StructuredFieldReadPartition) fields.get(0);

		Assert.assertEquals("Partition ID is incorrect", -1, sfrp.getPartitionId());
		Assert.assertEquals("Type is incorrect", StructuredFieldReadPartition.Type.QUERY, sfrp.getType());
	}

	@Test
	public void testInvalidType()  {
		byte[] buffer = new byte[] {StructuredField.SF_READ_PARTITION, -1, -1};

		try {
			new StructuredFieldReadPartition(buffer);
			fail("Should have failed due to invalid Type");
		} catch(NetworkException e) {
			Assert.assertEquals("Incorrect error message", "Unsupported Read Partition Type code = -1", e.getMessage());
		}

	}

}
