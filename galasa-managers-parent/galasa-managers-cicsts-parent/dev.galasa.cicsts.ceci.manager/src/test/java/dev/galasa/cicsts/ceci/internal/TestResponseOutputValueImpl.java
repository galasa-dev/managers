package dev.galasa.cicsts.ceci.internal;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dev.galasa.cicsts.ceci.CECIException;

public class TestResponseOutputValueImpl {
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testGetTextValue() {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl("TEXT");
        Assert.assertEquals("Unxpected result", "TEXT", responseOutputValue.getTextValue());
        
        responseOutputValue = new ResponseOutputValueImpl(new String[] {"TEXT","HEX"});
        Assert.assertEquals("Unxpected result", "TEXT", responseOutputValue.getTextValue());
    }

    @Test
    public void testGetHexValue() {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl("HEX");
        Assert.assertTrue("Unxpected result", Arrays.equals("HEX".toCharArray(), responseOutputValue.getHexValue()));

        responseOutputValue = new ResponseOutputValueImpl(new String[] {"TEXT","HEX"});
        Assert.assertTrue("Unxpected result", Arrays.equals("HEX".toCharArray(), responseOutputValue.getHexValue()));
    }

    @Test
    public void testGetIntValue() throws CECIException {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl(99);
        Assert.assertEquals("Unxpected result", 99, responseOutputValue.getIntValue());
        
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Value is " + String.class.getName() + " type");
        
        responseOutputValue = new ResponseOutputValueImpl("TEXT");
        responseOutputValue.getIntValue();
    }

    @Test
    public void testGetLongValue() throws CECIException {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl(99L);
        Assert.assertEquals("Unxpected result", 99L, responseOutputValue.getLongValue());
        
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("Value is " + String.class.getName() + " type");
        
        responseOutputValue = new ResponseOutputValueImpl("TEXT");
        responseOutputValue.getLongValue();
    }

    @Test
    public void testToString() {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl("TEXT");
        Assert.assertEquals("Unxpected result", "TEXT", responseOutputValue.toString());
        
        responseOutputValue = new ResponseOutputValueImpl("HEX");
        Assert.assertEquals("Unxpected result", "HEX", responseOutputValue.toString());
        
        responseOutputValue = new ResponseOutputValueImpl(99);
        Assert.assertEquals("Unxpected result", "99", responseOutputValue.toString());
        
        responseOutputValue = new ResponseOutputValueImpl(99L);
        Assert.assertEquals("Unxpected result", "99", responseOutputValue.toString());
    }

    @Test
    public void testIsArray() {
        ResponseOutputValueImpl responseOutputValue = new ResponseOutputValueImpl("TEXT");
        Assert.assertFalse("Unxpected result", responseOutputValue.isArray());
        
        responseOutputValue = new ResponseOutputValueImpl(new String[] {"TEXT","HEX"});
        Assert.assertTrue("Unxpected result", responseOutputValue.isArray());
    }
}
