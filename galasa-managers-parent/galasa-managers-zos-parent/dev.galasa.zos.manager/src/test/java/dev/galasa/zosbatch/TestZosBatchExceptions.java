/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestZosBatchExceptions {
    
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
    public void testZosBatchException1() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);
        throw new ZosBatchException();
    }
    
    @Test
    public void testZosBatchException2() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosBatchException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosBatchException3() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosBatchException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosBatchException4() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosBatchException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosBatchException5() throws ZosBatchException {
        exceptionRule.expect(ZosBatchException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosBatchException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
    
    @Test
    public void testZosBatchManagerException1() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        throw new ZosBatchManagerException();
    }
    
    @Test
    public void testZosBatchManagerException2() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosBatchManagerException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosBatchManagerException3() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosBatchManagerException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosBatchManagerException4() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosBatchManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosBatchManagerException5() throws ZosBatchManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosBatchManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
}
