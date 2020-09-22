/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosprogram.ZosProgram.Language;

public class TestExeptionsAndEnums {
    
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
    public void testLanguage() {
        Assert.assertEquals("Problem with LanguageExtended", ".cbl", Language.COBOL.getFileExtension());

        Assert.assertEquals("Problem with LanguageExtended", Language.COBOL, Language.fromExtension(".cbl"));
        
        String extension = ".xxx";
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Extension " + extension  + " does not match supported languages");
        Language.fromExtension(extension);
    }
    
    @Test
    public void testZosProgramException1() throws ZosProgramException {
        exceptionRule.expect(ZosProgramException.class);
        throw new ZosProgramException();
    }
    
    @Test
    public void testZosProgramException2() throws ZosProgramException {
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        throw new ZosProgramException(EXCEPTION_MESSAGE);
    }
    
    @Test
    public void testZosProgramException3() throws ZosProgramException {
        exceptionRule.expect(ZosProgramException.class);        
        exceptionRule.expectCause(cause);
        throw new ZosProgramException(new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosProgramException4() throws ZosProgramException {
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosProgramException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE));
    }
    
    @Test
    public void testZosProgramException5() throws ZosProgramException {
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage(EXCEPTION_MESSAGE);
        exceptionRule.expectCause(cause);
        throw new ZosProgramException(EXCEPTION_MESSAGE, new Exception(EXCEPTION_CAUSE), false, false);
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
