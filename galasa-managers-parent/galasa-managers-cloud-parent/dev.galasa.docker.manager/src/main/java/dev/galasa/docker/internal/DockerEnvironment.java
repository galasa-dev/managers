package dev.galasa.docker.internal;

import static dev.galasa.docker.internal.DockerManagerImpl.NAMESPACE;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.DockerServer;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerServer;
import dev.galasa.docker.internal.properties.DockerSlots;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

/**
 * Docker Environment. Manages the flow of both docker containers and slots to a specified docker server
 * 
 * @author James Davies
 */
public class DockerEnvironment implements IDockerEnvironment {
    private IFramework                          framework;
    private DockerManagerImpl                   dockerManager;
    private IDynamicStatusStoreService          dss;
    // private DockerServerImpl                    dockerServer;
    private IDynamicResource                    dynamicResource;
    private Map<String, DockerContainerImpl>    containersByTag = new HashMap<>();
    private Map<String, DockerServerImpl>       serversByTag = new HashMap<>();
    private boolean                             dockerServersChecked;

    private final static Log                    logger = LogFactory.getLog(DockerEnvironment.class);

    /**
     * Sets the environment up to use the dss in the docker namespace.
     * 
     * @param framework
     * @param dockerManager
     * @throws DockerManagerException
     */
    public DockerEnvironment(IFramework framework, DockerManagerImpl dockerManager) throws DockerManagerException {
        this.framework = framework;
        this.dockerManager = dockerManager;

        try {
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
        } catch(DynamicStatusStoreException e) {
            throw new DockerManagerException("Failed to create docker environment", e);
        }

    }

    /**
     * Generates any docker containers found in the test class. Also parses any docker server.
     * 
     * @param testClasses
     * @throws DockerProvisionsException
     */
    @Override
    public void generate(List<Class<?>> testClasses) throws DockerProvisionException {
        logger.info("Provisioning docker objects");

        for(Class<?> topTestClass : testClasses) {
            for (Class<?> testClass = topTestClass; testClass != null; testClass = testClass.getSuperclass()) {
                for (Field field : testClass.getDeclaredFields()) {
                    if (field.getType() == IDockerServer.class) {
                        DockerServer annotation = field.getAnnotation(DockerServer.class);
                        if (annotation != null) {
                            provisionDockerServer(annotation);
                        }
                    }
                    if (field.getType() == IDockerContainer.class) {
						DockerContainer annotation = field.getAnnotation(DockerContainer.class);
						if (annotation != null) {
							provisionDockerContainer(annotation);
						}
					}
                }
            }
        }
    }

    /**
     * Provisions the specified docker containers and stored the tag.
     * 
     * @param tag
     * @param imageName
     * @param start (boolean)
     * @return DockerContainerImpl
     * @throws DockerProvisionException
     */
    @Override
    public DockerContainerImpl provisionDockerContainer(String tag, String imageName, boolean start, String dockerServerTag)
            throws DockerProvisionException {
        DockerContainerImpl container = containersByTag.get(tag);
        DockerServerImpl server = serversByTag.get(dockerServerTag);
        if (container != null) {
            logger.info("Container already provisioned: " + tag);
            return container;
        }

        if (server == null) {
            server = buildDockerServer(dockerServerTag);
            serversByTag.put(dockerServerTag, server);
        }

        if (dockerServersChecked != true) {
            server.checkServer();
            dockerServersChecked = true;
        }

        try{
            DockerSlotImpl slot = provisionDockerSlot(server);

            DockerImageImpl image = new DockerImageImpl(framework, dockerManager, server, imageName);

            container = new DockerContainerImpl(framework, dockerManager, tag, server, image, start, slot);
            containersByTag.put(tag, container);

            logger.debug("Docker Container '" + tag + "' was provisioned as slot '" + container.getDockerSlot().getSlotName());

            return container;
        } catch (DockerManagerException e) {
            throw new DockerProvisionException("Failed to gather resources.", e);
        }
    }
    
    /**
     * Builds all the recorded docker containers
     * 
     * @param testClasses
     * @throws DockerProvisionException
     */
    @Override
    public void build(List<Class<?>> testClasses) throws DockerProvisionException {
        if (!serversByTag.isEmpty()) {
            checkDockerServers();
            dockerServersChecked = true;
        }
        // if (this.dockerServer != null) {
		// 	this.dockerServer.checkServer();
		// 	dockerServersChecked = true;
		// }

		for(DockerContainerImpl container : getContainers()) {
            container.checkContainer();
		}
    }

    private void checkDockerServers() throws DockerProvisionException {
        for (String id :serversByTag.keySet()) {
            serversByTag.get(id).checkServer();
        }
    }

