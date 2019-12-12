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
    private DockerServerImpl                    dockerServer;
    private IDynamicResource                    dynamicResource;
    private Map<String, DockerContainerImpl>    containersByTag = new HashMap<String, DockerContainerImpl>();
    private boolean                             dockerServerChecked;

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
    public DockerContainerImpl provisionDockerContainer(String tag, String imageName, boolean start)
            throws DockerProvisionException {
        DockerContainerImpl container = containersByTag.get(tag);
        DockerServerImpl server = buildDockerServer();
        if (container != null) {
            logger.info("Container already provisioned: " + tag);
            return container;
        }

        if (dockerServerChecked != true) {
            server.checkServer();
            dockerServerChecked = true;
        }

        try{
            DockerSlotImpl slot = provisionDockerSlot();

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
        if (this.dockerServer != null) {
			this.dockerServer.checkServer();
			dockerServerChecked = true;
		}

		for(DockerContainerImpl container : getContainers()) {
            container.checkContainer();
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
    public DockerServerImpl getDockerServerImpl() throws DockerManagerException {
        return dockerServer;
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
       String dockerHost = dockerServer.getHost();

       try {
           String currentSlot = dss.get("server." + dockerHost + ".current.slots");
            if (currentSlot == null){
                return;
            }
            
            int usedSlots = Integer.parseInt(currentSlot);
            usedSlots--;
            if (usedSlots < 0) {
                usedSlots = 0;
            }
            dynamicResource.delete(dockerSlot.getResourcePropertyKeys());

            String prefix = "server." + dockerHost + ".slot." + dockerSlot.getSlotName();
           HashMap<String,String> otherProps = new HashMap<>();
            otherProps.put("slot.run." + framework.getTestRunName() + "." + dockerSlot.getSlotName(), "free");
            otherProps.put("slot.run." + framework.getTestRunName() + "." + "server." + dockerHost + ".slot." + dockerSlot.getSlotName(), "finished");
            if(!dss.putSwap("server." + dockerHost + ".current.slots", currentSlot, Integer.toString(usedSlots), otherProps)) {
                Thread.sleep(200);
                freeDockerSlot(dockerSlot);
                return;
            }

            HashSet<String> delProps = new HashSet<>();
            delProps.add(prefix);
            delProps.add(prefix + ".allocated");
            dss.delete(delProps);
            logger.info("Discarding slot: " + dockerSlot.getSlotName() + ". on the socker server: " + dockerHost);
        }catch (Exception e) {
            logger.warn("Failed to free slot on server " + dockerHost + ", slot " + dockerSlot.getSlotName() + ", leaving for manager clean up routines", e);
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
        return buildDockerServer();
    }

    /**
     * Creates the docker server.
     * 
     * @return DockerServerImpl
     * @throws DockerProvisionException
     */
    private DockerServerImpl buildDockerServer() throws DockerProvisionException {
        if (dockerServer == null ) {
            dockerServer = new DockerServerImpl(framework, dockerManager);
        }
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
            annotation.start());
    }

    /**
     * Provisions a docker slot for a docker container.
     * @return
     * @throws DockerProvisionException
     * @throws DockerManagerException
     */
    private DockerSlotImpl provisionDockerSlot() throws DockerProvisionException, DockerManagerException {
        String              runName = framework.getTestRunName();
        String              dockerHost = dockerServer.getHost();

        this.dynamicResource = this.dss.getDynamicResource("server." + dockerHost);

        if (allocateDssSlot(dockerHost)) {
            return createDssDockerSlot(dockerHost, runName);
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
    private boolean allocateDssSlot(String dockerHost) {
        String slotKey = "server." + dockerHost + ".current.slots";
        try {
            int maxSlots = Integer.parseInt(DockerSlots.get(dockerHost));
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
    private DockerSlotImpl createDssDockerSlot(String dockerHost, String runName)
            throws DockerProvisionException {
        String slotNamePrefix = "SLOT_" + runName + "_";
        String allocatedSlotName;
        String allocatedTime = Instant.now().toString();
        
        try {
            for (int i=0;;i++) {
                allocatedSlotName = slotNamePrefix + i;

                String slotPropertyKey = "server." + dockerHost + ".slot." + allocatedSlotName;

                HashMap<String,String> otherProps = new HashMap<>();
                otherProps.put("slot.run." + runName + "." + allocatedSlotName, "active");

                if (dss.putSwap(slotPropertyKey, null, runName, otherProps)) { 
                    String resourcePropertyPrefix = "slot." + allocatedSlotName;

                    HashMap<String, String> resProps = new HashMap<>();
                    resProps.put(resourcePropertyPrefix, runName);
                    resProps.put(resourcePropertyPrefix + ".allocated", allocatedTime);
                    
                    dynamicResource.put(resProps);
                    
                    return new DockerSlotImpl(dockerManager, allocatedSlotName, resProps);
                }
            }
        } catch (DynamicStatusStoreException e) {
            throw new DockerProvisionException("Unable to set slot in DSS", e);
        }
    } 

    public static void deleteDss(String runName, String dockerServerHost, String slotName, IDynamicStatusStoreService dss) {
        try {
            IDynamicResource dynamicResource = dss.getDynamicResource("server." + dockerServerHost);
            String resPrefix = "slot." + slotName;

            HashSet<String> resProps = new HashSet<>();
            resProps.add(resPrefix + ".run");
            resProps.add(resPrefix + ".allocated");
            dynamicResource.delete(resProps);

            String prefix = "server." + dockerServerHost + ".slot." + slotName;
            String runSlot = dss.get("slot.run." + runName + "." + prefix);
            if("active".equals(runSlot)) {
                if (dss.putSwap("slot.run." + runName + "." + prefix, "active", "free")) {
                    while(true) {
                        String slots = dss.get("server." + dockerServerHost + ".current.slots");
                        int currentSlots = Integer.parseInt(slots);
                        currentSlots--;
                        if (currentSlots < 0) {
                            currentSlots = 0;
                        }
                        
                        if (dss.putSwap("image." + dockerServerHost + ".current.slots", slots, Integer.toString(currentSlots))) {
                            break;
                        }

                        Thread.sleep(100);
                    }
                }
            }

            HashSet<String> props = new HashSet<>();
			props.add(prefix);
			props.add("slot.run." + runName + "." + prefix);
			dss.delete(props);
        } catch (Exception e) {
            logger.error("Failed to discard slot " + slotName +" on docker server " + dockerServerHost, e);
        }
    }

}