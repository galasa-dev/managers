/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunixcommand;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;

public class TestZosUNIXCommandEnumsAndExceptions {
    
    private static final String EXCEPTION_MESSAGE = "exception-message";
    
    private static final String EXCEPTION_CAUSE = "exception-cause";
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private Matcher<? extends Throwable> cause = new BaseMatcher<Throwable>() {

        @Override
        public boolean matches(Object item) {
            return item.getClass().equals(Exception.class);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("\"" + EXCEPTION_CAUSE + "\"");
        }
    };
    @Test
    public void testUNIXFileDataType() {
        Assert.assertEquals("Problem with UNIXFileDataType", "text", UNIXFileDataType.TEXT.toString());
        Assert.assertEquals("Problem with UNIXFileDataType", "binary", UNIXFileDataType.BINARY.toString());
    }
    
    @Test
    public void testZosUNIXCommandException1() throws ZosUNIXCommandException {
        exceptionRule.expect(ZosUNIXCommandException.class);
        throw new ZosUNIXCommandException();
    }
    
    @Test
    public void testZosUNIXCommandException2() throws ZosUNIXCommandException {
        exceptionRule.expect(ZosUNIXCommandException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosUNIXCommandException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosUNIXCommandException3() throws ZosUNIXCommandException {
        exceptionRule.expect(ZosUNIXCommandException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosUNIXCommandException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosUNIXCommandException4() throws ZosUNIXCommandException {
        exceptionRule.expect(ZosUNIXCommandException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosUNIXCommandException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosUNIXCommandException5() throws ZosUNIXCommandException {
        exceptionRule.expect(ZosUNIXCommandException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosUNIXCommandException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
    
    @Test
    public void testZosUNIXCommandManagerException1() throws ZosUNIXCommandManagerException {
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        throw new ZosUNIXCommandManagerException();
    }
    
    @Test
    public void testZosUNIXCommandManagerException2() throws ZosUNIXCommandManagerException {
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosUNIXCommandManagerException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosUNIXCommandManagerException3() throws ZosUNIXCommandManagerException {
        exceptionRule.expect(ZosUNIXCommandManagerException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosUNIXCommandManagerException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosUNIXCommandManagerException4() throws ZosUNIXCommandManagerException {
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosUNIXCommandManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosUNIXCommandManagerException5() throws ZosUNIXCommandManagerException {
        exceptionRule.expect(ZosUNIXCommandManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosUNIXCommandManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
}
