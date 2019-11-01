/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.manager.ivt;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentials;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zos.ZosIpHost;
import dev.galasa.zos.ZosManagerException;

public class ZosManagerIVT {
	
    @Logger
    public Log logger;
    
    @ZosImage
    public IZosImage imagePrimary;
    
    @ZosIpHost
    public IIpHost hostPrimary;
    
//    @ZosIpPort
//    public IIpPort portPrimary;
    
    @Test
    public void checkPrimaryImage() throws Exception {
        if (imagePrimary == null) {
            throw new Exception("Primary Image is null, should have been filled by the zOS Manager");
        }
        if (hostPrimary == null) {
            throw new Exception("Primary Image Host is null, should have been filled by the zOS Manager");
        }
//        if (portPrimary == null) {
//            throw new Exception("Primary Image Port is null, should have been filled by the zOS Manager");
//        }
        logger.info("The Primary Image field has been correctly initialised");
    }

    @Test
    public void checkDefaultCredentials() throws Exception, ZosManagerException {
    	ICredentials creds = imagePrimary.getDefaultCredentials();
    	if (creds == null) {
            throw new Exception("Primary Credentials is null");
    	}
        logger.info("The Primary Credentials are being returned");
    }

}
