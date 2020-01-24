package dev.galasa.galasaecosystem.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IKubernetesEcosystem;
import dev.galasa.galasaecosystem.KubernetesEcosystem;
import dev.galasa.galasaecosystem.internal.properties.GalasaEcosystemPropertiesSingleton;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.spi.IKubernetesManagerSpi;

@Component(service = { IManager.class })
public class GalasaEcosystemManagerImpl extends AbstractManager {

    protected final static String               NAMESPACE = "galasaecosystem";
    private final Log                           logger = LogFactory.getLog(getClass());
    private IDynamicStatusStoreService          dss;
    private final Gson                          gson = new Gson();

    private IArtifactManager                    artifactManager;
    private IHttpManagerSpi                     httpManager;
    private IKubernetesManagerSpi               k8sManager;

    private boolean                             required = false;

    private HashMap<String, KubernetesEcosystemImpl> taggedEcosystems = new HashMap<>();

    /**
     * Initialise the Manager if the Ecosystem TPI is included in the test class
     */
    @Override
    public void initialise(@NotNull IFramework framework, 
            @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, 
            @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        //*** Check to see if we are needed
        if (!required) {
            for(Field field : testClass.getFields()) {
                if (field.getType() == IKubernetesEcosystem.class) {
                    required = true;
                    break;
                }
            }
        }

        if (!required) {
            return;
        }

        youAreRequired(allManagers, activeManagers);
        
        try {
            GalasaEcosystemPropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Failed to set the CPS with the Galasa Ecosystem namespace", e);
        }

        try {
            this.dss = this.getFramework().getDynamicStatusStoreService(NAMESPACE);
        } catch(DynamicStatusStoreException e) {
            throw new GalasaEcosystemManagerException("Unable to provide the DSS for the Galasa Ecosystem Manager", e);
        }

        this.logger.info("Galasa Ecosystem Manager initialised");
    }

    /**
     * This or another manager has indicated that this manager is required
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        super.youAreRequired(allManagers, activeManagers);

        if (activeManagers.contains(this)) {
            return;
        }

        //*** Add dependent managers
        this.k8sManager = this.addDependentManager(allManagers, activeManagers, IKubernetesManagerSpi.class);
        this.artifactManager = this.addDependentManager(allManagers, activeManagers, IArtifactManager.class);
        this.httpManager = this.addDependentManager(allManagers, activeManagers, IHttpManagerSpi.class);

        if (this.k8sManager == null) {
            throw new GalasaEcosystemManagerException("Unable to locate the Kubernetes Manager");
        }
        if (this.artifactManager == null) {
            throw new GalasaEcosystemManagerException("Unable to locate the Artifact Manager");
        }
        if (this.httpManager == null) {
            throw new GalasaEcosystemManagerException("Unable to locate the Http Manager");
        }

        activeManagers.add(this);
        this.required = true;
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if (otherManager == k8sManager) {
            return true;
        }

        return super.areYouProvisionalDependentOn(otherManager);
    }

    /**
     * Generate all the annotated fields, uses the standard generate by method mechanism
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(GalasaEcosystemManagerField.class);
    }

    /**
     * Generate a Galasa Ecosystem
     * 
     * @param field The test field
     * @param annotations any annotations with the ecosystem
     * @return a {@link IKubernetesEcosystem} ecosystem
     * @throws KubernetesManagerException if there is a problem generating a ecosystem
     */
    @GenerateAnnotatedField(annotation = KubernetesEcosystem.class)
    public IKubernetesEcosystem generateKubernetesEcosystem(Field field, List<Annotation> annotations) throws GalasaEcosystemManagerException {
        KubernetesEcosystem annotation = field.getAnnotation(KubernetesEcosystem.class);

        String tag = annotation.ecosystemNamespaceTag().trim().toUpperCase();
        if (tag.isEmpty()) {
            tag = "PRIMARY";
        }

        String namespaceTag = annotation.kubernetesNamespaceTag().trim().toUpperCase();
        if (namespaceTag.isEmpty()) {
            namespaceTag = "PRIMARY";
        }

        //*** Locate the Kubernetes Namespace object

        IKubernetesNamespace namespace = k8sManager.getNamespaceByTag(namespaceTag);
        if (namespace == null) {
            throw new GalasaEcosystemManagerException("Unable to locate the Kubernetes Namespace tagged " + namespaceTag);
        }

        KubernetesEcosystemImpl ecosystem = new KubernetesEcosystemImpl(this, tag, namespace);
        taggedEcosystems.put(tag, ecosystem);

        try {
            ecosystem.loadYamlResources();
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Unable to provision Kubernetes Ecosystem " + tag, e);
        }

        logger.info("Allocated Galasa Kubernetes Ecosystem on Kubernetes Namespace " + namespace.getFullId() + " for tag " + tag);

        return ecosystem;
    }

    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        super.provisionBuild();
        
        for(KubernetesEcosystemImpl ecosystem : taggedEcosystems.values()) {
            try {
                ecosystem.build();
            } catch(Exception e) {
                logger.error("Problem building provisioned Ecosystem " + ecosystem.getTag(),e);
            }
        }
    }

    @Override
    public void provisionStop() {
        for(KubernetesEcosystemImpl ecosystem : taggedEcosystems.values()) {
            try {
                ecosystem.stop();
            } catch(Exception e) {
                logger.error("Problem stopping provisioned Ecosystem " + ecosystem.getTag(),e);
            }
        }

        super.provisionStop();
    }

    @Override
    public void provisionDiscard() {
        for(KubernetesEcosystemImpl ecosystem : taggedEcosystems.values()) {
            try {
                ecosystem.discard();
            } catch(Exception e) {
                logger.error("Problem discarding provisioned Ecosystem " + ecosystem.getTag(),e);
            }
        }

        super.provisionDiscard();
    }

    protected IArtifactManager getArtifactManager() {
        return this.artifactManager;
    }

    protected IHttpManagerSpi getHttpManager() {
        return this.httpManager;
    }

    protected IKubernetesManagerSpi getKubernetesManager() {
        return this.k8sManager;
    }

    public Gson getGson() {
        return this.gson;
    }


}
