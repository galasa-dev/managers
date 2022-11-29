/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.common;

import java.util.Properties;

public class SSLTLSContextName {

	public static final String JAVA_VENDOR_PROPERTY = "java.vendor";
    public static final String JAVA_VERSION_PROPERTY = "java.version";
    
    
    public static String getSelectedSSLContextName( Properties... props ) {
        Properties p;
        if( props.length == 0){
            p = System.getProperties();
        } else {
            p = props[0];
            }
        boolean ibmJdk = p.getProperty(JAVA_VENDOR_PROPERTY).contains("IBM");
        String name ;
        if (ibmJdk) {
            if (p.getProperty(JAVA_VERSION_PROPERTY).startsWith("8.")) {
            	name="SSL_TLSv2"; 
            }else {
            	name ="TLSv1.2";
            }
        } else {
            name = "TLSv1.2";
        }
        return name ;
    }

}
