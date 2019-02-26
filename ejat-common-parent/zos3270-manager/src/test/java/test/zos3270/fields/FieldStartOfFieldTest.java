package test.zos3270.fields;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zos3270.internal.terminal.fields.Field;
import io.ejat.zos3270.internal.terminal.fields.FieldStartOfField;

public class FieldStartOfFieldTest {

	@Test
	public void testSetup() throws UnsupportedEncodingException {
		FieldStartOfField sf = new FieldStartOfField(20, false, false, false, false, false, false);
		Assert.assertEquals("Setup incorrect of FieldStartOfField", "StartOfField(20)", sf.toString());

	}

	@Test
	public void testStringConvert() {
		FieldStartOfField sf = new FieldStartOfField(20, false, false, false, false, false, false);

		StringBuilder sb = new StringBuilder();
		sf.getFieldString(sb);

		Assert.assertEquals("string convert incorrect of FieldStartOfField", " ", sb.toString());
	}

	@Test
	public void testMergeInvalid() {
		FieldStartOfField sf = new FieldStartOfField(20, false, false, false, false, false, false);
		try {
			sf.merge(null, null);
			fail("FieldStartOfField should throw an exception as merge is not applicable");
		} catch(UnsupportedOperationException e) {}
	}

	@Test
	public void testSplitRemove() {
		FieldStartOfField sfOriginal = new FieldStartOfField(20, false, false, false, false, false, false);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(sfOriginal);

		sfOriginal.split(fields, 0, 40);

		Assert.assertFalse("Original FieldStartOfField still exists", fields.contains(sfOriginal));
		Assert.assertTrue("Original field should have been removed", fields.isEmpty());

	}
	
	
	@Test
	public void testSplitIgnore() {
		FieldStartOfField sfOriginal = new FieldStartOfField(20, false, false, false, false, false, false);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(sfOriginal);

		sfOriginal.split(fields, 0, 10);
		Assert.assertTrue("Original FieldStartOfField is missing", fields.contains(sfOriginal));
		Assert.assertEquals("Existing Split FieldStartOfField 1 is incorrect", "StartOfField(20)", fields.get(0).toString());

		sfOriginal.split(fields, 30, 40);
		Assert.assertTrue("Original FieldStartOfField is missing", fields.contains(sfOriginal));
		Assert.assertEquals("Existing Split FieldStartOfField 1 is incorrect", "StartOfField(20)", fields.get(0).toString());
	}



}
