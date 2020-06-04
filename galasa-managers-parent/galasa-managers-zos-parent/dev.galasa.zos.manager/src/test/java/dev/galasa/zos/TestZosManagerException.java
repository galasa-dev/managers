/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestZosManagerException {
    
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
    public void testZosManagerException1() throws ZosManagerException {
        exceptionRule.expect(ZosManagerException.class);
        throw new ZosManagerException();
    }
    
    @Test
    public void testZosManagerException2() throws ZosManagerException {
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosManagerException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosManagerException3() throws ZosManagerException {
        exceptionRule.expect(ZosManagerException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosManagerException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosManagerException4() throws ZosManagerException {
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosManagerException5() throws ZosManagerException {
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosManagerException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
}
