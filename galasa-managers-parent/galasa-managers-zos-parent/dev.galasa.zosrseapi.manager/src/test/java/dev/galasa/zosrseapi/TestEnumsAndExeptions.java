/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;

public class TestEnumsAndExeptions {
    
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
    public void testZosmfRequestType() {
        Assert.assertEquals("Problem with ZosmfRequestType", "DELETE", RseapiRequestType.DELETE.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "GET", RseapiRequestType.GET.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "POST", RseapiRequestType.POST.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "POST_JSON", RseapiRequestType.POST_JSON.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT", RseapiRequestType.PUT.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT_JSON", RseapiRequestType.PUT_JSON.toString());
    }
    
    @Test
    public void testZosmfException1() throws RseapiException {
        exceptionRule.expect(RseapiException.class);
        throw new RseapiException();
    }
    
    @Test
    public void testZosmfException2() throws RseapiException {
        exceptionRule.expect(RseapiException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new RseapiException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosmfException3() throws RseapiException {
        exceptionRule.expect(RseapiException.class);        
        exceptionRule.expectCause(cause);
        throw new RseapiException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosmfException4() throws RseapiException {
        exceptionRule.expect(RseapiException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new RseapiException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosmfException5() throws RseapiException {
        exceptionRule.expect(RseapiException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new RseapiException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
    }
    
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
