/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.selenium.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.docker.IDockerManager;
import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.kubernetes.spi.IKubernetesManagerSpi;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.SeleniumManager;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.SeleniumManagerField;
import dev.galasa.selenium.internal.properties.SeleniumPropertiesSingleton;
import dev.galasa.selenium.internal.properties.SeleniumScreenshotFailure;

@Component(service = { IManager.class })
public class SeleniumManagerImpl extends AbstractManager {
	
	private static final Log logger = LogFactory.getLog(SeleniumManagerImpl.class);

    public static final String NAMESPACE = "selenium";

    private IConfigurationPropertyStoreService cps; // NOSONAR
    private IDynamicStatusStoreService dss;
    private Path screenshotRasDirectory;
    
    private IFramework framework;

    private IDockerManagerSpi dockerManager;
    private IHttpManagerSpi httpManager;
    private IKubernetesManagerSpi k8Manager;
    private IArtifactManager artifactManager;
    
    private SeleniumEnvironment seleniumEnvironment;

    private boolean required = false;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        this.framework = framework;

        if (galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(SeleniumManagerField.class);
            if (!ourFields.isEmpty() || this.required) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }

        try {
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            SeleniumPropertiesSingleton.setCps(cps);
        } catch (Exception e) {
            throw new SeleniumManagerException("Unable to request framework services", e);
        }
        
        logger.info("Selenium manager has been succesfully initialised.");
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
		if (otherManager instanceof IDockerManager) {
	        return true;
	    }
		if (otherManager instanceof IKubernetesManagerSpi) {
	        return true;
	    }

        return super.areYouProvisionalDependentOn(otherManager);
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        this.required = true;

        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
    	this.k8Manager = this.addDependentManager(allManagers, activeManagers, galasaTest, IKubernetesManagerSpi.class);
    	if (this.k8Manager == null) {
            throw new SeleniumManagerException("Unable to locate the Kubernetes Manager");
        }
		this.dockerManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IDockerManagerSpi.class);
		if (this.dockerManager == null) {
			throw new SeleniumManagerException("Unable to locate the Docker Manager");
	    }
        this.httpManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (this.httpManager == null) {
            throw new SeleniumManagerException("Unable to locate the Http Manager");
        }
        this.artifactManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);        
        if (this.artifactManager == null) {
            throw new SeleniumManagerException("Unable to locate the Artifact Manager");
        }
        
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        Path storedArtifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        screenshotRasDirectory = storedArtifactsRoot.resolve("selenium").resolve("screenshots");
        this.seleniumEnvironment = new SeleniumEnvironment(this, screenshotRasDirectory);

        generateAnnotatedFields(SeleniumManagerField.class);
    }
    
    @Override
    public void provisionDiscard() {
    	try {
			seleniumEnvironment.discard();
		} catch (SeleniumManagerException e) {
			logger.error("Failed to discard seleniumEnvironment", e);
		}
    }

    @GenerateAnnotatedField(annotation = SeleniumManager.class)
    public ISeleniumManager generateSeleniumManager(Field field, List<Annotation> annotations) throws ResourceUnavailableException, SeleniumManagerException {
    	SeleniumManager annoation = field.getAnnotation(SeleniumManager.class);
        Browser browser = annoation.browser();
       return this.seleniumEnvironment.allocateDriver(browser);
    }

    @Override
    public String endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull String currentResult,
            Throwable currentException) throws ManagerException {
        try {
            if (!currentResult.equals("Passed")) {
                if (SeleniumScreenshotFailure.get()) {
                	seleniumEnvironment.screenShotPages();
                }
            }
        } catch (ConfigurationPropertyStoreException e) {

        }
        return null;
    }
    
    
    public IFramework getFramework() {
    	return this.framework;
    }
    public IConfigurationPropertyStoreService getCps() {
    	return this.cps;
    }
    public IDynamicStatusStoreService getDss() {
    	return this.dss;
    }
    public IDockerManagerSpi getDockerManager() {
    	return this.dockerManager;
    }
    public IKubernetesManagerSpi getKubernetesManager() {
    	return this.k8Manager;
    }
    public IArtifactManager getArtifactManager() {
    	return this.artifactManager;
    }
    public IHttpManagerSpi getHttpManager() {
    	return this.httpManager;
    }
}