/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal.resourcemanagement;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.sem.internal.SemPoolApplids;

public class ApplidResourceMonitor implements Runnable {

    private final IFramework                 framework;
    private final IResourceManagement        resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger = LogFactory.getLog(this.getClass());
    private final Pattern                    applidRunPattern = Pattern.compile("^run\\.(\\w+)\\.applid\\.(\\w+)$");

    public ApplidResourceMonitor(IFramework framework, 
            IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, 
            SemResourceManagement semResourceManagement,
            IConfigurationPropertyStoreService cps) {
        this.framework          = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("SEM Applid resource monitor initialised");
    }

    
    @Override
    public void run() {
        logger.info("Starting Run Applid search");
        try {
            //*** Find all the runs with applids
            Map<String, String> runs = dss.getPrefix("run.");
            
            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for(String key : runs.keySet()) {
                Matcher matcher = applidRunPattern.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String applid  = matcher.group(2);

                        logger.info("Discarding applid " + applid + " as run " + runName + " has gone");

                        try {
                            SemPoolApplids.deleteDss(runName, applid, dss);
                        } catch(Exception e) {
                            logger.error("Failed to discard applid " + applid +  " for run " + runName);
                        }
                    }
                }
            }
        } catch(Exception e) {
            logger.error("Failure during applid scan",e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Run Applid search");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> applidRuns = dss.getPrefix("run." + runName + ".applid.");
            for(String key : applidRuns.keySet()) {
                Matcher matcher = applidRunPattern.matcher(key);
                if (matcher.find()) {
                    String applid = matcher.group(2);

                    logger.info("Discarding applid " + applid + " as run " + runName + " has gone");

                    try {
                        SemPoolApplids.deleteDss(runName, applid, dss);
                    } catch(Exception e) {
                        logger.error("Failed to discard applid " + applid  + " for run " + runName);
                    }
                }
            }
        } catch(Exception e) {
            logger.error("Failed to delete applid for run " + runName);
        }
    }

}
