/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.linux.internal;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.OperatingSystem;
import dev.galasa.linux.spi.ILinuxProvisionedImage;
import dev.galasa.linux.spi.ILinuxProvisioner;

public class LinuxDSEProvisioner implements ILinuxProvisioner {

    private final Log                                logger = LogFactory.getLog(getClass());

    private final LinuxManagerImpl                   manager;
    private final IConfigurationPropertyStoreService cps;

    public LinuxDSEProvisioner(LinuxManagerImpl manager) {

        this.manager = manager;
        this.cps = this.manager.getCps();
    }

    @Override
    public ILinuxProvisionedImage provision(String tag, OperatingSystem operatingSystem, List<String> capabilities)
            throws LinuxManagerException {

        try {
            String hostid = LinuxManagerImpl.nulled(this.cps.getProperty("dse.tag", tag + ".hostid"));

            if (hostid == null) {
                return null;
            }

            logger.info("Loading DSE for Linux Image tagged " + tag);

            return new LinuxDSEImage(manager, this.cps, tag, hostid);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to provision the Linux DSE", e);
        }
    }

}
