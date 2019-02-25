package test.zos3270.fields;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zos3270.internal.terminal.fields.FieldChars;

public class FieldTest {

	@Test
	public void testStartEnd() {
		FieldChars fc = new FieldChars('w', 20, 40);

		Assert.assertEquals("Start wrong in FieldChars", 20, fc.getStart());
		Assert.assertEquals("End wrong in FieldChars", 40, fc.getEnd());
	}

	@Test
	public void testPosition() {
		FieldChars fc = new FieldChars('w', 20, 40);

		Assert.assertTrue("Should have been inside FieldChars", fc.containsPosition(20));
		Assert.assertTrue("Should have been inside FieldChars", fc.containsPosition(30));
		Assert.assertTrue("Should have been inside FieldChars", fc.containsPosition(40));

		Assert.assertFalse("Should have been outside FieldChars", fc.containsPosition(5));
		Assert.assertFalse("Should have been outside FieldChars", fc.containsPosition(45));
	}

	@Test
	public void testPositions() {
		FieldChars fc = new FieldChars('w', 20, 40);

		Assert.assertTrue("Should have been inside FieldChars", fc.containsPositions(20, 40));
		Assert.assertTrue("Should have been inside FieldChars", fc.containsPositions(10, 30));
		Assert.assertTrue("Should have been inside FieldChars", fc.containsPositions(30, 50));

		Assert.assertFalse("Should have been outside FieldChars", fc.containsPositions(1,5));
		Assert.assertFalse("Should have been outside FieldChars", fc.containsPositions(45,50));
	}

}
