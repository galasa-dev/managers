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
import dev.galasa.sem.internal.SemPoolPorts;

public class PortResourceMonitor implements Runnable {

    private final IFramework                 framework;
    private final IResourceManagement        resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger = LogFactory.getLog(this.getClass());
    private final Pattern                    portRunPattern = Pattern.compile("^run\\.(\\w+)\\.port\\.(\\w+)$");

    public PortResourceMonitor(IFramework framework, 
            IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, 
            SemResourceManagement semResourceManagement,
            IConfigurationPropertyStoreService cps) {
        this.framework          = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("SEM Port resource monitor initialised");
    }

    
    @Override
    public void run() {
        logger.info("Starting Run Port search");
        try {
            //*** Find all the runs with port
            Map<String, String> runs = dss.getPrefix("run.");
            
            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for(String key : runs.keySet()) {
                Matcher matcher = portRunPattern.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String port  = matcher.group(2);

                        logger.info("Discarding port " + port + " as run " + runName + " has gone");

                        try {
                            SemPoolPorts.deleteDss(runName, port, dss);
                        } catch(Exception e) {
                            logger.error("Failed to discard port " + port +  " for run " + runName);
                        }
                    }
                }
            }
        } catch(Exception e) {
            logger.error("Failure during port scan",e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Run Port search");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> portRuns = dss.getPrefix("run." + runName + ".port.");
            for(String key : portRuns.keySet()) {
                Matcher matcher = this.portRunPattern.matcher(key);
                if (matcher.find()) {
                    String port = matcher.group(2);

                    logger.info("Discarding port " + port + " as run " + runName + " has gone");

                    try {
                        SemPoolPorts.deleteDss(runName, port, dss);
                    } catch(Exception e) {
                        logger.error("Failed to discard port " + port  + " for run " + runName);
                    }
                }
            }
        } catch(Exception e) {
            logger.error("Failed to delete port for run " + runName);
        }
    }

}
