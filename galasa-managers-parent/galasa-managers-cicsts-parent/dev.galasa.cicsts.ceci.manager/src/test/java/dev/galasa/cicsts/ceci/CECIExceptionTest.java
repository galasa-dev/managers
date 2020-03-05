package dev.galasa.cicsts.ceci;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CECIExceptionTest {
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testCECIException() throws CECIException {        
        exceptionRule.expect(CECIException.class);
        throw new CECIException();
    }

    @Test
    public void testCECIExceptionString() throws CECIException {       
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("EXCEPTION");
        throw new CECIException("EXCEPTION");
    }

    @Test
    public void testCECIExceptionThrowable() throws CECIException {      
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectCause(CoreMatchers.isA(NullPointerException.class));
        throw new CECIException(new NullPointerException());
    }

    @Test
    public void testCECIExceptionStringThrowable() throws CECIException {    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("NPE");
        exceptionRule.expectCause(CoreMatchers.isA(NullPointerException.class));
        throw new CECIException("NPE", new NullPointerException());
    }

    @Test
    public void testCECIExceptionStringThrowableBooleanBoolean() throws CECIException {    
        exceptionRule.expect(CECIException.class);
        exceptionRule.expectMessage("NPE");
        exceptionRule.expectCause(CoreMatchers.isA(NullPointerException.class));
        throw new CECIException("NPE", new NullPointerException(), true, true);
    }

}
