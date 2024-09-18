/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.hursley.cicsts.test.sem.interfaces.complex.IPool;

import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DssResourceDeletePrefix;
import dev.galasa.framework.spi.DssResourceUpdate;
import dev.galasa.framework.spi.DssUpdate;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.sem.SemManagerException;
import dev.galasa.sem.internal.properties.ModelPorts;

public class SemPoolPorts implements IPool {

    private static final Log logger = LogFactory.getLog(SemPoolPorts.class);

    private final SemManagerImpl             manager;
    private final IDynamicStatusStoreService dss;
    private final IResourcePoolingService    rps;

    private final String runName;

    private final List<String> resourceStrings;

    private final ArrayList<String> rejectedPorts = new ArrayList<>();
    private final ArrayList<String> availablePorts = new ArrayList<>();
    private final ArrayList<String> usedPorts = new ArrayList<>();

    public SemPoolPorts(SemManagerImpl manager, IDynamicStatusStoreService dss, IResourcePoolingService rps) throws SemManagerException {
        this.manager = manager;
        this.dss     = dss;
        this.rps     = rps;
        this.runName = this.manager.getFramework().getTestRunName();
        this.resourceStrings = ModelPorts.get();
    }



    private void reservePorts() throws SemManagerException {
        while(this.availablePorts.isEmpty()) {
            List<String> possiblePorts;
            try {
                possiblePorts = this.rps.obtainResources(this.resourceStrings, this.rejectedPorts, 10, 10, dss, "port");

                // Quickly reserve them to help maintain blocks of ports,  will free them 
                for(String possiblePort : possiblePorts) {
                    try {
                        this.dss.performActions(
                                new DssAdd("port." + possiblePort, this.runName), 
                                new DssAdd("run." + this.runName + ".port." + possiblePort, "active"),
                                new DssResourceUpdate("port." + possiblePort + ".run", this.runName)
                                );
                        this.availablePorts.add(possiblePort);
                        logger.trace("Reserved PORT '" + possiblePort + "' for SEM complex");
                    } catch (DynamicStatusStoreException e) {
                        this.rejectedPorts.add(possiblePort);
                    }
                }
            } catch (InsufficientResourcesAvailableException e) {
                throw new SemManagerException("Not enough ports for this run", e);
            }
        }   
    }



    @Override
    public String getNextValue() {
        if (availablePorts.isEmpty()) {
            try {
                reservePorts();
            } catch (SemManagerException e) {
                logger.error("Reservation of SEM PORTs failed",e);
            }
        }

        if (availablePorts.isEmpty()) {
            return null;
        }

        String nextPort = availablePorts.remove(0);
        usedPorts.add(nextPort);

        logger.debug("Allocated PORT '" + nextPort + "' to SEM complex");

        return nextPort;
    }

    public void generateComplete() {
        for(String port : this.availablePorts) {
            freePort(port);
            logger.trace("Freed PORT '" + port + "' from reservation list");
        }
    }



    private void freePort(String port) {
        try {
            deleteDss(this.runName, port, this.dss);
        } catch(DynamicStatusStoreException e) {
            logger.debug("Failed to release port '" + port + "', leaving for resource management",e);
        }
    }



    public void discard() {
        // Free any ports remaining in available, shouldn't be, but just in case

        for(String port : this.availablePorts) {
            freePort(port);
        }

        // Free any used Ports

        for(String port : this.usedPorts) {
            //TODO Call commserver to reset the port on the zos image 
            freePort(port);
            logger.debug("Discarded PORT '" + port + "'");
        }       
    }



    public void setSystem(String sysid) throws SemManagerException {
        for(String port : usedPorts) {
            try {
                this.dss.performActions(
                        new DssUpdate("port." + port + ".image", sysid)
                        );
            } catch(DynamicStatusStoreException e) {
                throw new SemManagerException("Failed to add port systems in DSS",e);
            }
        }

    }



    public static void deleteDss(String runName, String port, IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
        // TODO - Should be calling zoscommserver manager to deactive and reactive the port
        
        dss.performActions(
                new DssDelete("port." + port, null), 
                new DssDelete("port." + port + ".image", null), 
                new DssDelete("run." + runName + ".port." + port, null),
                new DssResourceDeletePrefix("port." + port + "."));
    }
}
