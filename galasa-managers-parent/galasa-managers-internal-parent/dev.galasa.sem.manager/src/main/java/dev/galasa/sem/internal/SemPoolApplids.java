/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal;

import java.util.ArrayList;
import java.util.HashSet;
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
import dev.galasa.sem.internal.properties.ModelApplids;

public class SemPoolApplids implements IPool {

    private static final Log logger = LogFactory.getLog(SemPoolApplids.class);

    private final SemManagerImpl             manager;
    private final IDynamicStatusStoreService dss;
    private final IResourcePoolingService    rps;

    private final String runName;

    private final List<String> resourceStrings;

    private final ArrayList<String> rejectedApplids = new ArrayList<>();
    private final ArrayList<String> availableApplids = new ArrayList<>();
    private final HashSet<String> usedApplids = new HashSet<>();

    public SemPoolApplids(SemManagerImpl manager, IDynamicStatusStoreService dss, IResourcePoolingService rps) throws SemManagerException {
        this.manager = manager;
        this.dss     = dss;
        this.rps     = rps;
        this.runName = this.manager.getFramework().getTestRunName();
        this.resourceStrings = ModelApplids.get();
    }



    private void reserveApplids() throws SemManagerException {
        while(this.availableApplids.isEmpty()) {
            List<String> possibleApplids;
            try {
                possibleApplids = this.rps.obtainResources(this.resourceStrings, this.rejectedApplids, 10, 10, dss, "applid");

                // Quickly reserve them to help maintain blocks of applids,  will free them 
                for(String possibleApplid : possibleApplids) {
                    try {
                        this.dss.performActions(
                                new DssAdd("applid." + possibleApplid, this.runName), 
                                new DssAdd("run." + this.runName + ".applid." + possibleApplid, "active"),
                                new DssResourceUpdate("applid." + possibleApplid + ".run", this.runName)
                                );
                        this.availableApplids.add(possibleApplid);
                        logger.trace("Reserved APPLID '" + possibleApplid + "' for SEM complex");
                    } catch (DynamicStatusStoreException e) {
                        this.rejectedApplids.add(possibleApplid);
                    }
                }
            } catch (InsufficientResourcesAvailableException e) {
                throw new SemManagerException("Not enough applids for this run", e);
            }
        }   
    }



    @Override
    public String getNextValue() {
        if (availableApplids.isEmpty()) {
            try {
                reserveApplids();
            } catch (SemManagerException e) {
                logger.error("Reservation of SEM APPLIDs failed",e);
            }
        }

        if (availableApplids.isEmpty()) {
            return null;
        }

        String nextApplid = availableApplids.remove(0);
        usedApplids.add(nextApplid);

        logger.debug("Allocated APPLID '" + nextApplid + "' to SEM complex");

        return nextApplid;
    }

    public void generateComplete() {
        for(String applid : this.availableApplids) {
            freeApplid(applid);
            logger.trace("Freed APPLID '" + applid + "' from reservation list");
        }
    }



    private void freeApplid(String applid) {
        try {
            deleteDss(this.runName, applid, this.dss);
        } catch(DynamicStatusStoreException e) {
            logger.debug("Failed to release applid '" + applid + "', leaving for resource management",e);
        }
    }



    public void discard() {
        // Free any applids remaining in available, shouldn't be, but just in case

        for(String applid : this.availableApplids) {
            freeApplid(applid);
        }

        // Free any used Applids

        for(String applid : this.usedApplids) {
            //TODO Call commserver to reset the applid on the zos image 
            freeApplid(applid);
            logger.debug("Discarded APPLID '" + applid + "'");
        }       
    }



    public void setSystem(String applid, String sysid) throws SemManagerException {
        if (!usedApplids.contains(applid)) {
            return;
        }

        if (sysid == null || sysid.isEmpty()) {
            return;
        }

        try {
            this.dss.performActions(
                    new DssUpdate("applid." + applid + ".image", sysid)
                    );
        } catch(DynamicStatusStoreException e) {
            throw new SemManagerException("Failed to add applid systems in DSS",e);
        }
    }



    public static void deleteDss(String runName, String applid, IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
        
        // TODO - Should be calling zoscommserver manager to deactive and reactive the applid
        
        dss.performActions(
                new DssDelete("applid." + applid, null), 
                new DssDelete("applid." + applid + ".image", null), 
                new DssDelete("run." + runName + ".applid." + applid, null),
                new DssResourceDeletePrefix("applid." + applid + "."));
    }
}