    /**
     * Discrads the entire docker environment, stopping and deleting all docker containers in instance.
     * 
     * @throws DockerManagerException
     */
    @Override
    public void discard() throws DockerManagerException {
        for (DockerContainerImpl container: containersByTag.values() ) {
                container.discard();
        }
    }

    /**
     * Returns the docker server.
     */
    @Override
    public DockerServerImpl getDockerServerImpl(String dockerServerTag) throws DockerManagerException {
        if (serversByTag.containsKey(dockerServerTag)) {
            return serversByTag.get(dockerServerTag);
        }
        throw new DockerManagerException("Unable to find docker server with the tag: " + dockerServerTag);
    }

    /**
     * Returns the container implementation for a given tag
     * 
     * @param dockerContainerTag
     * @return DockerContainerImpl
     * @throws DockerManagerException
     */
    @Override
    public DockerContainerImpl getDockerContainerImpl(String dockerContainerTag) throws DockerManagerException {
        if (containersByTag.containsKey(dockerContainerTag)) {
            return containersByTag.get(dockerContainerTag);
        }
        throw new DockerManagerException("Unable to find docker container with the tag: " + dockerContainerTag);
    }

    /**
     * Returns the collection of docker container tags
     * 
     * @return Collections<DockerContainerImpl>
     */
    @Override
    public Collection<DockerContainerImpl> getContainers() {
        return containersByTag.values();
    }

    /**
     * Free a specifed docker slot in terms of the container and environment
     * 
     * @param dockerSlot
     * @throws DockerProvisionException
     */
    @Override
    public void freeDockerSlot(DockerSlotImpl dockerSlot) throws DockerProvisionException {
        DockerServerImpl dockerServer = dockerSlot.getDockerServer();
        String dockerServerId = dockerServer.getServerId();

        try {
            String currentSlot = dss.get("server." + dockerServerId + ".current.slots");
            if (currentSlot == null){
                return;
            }
            
            int usedSlots = Integer.parseInt(currentSlot);
            usedSlots--;
            if (usedSlots < 0) {
                usedSlots = 0;
            }
            dynamicResource.delete(dockerSlot.getResourcePropertyKeys());

            String prefix = "server." + dockerServerId + ".slot." + dockerSlot.getSlotName();
            HashMap<String,String> otherProps = new HashMap<>();
            otherProps.put("slot." + dockerServerId + ".run." + framework.getTestRunName() + "." + dockerSlot.getSlotName(), "free");
            if(!dss.putSwap("server." + dockerServerId + ".current.slots", currentSlot, Integer.toString(usedSlots), otherProps)) {
                Thread.sleep(200);
                freeDockerSlot(dockerSlot);
                return;
            }

            HashSet<String> delProps = new HashSet<>();
            delProps.add(prefix);
            delProps.add(prefix + ".allocated");
            dss.delete(delProps);
            logger.info("Discarding slot: " + dockerSlot.getSlotName() + ". on the socker server: " + dockerServerId);
        }catch (Exception e) {
            logger.warn("Failed to free slot on server " + dockerServerId + ", slot " + dockerSlot.getSlotName() + ", leaving for manager clean up routines", e);
        }
    }

    /**
     * Builds the docker server from the given annotation.
     * 
     * @param annotation
     * @return
     * @throws DockerProvisionException
     */
    private DockerServerImpl provisionDockerServer(DockerServer annotation) throws DockerProvisionException {
        return buildDockerServer(annotation.dockerServerTag());
    }

    /**
     * Creates the docker server.
     * 
     * @return DockerServerImpl
     * @throws DockerProvisionException
     */
    private DockerServerImpl buildDockerServer(String dockerServerTag) throws DockerProvisionException {
        if (serversByTag.containsKey(dockerServerTag)) {
            logger.info("dockerServer already built, returning that.");
            return serversByTag.get(dockerServerTag);
        }
        DockerServerImpl dockerServer = new DockerServerImpl(framework, dockerManager, dockerServerTag);

        serversByTag.put(dockerServerTag, dockerServer);

        return dockerServer;
    }

    /**
     * Provisions the docker containers from the annotations in the test classs
     * 
     * @param annotation
     * @return DockerContainerImpl
     * @throws DockerProvisionException
     */
    private DockerContainerImpl provisionDockerContainer(DockerContainer annotation) throws DockerProvisionException {
        return provisionDockerContainer(
            "GALASA_"+ annotation.dockerContainerTag().trim().toUpperCase(), 
            annotation.image(), 
            annotation.start(),
            annotation.DockerServerTag());
    }

