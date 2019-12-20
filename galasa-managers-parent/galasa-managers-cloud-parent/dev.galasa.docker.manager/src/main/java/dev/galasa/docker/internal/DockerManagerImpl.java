package dev.galasa.docker.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.DockerServer;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerManager;
import dev.galasa.docker.IDockerServer;
import dev.galasa.docker.internal.properties.DockerPropertiesSingleton;
import dev.galasa.docker.internal.properties.DockerRegistry;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.http.spi.IHttpManagerSpi;

/**
 * Docker manager implementation.
 * 
 * Extracting from the test class the two current annotations of @DockerServer and @DockerContainer
 * 
 * @DockerServer - where the containers will be running, set value in CPS (see properties)
 * @DockerContainer - define what container is to be run, image names mus be defined, tag can be set
 * 
 * @author James Davies
 */
@Component(service = { IManager.class })
public class DockerManagerImpl extends AbstractManager implements IDockerManager {
    protected final static String               NAMESPACE = "docker";
    private final static Log                    logger = LogFactory.getLog(DockerManagerImpl.class);
    private IFramework                          framework;
    protected IHttpManagerSpi                   httpManager;
    private IDockerEnvironment                  dockerEnvironment;
    private List<DockerRegistryImpl>            registries = new ArrayList<DockerRegistryImpl>();

    /**
     * Initialies the DockerManager, adding the requirement of the HttpManager
     * 
     * Docker Environment is generated at this stage
     * 
     * @param IFramework - the galasa framework
     * @param List<IManager> - list of all the managers
     * @param List<Imanager> - list of all the active managers
     * @param Class<?> - the test class
     * @throws ManagerException
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {

        super.initialise(framework, allManagers, activeManagers, testClass);
        this.youAreRequired(allManagers, activeManagers);

        try {
            DockerPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException("Failed to set the CPS with the docker namespace", e);
        }

        this.framework = framework;
        dockerEnvironment = new DockerEnvironment(framework, this);
        logger.info("Docker manager intialised");
    }
    
    /**
     * Makes sure that the docker manager is added to the list of active managers, and adds the dependency on http manager.
     * 
     * @param List<IManager> - list of all the managers
     * @param List<IManager> - list of the active managers
     * @throws ManagerException
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this)) {
			return;
		}
        activeManagers.add(this);
        httpManager = addDependentManager(allManagers, activeManagers, IHttpManagerSpi.class);
    }

    /**
     * Generates the docker containers defined in the annotation inside the test class.
     * 
     * @param field
     * @param annotations
     * @return IDockerContainer
     * @throws DockerManagerException
     */
    @GenerateAnnotatedField(annotation = DockerContainer.class)
    public IDockerContainer generateDockerContainer(Field field, List<Annotation> annotations) throws DockerManagerException {
        DockerContainer annotationContainer = field.getAnnotation(DockerContainer.class);
        return this.provisionContainer(annotationContainer.dockerContainerTag(),
                                        annotationContainer.image(),
                                        annotationContainer.start()); 
    }

    /**
     * Private method to provision the dockerContainer described by the generateDockerContainer() in the provisioned docker environment.
     *  
     * @param dockerContainerTag
     * @param image
     * @param start
     * @return
     * @throws DockerManagerException
     */
    private IDockerContainer provisionContainer(String dockerContainerTag, String image, boolean start) throws DockerManagerException {
        try {
            return dockerEnvironment.provisionDockerContainer(dockerContainerTag, image, start);
        } catch (DockerProvisionException e) {
            throw new DockerManagerException("Failed to provision docker container tag: "+ dockerContainerTag, e);
        }
    }

    /**
     * Directs to the docker server where the containers will be provisioned.
     * 
     * @param field
     * @param annotations
     * @return IDockerServer
     * @throws DockerManagerException
     */
    @GenerateAnnotatedField(annotation = DockerServer.class)
    public IDockerServer generateDockerServer(Field field, List<Annotation> annotations) throws DockerManagerException {
        return this.getDockerServer();
    }

    /**
     * Private method for retrieving the docker sever used by the docker environment
     * 
     * @return
     * @throws DockerManagerException
     */
    private IDockerServer getDockerServer() throws DockerManagerException {
        return dockerEnvironment.getDockerServerImpl();
    }

    /**
     * Provision generate step. Extracts the docker manager realted annotations and generates the resources
     * 
     * @throws ManagerException
     */
    @Override
    public void provisionGenerate() throws ManagerException {
        logger.info("Finding all docker related annotations");
        generateDockerFields();
        logger.info("Registering docker registries");
        registerDockerRegistires();
    }

    /**
     * Used in the provision generate step to extract annotations from the test class.
     * 
     * @throws ManagerException
     */
    private void generateDockerFields() throws ManagerException {
        List<AnnotatedField> annotatedFields = findAnnotatedFields(DockerManagerField.class);

        for (AnnotatedField annotatedField: annotatedFields) {
            final Field field = annotatedField.getField();
            final List<Annotation> annotations = annotatedField.getAnnotations();

            if (field.getType() == IDockerManager.class) {
                registerAnnotatedField(field, this);
            } else if (field.getType() == IDockerServer.class) {
                DockerServer annotation = field.getAnnotation(DockerServer.class);
                if (annotation != null) {
                    IDockerServer dockerServer = generateDockerServer(field, annotations);
                    registerAnnotatedField(field, dockerServer);
                }
            } else if (field.getType() == IDockerContainer.class) {
                DockerContainer annotation = field.getAnnotation(DockerContainer.class);
                if (annotation != null) {
                    IDockerContainer dockerContainer = generateDockerContainer(field, annotations);
                    registerAnnotatedField(field, dockerContainer);
                }
            }
        }
        generateAnnotatedFields(DockerManagerField.class);
    }
    
    /**
     * Used in the provision generate step to register all the docker registries from the CPS, if non specified docker hub is registered.
     * 
     * @throws DockerProvisionException
     */
    private void registerDockerRegistires() throws DockerProvisionException {
        try {
            URL[] registryURLs = DockerRegistry.get();

            for (URL url: registryURLs) {
                registries.add(new DockerRegistryImpl(framework, this, url));
            }
        } catch (Exception e) {
            throw new DockerProvisionException("Unable to resolve docker registries: ", e);
        }
    }

    /**
     * Cleans and discards the docker environment, deleting docker containers not flagged to keep running.
     */
    @Override
    public void provisionStop() {
        try {
            dockerEnvironment.discard();
        } catch (DockerManagerException e) {
            logger.error("Unable to discard docker environment", e);
        }

    }

    /**
     * Gets any container running in the docker environment via the image tag
     * 
     * @throws DockerManagerException
     */
    public IDockerContainer getDockerContainer(String dockerContainerTag) throws DockerManagerException {
        return dockerEnvironment.getDockerContainerImpl(dockerContainerTag);
    }

    /**
     * Frees up any slot in use from a docker container.
     * 
     * @param slot
     * @throws DockerProvisionException
     */
    public void freeDockerSlot(DockerSlotImpl slot) throws DockerProvisionException {
        dockerEnvironment.freeDockerSlot(slot);
    }

    /**
     * Returns a list of all the registered registries used by the docker container.
     * 
     * @return registries
     */
	public List<DockerRegistryImpl> getRegistries() {
		return this.registries;
	}

}