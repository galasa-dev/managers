package dev.galasa.docker.internal;

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

/**
 * Docker resource monitor
 * 
 * @author James Davies
 */
public class DockerResourceMonitor implements Runnable {
    private final IFramework                    framework;
    private final IResourceManagement           resourceManagement;
    private final IDynamicStatusStoreService    dss;
    private final Log                           logger = LogFactory.getLog(DockerResourceMonitor.class);
    private final Pattern                       slotRunPattern = Pattern.compile("^slot\\.run\\.(\\w+)\\.server\\.(\\w+)\\.slot\\.(\\w+)$");

   /**
    * Docker resource monitor

    * @param framework
    * @param resourceManagement
    * @param dss
    * @param dockerResourceManagement
    * @param cps
    */
    public DockerResourceMonitor(IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss, 
            DockerResourceManagement dockerResourceManagement, IConfigurationPropertyStoreService cps) {
        this.framework          = framework;
        this.dss                = dss;
        this.resourceManagement = resourceManagement;

        this.logger.info("Docker slot resource monitor intialised");
    }

    /**
     * Runs the docker resource monitor.
     */
    @Override
    public void run() {
        logger.info("Starting search for run slots.");

        try {
            Map<String, String> slotRuns = dss.getPrefix("slot.run.");
			
			Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

			for(String key : slotRuns.keySet()) {
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String runName = matcher.group(1);

					if (!activeRunNames.contains(runName)) {
						String dockerServer = matcher.group(2);
						String slot    = matcher.group(3);

						logger.info("Discarding slot " + slot + " on docker server " + dockerServer + " as run " + runName + " has gone");

						try {
							DockerEnvironment.deleteDss(runName, dockerServer, slot, dss);
						} catch(Exception e) {
							logger.error("Failed to discard slot " + slot + " on image " + dockerServer + " as run " + runName);
						}
					}
				}
			}
        } catch (Exception e) {
            logger.error("Problem when trying run the docker resource monitor.", e);
        }
    }

    /**
     * 
     * @param runName
     */
    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String,String> slotRuns = dss.getPrefix("slot.run." + runName + ".");
            for(String key : slotRuns.keySet()) {
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String dockerServer = matcher.group(2);
					String slot    = matcher.group(3);

					logger.info("Discarding slot " + slot + " on image " + dockerServer + " as run " + runName + " has gone");

					try {
						DockerEnvironment.deleteDss(runName, dockerServer, slot, dss);
					} catch(Exception e) {
						logger.error("Failed to discard slot " + slot + " on image " + dockerServer + " as run " + runName);
					}
				}
			}
        } catch (Exception e) {
            logger.error("message", e);
        }
    }
    
}