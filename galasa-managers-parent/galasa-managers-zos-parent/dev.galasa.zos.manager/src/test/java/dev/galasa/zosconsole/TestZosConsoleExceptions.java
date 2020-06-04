/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosconsole;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestZosConsoleExceptions {
    
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
    public void testZosConsoleException1() throws ZosConsoleException {
        exceptionRule.expect(ZosConsoleException.class);
        throw new ZosConsoleException();
    }
    
    @Test
    public void testZosConsoleException2() throws ZosConsoleException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosConsoleException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosConsoleException3() throws ZosConsoleException {
        exceptionRule.expect(ZosConsoleException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosConsoleException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosConsoleException4() throws ZosConsoleException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosConsoleException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosConsoleException5() throws ZosConsoleException {
        exceptionRule.expect(ZosConsoleException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosConsoleException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
    
    @Test
    public void testZosConsoleManagerException1() throws ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleManagerException.class);
        throw new ZosConsoleManagerException();
    }
    
    @Test
    public void testZosConsoleManagerException2() throws ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosConsoleManagerException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosConsoleManagerException3() throws ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleManagerException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosConsoleManagerException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosConsoleManagerException4() throws ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosConsoleManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosConsoleManagerException5() throws ZosConsoleManagerException {
        exceptionRule.expect(ZosConsoleManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosConsoleManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
}
