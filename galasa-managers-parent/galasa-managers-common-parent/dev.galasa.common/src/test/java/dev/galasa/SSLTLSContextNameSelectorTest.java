/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.common;

import org.junit.Test;

import static org.junit.Assert.* ;
import java.util.Properties;

public class SSLTLSContextNameSelectorTest {
    @Test
    public void testNonIBMJVMGivesTLS12Context() throws Exception {
        Properties testProps = new Properties();
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VENDOR_PROPERTY,"FakeJavaRuntimeVendorName" );
        // When...
        String contextName = SSLTLSContextNameSelector.getSelectedSSLContextName(testProps);
        // Then...
        String expected = "TLSv1.2";
        assertEquals(expected,contextName);
    }
    @Test
    public void testIBMJVM8GivesSSL_TLSv2Context() throws Exception {
        // Given...
        Properties testProps = new Properties();
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VENDOR_PROPERTY,"IBMJavaRuntimeVendorName" );
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VERSION_PROPERTY, "8.0.16");
        // When...
        String contextName = SSLTLSContextNameSelector.getSelectedSSLContextName(testProps);
        // Then...
        String expected = "SSL_TLSv2";
        assertEquals(expected,contextName);
    }
    
    @Test
    public void testIBMJVM11GivesTLSv12Context() throws Exception {
        // Given...
        Properties testProps = new Properties();
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VENDOR_PROPERTY,"IBMJavaRuntimeVendorName" );
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VERSION_PROPERTY, "11.0.16.1+1");
        // When...
        String contextName = SSLTLSContextNameSelector.getSelectedSSLContextName(testProps);
        // Then...
        String expected = "TLSv1.2";
        assertEquals(expected,contextName);
    }

    @Test
    public void testSystemPropertiesContextIsSelected() throws Exception {
        // Given...
        // User's default Java
        // When...
        String contextName = SSLTLSContextNameSelector.getSelectedSSLContextName();
        // Then...
        boolean ibmJdk = System.getProperty(SSLTLSContextNameSelector.JAVA_VENDOR_PROPERTY).contains("IBM");
        String expected ;
        if (ibmJdk) {
            if (System.getProperty(SSLTLSContextNameSelector.JAVA_VERSION_PROPERTY).startsWith("8.")) {
            	expected="SSL_TLSv2"; 
            }else {
            	expected ="TLSv1.2";
            }
        } else {
            expected = "TLSv1.2";
        }
        assertEquals(expected,contextName);
    }
}