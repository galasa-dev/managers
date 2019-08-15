package test.zos3270.fields;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.common.zos3270.internal.terminal.fields.Field;
import dev.galasa.common.zos3270.internal.terminal.fields.FieldChars;
import dev.galasa.common.zos3270.internal.terminal.fields.FieldStartOfField;
import dev.galasa.common.zos3270.internal.terminal.fields.FieldText;

public class FieldCharsTest {
	
	@Test
	public void testSetup() {
		FieldChars fc = new FieldChars('w', 20, 40);
		Assert.assertEquals("Setup incorrect of FieldChars", "Chars(w,20-40)", fc.toString());
		Assert.assertEquals("Setup incorrect of FieldChars", 'w', fc.getCharacter());

		fc = new FieldChars((char) 0x00, 20, 40);
		Assert.assertEquals("Setup incorrect of FieldChars", "Chars( ,20-40)", fc.toString());

		fc = new FieldChars((char) 'w', 20, 20);
		Assert.assertEquals("Setup incorrect of FieldChars", "Chars(w,20)", fc.toString());
	}

	@Test
	public void testStringConvert() {
		FieldChars fc = new FieldChars('w', 20,29);
		
		StringBuilder sb = new StringBuilder();
		fc.getFieldString(sb);
		
		Assert.assertEquals("string convert incorrect of FieldChars", "wwwwwwwwww", sb.toString());
	}
	
	@Test
	public void testMergeSameCharacter() {
		FieldChars fc1 = new FieldChars('w', 20, 30);
		FieldChars fc2 = new FieldChars('w', 31, 40);
		
		LinkedList<Field> fields = new LinkedList<>();
		fields.add(fc1);
		fields.add(fc2);
		
		fc1.merge(fields, fc2);
		
		Assert.assertEquals("Setup incorrect of FieldChars", "Chars(w,20-40)", fc1.toString());
		Assert.assertEquals("Setup incorrect of FieldChars", 'w', fc1.getCharacter());
		Assert.assertTrue("Original FieldChar is missing", fields.contains(fc1));
		Assert.assertFalse("Merged FieldChar still exists", fields.contains(fc2));
	}

	@Test
	public void testMergeDifferentCharacter() {
		FieldChars fc1 = new FieldChars('w', 20, 30);
		FieldChars fc2 = new FieldChars('x', 31, 40);
		
		LinkedList<Field> fields = new LinkedList<>();
		fields.add(fc1);
		fields.add(fc2);
		
		fc1.merge(fields, fc2);
		
		Assert.assertFalse("Original FieldChar still exists", fields.contains(fc1));
		Assert.assertFalse("Merged FieldChar still exists", fields.contains(fc2));
		
		FieldText newTextField = (FieldText) fields.get(0);
		Assert.assertEquals("New FieldText is incorrect", "Text(wwwwwwwwwwwxxxxxxxxxx,20-40)", newTextField.toString());
		
	}

	@Test
	public void testMergeWithText() {
		FieldChars fc1 = new FieldChars('w', 20, 30);
		FieldText  ft2 = new FieldText("xxxxxxxxxx", 31, 40);
		
		LinkedList<Field> fields = new LinkedList<>();
		fields.add(fc1);
		fields.add(ft2);
		
		fc1.merge(fields, ft2);
		
		Assert.assertFalse("Original FieldChar still exists", fields.contains(fc1));
		Assert.assertFalse("Merged FieldText still exists", fields.contains(ft2));
		
		FieldText newTextField = (FieldText) fields.get(0);
		Assert.assertEquals("New FieldText is incorrect", "Text(wwwwwwwwwwwxxxxxxxxxx,20-40)", newTextField.toString());
	}

	@Test
	public void testSplitRemove() {
		FieldChars fcOriginal = new FieldChars('w', 10, 30);
		
		LinkedList<Field> fields = new LinkedList<>();
		fields.add(fcOriginal);
		
		fcOriginal.split(fields, 0, 40);
		
		Assert.assertFalse("Original FieldChars still exists", fields.contains(fcOriginal));
		Assert.assertTrue("Original field should have been removed", fields.isEmpty());

	}

	@Test
	public void testSplitMiddle() {
		FieldChars fcOriginal = new FieldChars('w', 10, 30);
		
		LinkedList<Field> fields = new LinkedList<>();
		fields.add(fcOriginal);
		
		fcOriginal.split(fields, 15, 16);
		
		Assert.assertTrue("Original FieldChars is missing", fields.contains(fcOriginal));
		Assert.assertEquals("Existing Split FieldChars 1 is incorrect", "Chars(w,10-14)", fields.get(0).toString());
		Assert.assertEquals("New Split FieldChars 2 is incorrect", "Chars(w,17-30)", fields.get(1).toString());
	}

	@Test
	public void testSplitChopOfEnd() {
		FieldChars fcOriginal = new FieldChars('w', 10, 30);
		
		LinkedList<Field> fields = new LinkedList<>();
		fields.add(fcOriginal);
		
		fcOriginal.split(fields, 25, 40);
		
		Assert.assertTrue("Original FieldChars is missing", fields.contains(fcOriginal));
		Assert.assertEquals("Existing Split FieldChars 1 is incorrect", "Chars(w,10-24)", fields.get(0).toString());
	}

	@Test
	public void testSplitChopOfStart() {
		FieldChars fcOriginal = new FieldChars('w', 10, 30);
		
		LinkedList<Field> fields = new LinkedList<>();
		fields.add(fcOriginal);
		
		fcOriginal.split(fields, 5, 15);
		
		Assert.assertTrue("Original FieldChars is missing", fields.contains(fcOriginal));
		Assert.assertEquals("Existing Split FieldChars 1 is incorrect", "Chars(w,16-30)", fields.get(0).toString());
	}

	@Test
	public void testSplitIgnore() {
		FieldChars fcOriginal = new FieldChars('w', 10, 30);
		
		LinkedList<Field> fields = new LinkedList<>();
		fields.add(fcOriginal);
		
		fcOriginal.split(fields, 1, 5);
		Assert.assertTrue("Original FieldChars is missing", fields.contains(fcOriginal));
		Assert.assertEquals("Existing Split FieldChars 1 is incorrect", "Chars(w,10-30)", fields.get(0).toString());
		
		fcOriginal.split(fields, 31, 35);
		Assert.assertTrue("Original FieldChars is missing", fields.contains(fcOriginal));
		Assert.assertEquals("Existing Split FieldChars 1 is incorrect", "Chars(w,10-30)", fields.get(0).toString());
	}

    @Test
    public void testContains() {
        FieldChars field = new FieldChars('w', 10, 30);
        
        Assert.assertTrue("Should have found wwwwww", field.containsText("wwwwww"));
        Assert.assertFalse("Should not have found xxxxxx", field.containsText("xxxxxx"));
    }
    
    @Test
    public void testTypable() {
        FieldChars field = new FieldChars('w', 10, 30);
        
        field.setPreviousStartOfField(new FieldStartOfField(0, false, false, false, false, false, false));
        Assert.assertTrue("Should be typeable", field.isTypeable());

        field.setPreviousStartOfField(new FieldStartOfField(0, true, false, false, false, false, false));
        Assert.assertFalse("Should not be typeable", field.isTypeable());
    }



}
