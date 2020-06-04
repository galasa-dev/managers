/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosmf;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosmf.IZosmf.ZosmfCustomHeaders;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;

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
        Assert.assertEquals("Problem with ZosmfRequestType", "DELETE", ZosmfRequestType.DELETE.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "GET", ZosmfRequestType.GET.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "POST", ZosmfRequestType.POST.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "POST_JSON", ZosmfRequestType.POST_JSON.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT", ZosmfRequestType.PUT.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT_BINARY", ZosmfRequestType.PUT_BINARY.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT_JSON", ZosmfRequestType.PUT_JSON.toString());
        Assert.assertEquals("Problem with ZosmfRequestType", "PUT_TEXT", ZosmfRequestType.PUT_TEXT.toString());
    }
    
    @Test
    public void testZosmfCustomHeaders() {
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-CSRF-ZOSMF-HEADER", ZosmfCustomHeaders.X_CSRF_ZOSMF_HEADER.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Attributes", ZosmfCustomHeaders.X_IBM_ATTRIBUTES.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Data-Type", ZosmfCustomHeaders.X_IBM_DATA_TYPE.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Intrdr-Lrecl", ZosmfCustomHeaders.X_IBM_INTRDR_LRECL.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Intrdr-Recfm", ZosmfCustomHeaders.X_IBM_INTRDR_RECFM.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Job-Modify-Version", ZosmfCustomHeaders.X_IBM_JOB_MODIFY_VERSION.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Lstat", ZosmfCustomHeaders.X_IBM_LSTAT.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Max-Items", ZosmfCustomHeaders.X_IBM_MAX_ITEMS.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Option", ZosmfCustomHeaders.X_IBM_OPTION.toString());
        Assert.assertEquals("Problem with ZosmfCustomHeaders", "X-IBM-Requested-Method", ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString());
    }
    
    @Test
    public void testZosmfException1() throws ZosmfException {
        exceptionRule.expect(ZosmfException.class);
        throw new ZosmfException();
    }
    
    @Test
    public void testZosmfException2() throws ZosmfException {
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosmfException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosmfException3() throws ZosmfException {
        exceptionRule.expect(ZosmfException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosmfException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosmfException4() throws ZosmfException {
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosmfException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosmfException5() throws ZosmfException {
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosmfException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
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
