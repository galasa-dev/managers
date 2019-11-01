/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.linux.manager.ivt;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.LinuxImage;
import dev.galasa.linux.LinuxIpHost;
import dev.galasa.linux.OperatingSystem;

@Test
public class LinuxManagerIVT {
	
    @Logger
    public Log logger;
    
	@LinuxImage(operatingSystem=OperatingSystem.any)
	public ILinuxImage linuxPrimary;
	
    @LinuxIpHost
    public IIpHost linuxHost;
	
    @Test
    public void checkPrimaryImage() throws Exception {
        if (linuxPrimary == null) {
            throw new Exception("Primary Image is null, should have been filled by the Linux Manager");
        }
        if (linuxHost == null) {
            throw new Exception("Primary Image Host is null, should have been filled by the Linux Manager");
        }
        logger.info("The Primary Image field has been correctly initialised");
    }
    
    @Test
    public void checkCommandShell() throws Exception {
    	
    	ICommandShell commandShell = this.linuxPrimary.getCommandShell();
    	
    	String response = commandShell.issueCommand("uname -a");
    	
    	if (response.isEmpty()) {
    		throw new Exception("Invalid response from the command shell :-\n" + response);
    	}
    	
    }


}
