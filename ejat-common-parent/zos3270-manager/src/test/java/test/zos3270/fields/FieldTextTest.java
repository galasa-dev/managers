package test.zos3270.fields;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zos3270.internal.terminal.fields.Field;
import io.ejat.zos3270.internal.terminal.fields.FieldChars;
import io.ejat.zos3270.internal.terminal.fields.FieldStartOfField;
import io.ejat.zos3270.internal.terminal.fields.FieldText;

public class FieldTextTest {

	@Test
	public void testSetup() throws UnsupportedEncodingException {
		FieldText ft = new FieldText("abcde", 20, 24);
		Assert.assertEquals("Setup incorrect of FieldText", "Text(abcde,20-24)", ft.toString());

		ft = new FieldText(new String(new byte[] {0x61,0x00,0x63},"utf-8"), 20, 22);
		Assert.assertEquals("Setup incorrect of FieldText", "Text(a c,20-22)", ft.toString());

		ft = new FieldText("w", 20, 20);
		Assert.assertEquals("Setup incorrect of FieldText", "Text(w,20)", ft.toString());
	}

	@Test
	public void testStringConvert() {
		FieldText ft = new FieldText("abcde", 20, 24);

		StringBuilder sb = new StringBuilder();
		ft.getFieldString(sb);

		Assert.assertEquals("string convert incorrect of FieldText", "abcde", sb.toString());
	}

	@Test
	public void testMergeSame() {
		FieldText ft1 = new FieldText("abc", 20, 22);
		FieldText ft2 = new FieldText("de", 23, 24);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(ft1);
		fields.add(ft2);

		ft1.merge(fields, ft2);

		Assert.assertEquals("Setup incorrect of FieldText", "Text(abcde,20-24)", ft1.toString());
		Assert.assertTrue("Original FieldText is missing", fields.contains(ft1));
		Assert.assertFalse("Merged FieldText still exists", fields.contains(ft2));
	}

	@Test
	public void testMergeInvalid() {
		FieldText ft1 = new FieldText("abc", 20, 22);
		FieldStartOfField sf2 = new FieldStartOfField(23, false, false, false, false, false, false);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(ft1);
		fields.add(sf2);

		try {
			ft1.merge(fields, sf2);
			fail("Should have failed as can't merge");
		} catch(UnsupportedOperationException e) {
			Assert.assertTrue("dummy",true);
		}
	}

	@Test
	public void testMergeWithChars() {
		FieldText  ft1 = new FieldText("abcde", 20, 24);
		FieldChars fc2 = new FieldChars('w', 25, 27);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(ft1);
		fields.add(fc2);

		ft1.merge(fields, fc2);

		Assert.assertTrue("Original FieldText is missing", fields.contains(ft1));
		Assert.assertFalse("Merged FieldChars still exists", fields.contains(fc2));

		FieldText newTextField = (FieldText) fields.get(0);
		Assert.assertEquals("New FieldText is incorrect", "Text(abcdewww,20-27)", newTextField.toString());
	}

	@Test
	public void testSplitRemove() {
		FieldText ftoriginal = new FieldText("abcde", 20, 24);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(ftoriginal);

		ftoriginal.split(fields, 0, 40);

		Assert.assertFalse("Original FieldText still exists", fields.contains(ftoriginal));
		Assert.assertTrue("Original field should have been removed", fields.isEmpty());

	}

	@Test
	public void testSplitMiddle() {
		FieldText ftoriginal = new FieldText("abcde", 20, 24);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(ftoriginal);

		ftoriginal.split(fields, 21, 22);

		Assert.assertTrue("Original FieldText is missing", fields.contains(ftoriginal));
		Assert.assertEquals("Existing Split FieldText 1 is incorrect", "Text(a,20)", fields.get(0).toString());
		Assert.assertEquals("New Split FieldText 2 is incorrect", "Text(de,23-24)", fields.get(1).toString());
	}

	@Test
	public void testSplitChopOfEnd() {
		FieldText ftoriginal = new FieldText("abcde", 20, 24);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(ftoriginal);

		ftoriginal.split(fields, 23, 25);

		Assert.assertTrue("Original FieldText is missing", fields.contains(ftoriginal));
		Assert.assertEquals("Existing Split FieldText 1 is incorrect", "Text(abc,20-22)", fields.get(0).toString());
	}

	@Test
	public void testSplitChopOfStart() {
		FieldText ftoriginal = new FieldText("abcde", 20, 24);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(ftoriginal);

		ftoriginal.split(fields, 19, 21);

		Assert.assertTrue("Original FieldText is missing", fields.contains(ftoriginal));
		Assert.assertEquals("Existing Split FieldText 1 is incorrect", "Text(cde,22-24)", fields.get(0).toString());
	}

	@Test
	public void testSplitIgnore() {
		FieldText ftoriginal = new FieldText("abcde", 20, 24);

		LinkedList<Field> fields = new LinkedList<>();
		fields.add(ftoriginal);

		ftoriginal.split(fields, 10, 15);
		Assert.assertTrue("Original FieldText is missing", fields.contains(ftoriginal));
		Assert.assertEquals("Existing Split FieldText 1 is incorrect", "Text(abcde,20-24)", fields.get(0).toString());

		ftoriginal.split(fields, 31, 35);
		Assert.assertTrue("Original FieldText is missing", fields.contains(ftoriginal));
		Assert.assertEquals("Existing Split FieldText 1 is incorrect", "Text(abcde,20-24)", fields.get(0).toString());
	}

}
