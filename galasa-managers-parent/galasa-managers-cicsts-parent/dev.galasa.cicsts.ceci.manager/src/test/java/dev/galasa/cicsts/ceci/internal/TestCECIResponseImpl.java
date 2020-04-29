package dev.galasa.cicsts.ceci.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import dev.galasa.cicsts.ceci.IResponseOutputValue;

public class TestCECIResponseImpl {
    
    @Mock
    IResponseOutputValue responseOutputValueMock;

    private static CECIResponseImpl ceciResponseImpl;
    
    @BeforeClass
    public static void beforeClass() {
        ceciResponseImpl = new CECIResponseImpl("NORMAL", 0, 0);
    }

    @Test
    public void testIsNormal() {
        Assert.assertTrue("Unxpected result", ceciResponseImpl.isNormal());
    }

    @Test
    public void testGetResponse() {
        Assert.assertEquals("Unxpected result", "NORMAL", ceciResponseImpl.getResponse());
    }

    @Test
    public void testGetEIBRESP() {
        Assert.assertEquals("Unxpected result", 0, ceciResponseImpl.getEIBRESP());
    }

    @Test
    public void testGetEIBRESP2() {
        Assert.assertEquals("Unxpected result", 0, ceciResponseImpl.getEIBRESP2());
    }

    @Test
    public void testResponseOutputValues() {
        Map<String, IResponseOutputValue> responseOutput = new LinkedHashMap<>();
        responseOutput.put("KEY", responseOutputValueMock);
        ceciResponseImpl.setResponseOutput(responseOutput);
        Assert.assertTrue("Unxpected result", responseOutput.equals(ceciResponseImpl.getResponseOutputValues()));
    }

    @Test
    public void testToString() {
        String result = String.format("RESPONSE: %s EIBRESP=%+010d EIBRESP2=%+010d", "NORMAL", 0, 0);
        Assert.assertEquals("Unxpected result", result, ceciResponseImpl.toString());
    }

}
