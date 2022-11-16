package dev.galasa;

import org.junit.Test;

import dev.galasa.http.internal.HttpClientImpl;

import static org.junit.Assert.* ;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

public class TestHttpClientImpl {
    @Test
    public void testNonIBMJVMGivesTLS12Context() throws Exception {
        // Given...
        Log log = new SimpleLog("Test log");
        HttpClientImpl clientUnderTest = new HttpClientImpl(1,log);
        Properties testProps = new Properties();
        testProps.setProperty(HttpClientImpl.JAVA_VENDOR_PROPERTY,"FakeJavaRuntimeVendorName" );
        // When...
        String contextName = clientUnderTest.getSelectedSSLContextName(testProps);
        // Then...
        String expected = "TLSv1.2";
        assertEquals(expected,contextName);
    }
    @Test
    public void testIBMJVM8GivesSSL_TLSv2Context() throws Exception {
        // Given...
        Log log = new SimpleLog("Test log");
        HttpClientImpl clientUnderTest = new HttpClientImpl(1,log);
        Properties testProps = new Properties();
        testProps.setProperty(HttpClientImpl.JAVA_VENDOR_PROPERTY,"IBMJavaRuntimeVendorName" );
        testProps.setProperty(HttpClientImpl.JAVA_VERSION_PROPERTY, "8.0.16");
        // When...
        String contextName = clientUnderTest.getSelectedSSLContextName(testProps);
        // Then...
        String expected = "SSL_TLSv2";
        assertEquals(expected,contextName);
    }
    
    @Test
    public void testIBMJVM11GivesTLSv12Context() throws Exception {
        // Given...
        Log log = new SimpleLog("Test log");
        HttpClientImpl clientUnderTest = new HttpClientImpl(1,log);
        Properties testProps = new Properties();
        testProps.setProperty(HttpClientImpl.JAVA_VENDOR_PROPERTY,"IBMJavaRuntimeVendorName" );
        testProps.setProperty(HttpClientImpl.JAVA_VERSION_PROPERTY, "11.0.16.1+1");
        // When...
        String contextName = clientUnderTest.getSelectedSSLContextName(testProps);
        // Then...
        String expected = "TLSv1.2";
        assertEquals(expected,contextName);
    }
}