    /**
     * Provisions a docker slot for a docker container.
     * @return
     * @throws DockerProvisionException
     * @throws DockerManagerException
     */
    private DockerSlotImpl provisionDockerSlot(DockerServerImpl server) throws DockerProvisionException, DockerManagerException {
        String              runName = framework.getTestRunName();
        String              dockerServerId = server.getServerId();

        this.dynamicResource = this.dss.getDynamicResource("server." + dockerServerId);

        if (allocateDssSlot(dockerServerId, server)) {
            return createDssDockerSlot(dockerServerId, runName, server);
        } else {
            discard();
            logger.info("No available slots currently");
            throw new DockerProvisionException("No Docker slots available.");
        }
    }

    /**
     * Used during the provisioning of a docker slot to allocate a slot within the DSS to lock the resource.
     * 
     * @param dockerHost
     * @return boolean (failed/passed)
     */
    private boolean allocateDssSlot(String dockerServerId, DockerServerImpl server) {
        String slotKey = "server." + dockerServerId + ".current.slots";
        try {
            int maxSlots = Integer.parseInt(DockerSlots.get(server));
            int usedSlots = 0;
            String currentSlots = dss.get(slotKey);

            if(currentSlots != null) {
                usedSlots = Integer.parseInt(currentSlots);
            }
            if (usedSlots >= maxSlots) {
                return false;
            }
            usedSlots++;
            String slotIncrease = Integer.toString(usedSlots);
            return dss.putSwap(slotKey, currentSlots, slotIncrease);
        } catch (DockerManagerException e) {
            logger.error("Could not find number of docker slots in CPS");
        } catch (DynamicStatusStoreException e) {
            logger.warn("Could not perform putswap on dss");
        }
        return false;
    }

    /**
     * Used during the provisioning of a docker slot to claim a slot name and set the resource in the dss.
     * 
     * @param dockerHost
     * @param runName
     * @return
     * @throws DockerProvisionException
     */
    private DockerSlotImpl createDssDockerSlot(String dockerServerId, String runName, DockerServerImpl dockerServer)
            throws DockerProvisionException {
        String slotNamePrefix = "SLOT_" + runName + "_";
        String allocatedSlotName;
        String allocatedTime = Instant.now().toString();
        
        try {
            for (int i=0;;i++) {
                allocatedSlotName = slotNamePrefix + i;

                String slotPropertyKey = "server." + dockerServerId + ".slot." + allocatedSlotName;

                HashMap<String,String> otherProps = new HashMap<>();
                otherProps.put("slot."+ dockerServerId + ".run." + runName + "." + allocatedSlotName, "active");

                if (dss.putSwap(slotPropertyKey, null, runName, otherProps)) { 
                    String resourcePropertyPrefix = "slot." + allocatedSlotName;

                    HashMap<String, String> resProps = new HashMap<>();
                    resProps.put(resourcePropertyPrefix, runName);
                    resProps.put(resourcePropertyPrefix + ".allocated", allocatedTime);
                    
                    dynamicResource.put(resProps);
                    
                    return new DockerSlotImpl(dockerManager, dockerServer, allocatedSlotName, resProps);
                }
            }
        } catch (DynamicStatusStoreException e) {
            throw new DockerProvisionException("Unable to set slot in DSS", e);
        }
    } 

    public static void deleteDss(String runName, String dockerServerId, String slotName, IDynamicStatusStoreService dss) {
        try {
            IDynamicResource dynamicResource = dss.getDynamicResource("server." + dockerServerId);
            String resPrefix = "slot." + slotName;

            HashSet<String> resProps = new HashSet<>();
            resProps.add(resPrefix + ".run");
            resProps.add(resPrefix + ".allocated");
            dynamicResource.delete(resProps);

            String prefix = "server." + dockerServerId + ".slot." + slotName;
            String runSlot = dss.get("slot." + dockerServerId + ".run." + runName + "." + prefix);
            if("active".equals(runSlot)) {
                if (dss.putSwap("slot." + dockerServerId + ".run." + runName + "." + prefix, "active", "free")) {
                    while(true) {
                        String slots = dss.get("server." + dockerServerId + ".current.slots");
                        int currentSlots = Integer.parseInt(slots);
                        currentSlots--;
                        if (currentSlots < 0) {
                            currentSlots = 0;
                        }
                        
                        if (dss.putSwap("image." + dockerServerId + ".current.slots", slots, Integer.toString(currentSlots))) {
                            break;
                        }

                        Thread.sleep(100);
                    }
                }
            }

            HashSet<String> props = new HashSet<>();
			props.add(prefix);
			props.add("slot." + dockerServerId + ".run." + runName + "." + prefix);
			dss.delete(props);
        } catch (Exception e) {
            logger.error("Failed to discard slot " + slotName +" on docker server " + dockerServerId, e);
        }
    }

}