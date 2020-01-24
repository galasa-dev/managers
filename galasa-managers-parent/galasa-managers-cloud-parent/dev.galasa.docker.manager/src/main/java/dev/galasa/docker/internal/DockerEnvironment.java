package dev.galasa.docker.internal;

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
import dev.galasa.docker.DockerEngine;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerEngine;
import dev.galasa.docker.internal.properties.DockerSlots;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

/**
 * Docker Environment. Manages the flow of both docker containers and slots to a specified docker engine
 * 
 * @author James Davies
 */
public class DockerEnvironment implements IDockerEnvironment {
    private IFramework                          framework;
    private DockerManagerImpl                   dockerManager;
    private IDynamicStatusStoreService          dss;
    private IDynamicResource                    dynamicResource;
    private Map<String, DockerContainerImpl>    containersByTag = new HashMap<>();
    private Map<String, DockerEngineImpl>       enginesByTag = new HashMap<>();
    private boolean                             dockerEnginesChecked;

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
            this.dss = framework.getDynamicStatusStoreService(dockerManager.NAMESPACE);
        } catch(DynamicStatusStoreException e) {
            throw new DockerManagerException("Failed to create docker environment", e);
        }

    }

    /**
     * Generates any docker containers found in the test class. Also parses any docker engine.
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
                    if (field.getType() == IDockerEngine.class) {
                        DockerEngine annotation = field.getAnnotation(DockerEngine.class);
                        if (annotation != null) {
                            provisionDockerEngine(annotation);
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
    public DockerContainerImpl provisionDockerContainer(String tag, String imageName, boolean start, String dockerEngineTag)
            throws DockerProvisionException {
        DockerContainerImpl container = containersByTag.get(tag);
        DockerEngineImpl engine = enginesByTag.get(dockerEngineTag);
        if (container != null) {
            logger.info("Container already provisioned: " + tag);
            return container;
        }

        if (engine == null) {
            engine = buildDockerEngine(dockerEngineTag);
            enginesByTag.put(dockerEngineTag, engine);
        }

        if (dockerEnginesChecked != true) {
            engine.checkEngine();
            dockerEnginesChecked = true;
        }

        try{
            DockerSlotImpl slot = provisionDockerSlot(engine);

            DockerImageImpl image = new DockerImageImpl(framework, dockerManager, engine, imageName);

            container = new DockerContainerImpl(framework, dockerManager, tag, engine, image, start, slot);
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
        if (!enginesByTag.isEmpty()) {
            checkDockerEngines();
            dockerEnginesChecked = true;
        }
        // if (this.dockerEngine != null) {
		// 	this.dockerEngine.checkEngine();
		// 	dockerEnginesChecked = true;
		// }

		for(DockerContainerImpl container : getContainers()) {
            container.checkContainer();
		}
    }

    private void checkDockerEngines() throws DockerProvisionException {
        for (String id :enginesByTag.keySet()) {
            enginesByTag.get(id).checkEngine();
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
     * Returns the docker engine.
     */
    @Override
    public DockerEngineImpl getDockerEngineImpl(String dockerEngineTag) throws DockerManagerException {
        if (enginesByTag.containsKey(dockerEngineTag)) {
            return enginesByTag.get(dockerEngineTag);
        }
        throw new DockerManagerException("Unable to find docker engine with the tag: " + dockerEngineTag);
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
     * Builds the docker engine from the given annotation.
     * 
     * @param annotation
     * @return
     * @throws DockerProvisionException
     */
    private DockerEngineImpl provisionDockerEngine(DockerEngine annotation) throws DockerProvisionException {
        return buildDockerEngine(annotation.dockerEngineTag());
    }

    /**
     * Creates the docker engine.
     * 
     * @return DockerEngineImpl
     * @throws DockerProvisionException
     */
    private DockerEngineImpl buildDockerEngine(String dockerEngineTag) throws DockerProvisionException {
        if (enginesByTag.containsKey(dockerEngineTag)) {
            logger.info("dockerEngine already built, returning that.");
            return enginesByTag.get(dockerEngineTag);
        }
        DockerEngineImpl dockerEngine = new DockerEngineImpl(framework, dockerManager, dockerEngineTag);

        enginesByTag.put(dockerEngineTag, dockerEngine);

        return dockerEngine;
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
            annotation.DockerEngineTag());
    }

    /**
     * Provisions a docker slot for a docker container.
     * @return
     * @throws DockerProvisionException
     * @throws DockerManagerException
     */
    private DockerSlotImpl provisionDockerSlot(DockerEngineImpl engine) throws DockerProvisionException, DockerManagerException {
        String              runName = framework.getTestRunName();
        String              dockerEngineId = engine.getEngineId();

        this.dynamicResource = this.dss.getDynamicResource("engine." + dockerEngineId);

       return allocateAndCreateDssSlot(dockerEngineId, runName, engine);
    }

    /**
     * Used during the provisioning of a docker slot to allocate a slot within the
     * DSS to lock the resource.
     * 
     * @param dockerHost
     * @return boolean (failed/passed)
     * @throws DockerProvisionException
     */
    private DockerSlotImpl allocateAndCreateDssSlot(String dockerEngineId, String runName, DockerEngineImpl engine)
            throws DockerProvisionException {
        String slotKey = "engine." + dockerEngineId + ".current.slots";
        String slotNamePrefix = "SLOT_" + runName + "_";
        String allocatedSlotName;
        String slotPropertyKey;
        String allocatedTime = Instant.now().toString();
        
        HashMap<String,String> slotProps = new HashMap<>();

        try {
            int maxSlots = Integer.parseInt(DockerSlots.get(engine));
            int usedSlots = 0;
            String currentSlots = dss.get(slotKey);

            if(currentSlots != null) {
                usedSlots = Integer.parseInt(currentSlots);
            }
            if (usedSlots >= maxSlots) {
                throw new DockerProvisionException("Not enough available slots");
            }
            usedSlots++;
            String slotIncrease = Integer.toString(usedSlots);

            for (int i=0;;i++) {
                slotProps.clear();
                allocatedSlotName = slotNamePrefix + i;
                slotPropertyKey = "engine." + dockerEngineId + ".slot." + allocatedSlotName;                
                slotProps.put("slot."+ dockerEngineId + ".run." + runName + "." + allocatedSlotName, "active");
                if (dss.putSwap(slotPropertyKey, null, runName)) {
                    break;
                }
            }

            if (dss.putSwap(slotKey, currentSlots, slotIncrease, slotProps)) {
                String resourcePropertyPrefix = "slot." + allocatedSlotName;

                HashMap<String, String> resProps = new HashMap<>();
                resProps.put(resourcePropertyPrefix, runName);
                resProps.put(resourcePropertyPrefix + ".allocated", allocatedTime);
                
                dynamicResource.put(resProps);
                
                return new DockerSlotImpl(dockerManager, engine, allocatedSlotName, resProps);
            }
            else {
                //Retry as another slot was allocated during the allocation of this slot
                dss.delete(slotPropertyKey);
                return allocateAndCreateDssSlot(dockerEngineId, runName, engine);
            }
        } catch (DockerManagerException e) {
            logger.error("Could not find number of docker slots in CPS");
        } catch (DynamicStatusStoreException e) {
            logger.warn("Could not perform putswap on dss");
        }
        throw new DockerProvisionException("Failed to provision docker slot");
    }

    /**
     * Free a specifed docker slot in terms of the container and environment
     * 
     * @param dockerSlot
     * @throws DockerProvisionException
     */
    @Override
    public void freeDockerSlot(DockerSlotImpl dockerSlot) throws DockerProvisionException {
        DockerEngineImpl dockerEngine = dockerSlot.getDockerEngine();
        String dockerEngineId = dockerEngine.getEngineId();

        try {
            String currentSlot = dss.get("engine." + dockerEngineId + ".current.slots");
            if (currentSlot == null){
                return;
            }
            
            int usedSlots = Integer.parseInt(currentSlot);
            usedSlots--;
            if (usedSlots < 0) {
                usedSlots = 0;
            }
            dynamicResource.delete(dockerSlot.getResourcePropertyKeys());

            String prefix = "engine." + dockerEngineId + ".slot." + dockerSlot.getSlotName();
            String slotKey = "slot." + dockerEngineId + ".run." + framework.getTestRunName() + "." + dockerSlot.getSlotName();
            HashMap<String,String> otherProps = new HashMap<>();
            otherProps.put(slotKey, "free");
            if(!dss.putSwap("engine." + dockerEngineId + ".current.slots", currentSlot, Integer.toString(usedSlots), otherProps)) {
                Thread.sleep(200);
                freeDockerSlot(dockerSlot);
                return;
            }

            HashSet<String> delProps = new HashSet<>();
            delProps.add(prefix);
            delProps.add(prefix + ".allocated");
            delProps.add(slotKey);
            dss.delete(delProps);
            logger.info("Discarding slot: " + dockerSlot.getSlotName() + ". on the socker engine: " + dockerEngineId);
        }catch (Exception e) {
            logger.warn("Failed to free slot on engine " + dockerEngineId + ", slot " + dockerSlot.getSlotName() + ", leaving for manager clean up routines", e);
        }
    }

    /**
     * Used by resource management to clean up stale properties
     * 
     * @param runName
     * @param dockerEngineId
     * @param slotName
     * @param dss
     */
    public static void deleteStaleDssSlot(String runName, String dockerEngineId, String slotName, IDynamicStatusStoreService dss) {
        try {
            IDynamicResource dynamicResource = dss.getDynamicResource("engine." + dockerEngineId);
            String resPrefix = "slot." + slotName;

            HashSet<String> resProps = new HashSet<>();
            resProps.add(resPrefix + ".run");
            resProps.add(resPrefix + ".allocated");
            dynamicResource.delete(resProps);

            String prefix = "engine." + dockerEngineId + ".slot." + slotName;
            String runSlot = dss.get("slot." + dockerEngineId + ".run." + runName + "." + prefix);
            if("active".equals(runSlot)) {
                if (dss.putSwap("slot." + dockerEngineId + ".run." + runName + "." + prefix, "active", "free")) {
                    while(true) {
                        String slots = dss.get("engine." + dockerEngineId + ".current.slots");
                        int currentSlots = Integer.parseInt(slots);
                        currentSlots--;
                        if (currentSlots < 0) {
                            currentSlots = 0;
                        }
                        
                        if (dss.putSwap("image." + dockerEngineId + ".current.slots", slots, Integer.toString(currentSlots))) {
                            break;
                        }

                        Thread.sleep(100);
                    }
                }
            }

            HashSet<String> props = new HashSet<>();
			props.add(prefix);
			props.add("slot." + dockerEngineId + ".run." + runName + "." + prefix);
			dss.delete(props);
        } catch (Exception e) {
            logger.error("Failed to discard slot " + slotName +" on docker engine " + dockerEngineId, e);
        }
    }

}