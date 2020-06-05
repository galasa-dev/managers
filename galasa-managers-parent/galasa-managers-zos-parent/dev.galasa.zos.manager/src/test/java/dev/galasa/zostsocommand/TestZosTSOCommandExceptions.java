/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostsocommand;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestZosTSOCommandExceptions {
    
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
    public void testZosTSOCommandException1() throws ZosTSOCommandException {
        exceptionRule.expect(ZosTSOCommandException.class);
        throw new ZosTSOCommandException();
    }
    
    @Test
    public void testZosTSOCommandException2() throws ZosTSOCommandException {
        exceptionRule.expect(ZosTSOCommandException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosTSOCommandException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosTSOCommandException3() throws ZosTSOCommandException {
        exceptionRule.expect(ZosTSOCommandException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosTSOCommandException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosTSOCommandException4() throws ZosTSOCommandException {
        exceptionRule.expect(ZosTSOCommandException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosTSOCommandException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosTSOCommandException5() throws ZosTSOCommandException {
        exceptionRule.expect(ZosTSOCommandException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosTSOCommandException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
    
    @Test
    public void testZosTSOCommandManagerException1() throws ZosTSOCommandManagerException {
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        throw new ZosTSOCommandManagerException();
    }
    
    @Test
    public void testZosTSOCommandManagerException2() throws ZosTSOCommandManagerException {
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosTSOCommandManagerException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosTSOCommandManagerException3() throws ZosTSOCommandManagerException {
        exceptionRule.expect(ZosTSOCommandManagerException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosTSOCommandManagerException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosTSOCommandManagerException4() throws ZosTSOCommandManagerException {
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosTSOCommandManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosTSOCommandManagerException5() throws ZosTSOCommandManagerException {
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosTSOCommandManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
}
