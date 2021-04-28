/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.galasaecosystem.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
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
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IKubernetesEcosystem;
import dev.galasa.galasaecosystem.KubernetesEcosystem;
import dev.galasa.galasaecosystem.internal.properties.GalasaEcosystemPropertiesSingleton;
import dev.galasa.galasaecosystem.internal.properties.KubernetesEcosystemTagSharedEnvironment;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.spi.IKubernetesManagerSpi;

/**
 * The Galasa Ecosystem Manager
 * 
 * @author Michael Baylis
 *
 */
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
    private HashSet<String> sharedEnvironmentEcosystemTags = new HashSet<>();

    /**
     * Initialise the Manager if the Ecosystem TPI is included in the test class
     */
    @Override
    public void initialise(@NotNull IFramework framework, 
            @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, 
            @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            //*** Check to see if we are needed
            if (!required) {
                for(Field field : galasaTest.getJavaTestClass().getFields()) {
                    if (field.getType() == IKubernetesEcosystem.class) {
                        required = true;
                        break;
                    }
                }
            }

            if (!required) {
                return;
            }
        }

        youAreRequired(allManagers, activeManagers, galasaTest);
        
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
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        super.youAreRequired(allManagers, activeManagers, galasaTest);

        if (activeManagers.contains(this)) {
            return;
        }

        //*** Add dependent managers
        this.k8sManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IKubernetesManagerSpi.class);
        this.artifactManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
        this.httpManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);

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
    
    @Override
    public boolean doYouSupportSharedEnvironments() {
        return true;
    }

    /**
     * Generate all the annotated fields, uses the standard generate by method mechanism
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        if (getFramework().getTestRun().isSharedEnvironment()) {
            logger.info("Manager running in Shared Environment setup");
        }

        //*** Shared Environment Discard processing
        try {
            if (getFramework().getSharedEnvironmentRunType() == SharedEnvironmentRunType.DISCARD) {
                KubernetesEcosystemImpl.loadEcosystemsFromRun(this, dss, taggedEcosystems, getFramework().getTestRun());
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new KubernetesManagerException("Unable to determine Shared Environment phase", e);
        }

        
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
        
        //*** Check to see if we already have it
        KubernetesEcosystemImpl ecosystem = this.taggedEcosystems.get(tag);
        if (ecosystem != null) {
            return ecosystem;
        }
        
        try {
            if (getFramework().getSharedEnvironmentRunType() == SharedEnvironmentRunType.DISCARD) {
                throw new GalasaEcosystemManagerException("Attempt to generate a new Ecosystem during Shared Environment discard");
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Unable to determine Shared Environment phase", e);
        }


        //*** check to see if the tag is a shared environment
        String sharedEnvironmentRunName = KubernetesEcosystemTagSharedEnvironment.get(tag);
        if (sharedEnvironmentRunName != null) {
            try {
                IRun sharedEnvironmentRun = getFramework().getFrameworkRuns().getRun(sharedEnvironmentRunName);
                
                if (sharedEnvironmentRun == null || !sharedEnvironmentRun.isSharedEnvironment()) {
                    throw new GalasaEcosystemManagerException("Unable to locate Shared Environment " + sharedEnvironmentRunName + " for Ecosystem Tag " + tag);
                }
                
                HashMap<String, KubernetesEcosystemImpl> tempSharedEnvironmentNamespaces = new HashMap<>();
                KubernetesEcosystemImpl.loadEcosystemsFromRun(this, dss, tempSharedEnvironmentNamespaces, sharedEnvironmentRun);
                
                ecosystem = tempSharedEnvironmentNamespaces.get(tag);
                if (ecosystem == null) {
                    throw new GalasaEcosystemManagerException("Unable to locate Shared Environment " + sharedEnvironmentRunName + " for Ecosystem Tag " + tag);
                }
                
                this.taggedEcosystems.put(tag, ecosystem);
                this.sharedEnvironmentEcosystemTags.add(tag);
                
                logger.info("Kubernetes Ecosystem tag " + tag + " is using Shared Environment " + sharedEnvironmentRunName);
                
                return ecosystem;
            } catch(FrameworkException e) {
                throw new GalasaEcosystemManagerException("Problem loading Shared Environment " + sharedEnvironmentRunName + " for Ecosystem Tag " + tag, e);
            }
        } 

        //*** Locate the Kubernetes Namespace object

        IKubernetesNamespace namespace = k8sManager.getNamespaceByTag(namespaceTag);
        if (namespace == null) {
            throw new GalasaEcosystemManagerException("Unable to locate the Kubernetes Namespace tagged " + namespaceTag);
        }

        ecosystem = new KubernetesEcosystemImpl(this, tag, namespace);
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
                if (this.sharedEnvironmentEcosystemTags.contains(ecosystem.getTag())) {
                    continue; //*** Do not build shared environments
                }
                ecosystem.build();
            } catch(Exception e) {
                logger.error("Problem building provisioned Ecosystem " + ecosystem.getTag(),e);

                throw new GalasaEcosystemManagerException("Problem building the Ecosystem", e);
            }
        }
    }

    @Override
    public void provisionStop() {
        for(KubernetesEcosystemImpl ecosystem : taggedEcosystems.values()) {
            if (this.sharedEnvironmentEcosystemTags.contains(ecosystem.getTag())) {
                continue;   //dont stop shared environments
            }
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
            if (this.sharedEnvironmentEcosystemTags.contains(ecosystem.getTag())) {
                continue;   //dont discard shared environments
            }
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

    public IDynamicStatusStoreService getDss() {
        return this.dss;
    }


}
