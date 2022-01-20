/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.eclipseruntime.ubuntu.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.eclipseruntime.EclipseManagerException;
import dev.galasa.eclipseruntime.spi.IEclipseruntimeManagerSpi;
import dev.galasa.eclipseruntime.ubuntu.EclipseInstallUbuntu;
import dev.galasa.eclipseruntime.ubuntu.EclipseUbuntuManagerException;
import dev.galasa.eclipseruntime.ubuntu.EclipseUbuntuManagerField;
import dev.galasa.eclipseruntime.ubuntu.IEclipseInstallUbuntu;
import dev.galasa.eclipseruntime.ubuntu.spi.EclipseUbuntuInstallImpl;
import dev.galasa.eclipseruntime.ubuntu.spi.IEclipseruntimeUbuntuManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.java.spi.IJavaManagerSpi;
import dev.galasa.linux.spi.ILinuxManagerSpi;

@Component(service = {IManager.class})
public class EclipseUbuntuManagerImpl extends AbstractManager implements IEclipseruntimeUbuntuManagerSpi {
	public final static String NAMESPACE = "eclipseubuntu";
	private final static Log logger = LogFactory.getLog(EclipseUbuntuManagerImpl.class);
	private IEclipseruntimeManagerSpi eclipseManager;
	private ILinuxManagerSpi linuxManager;
	private IHttpManagerSpi httpManager;
	private EclipseUbuntuInstallImpl install;
	private Class<?> test;
	
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        
        if(galasaTest.isJava()) {
            // Check to see if our annotation is present in the test class
            List<AnnotatedField> ourFields = findAnnotatedFields(EclipseUbuntuManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
            this.test = galasaTest.getJavaTestClass();
        }
    }
    
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if (otherManager == this.linuxManager) {
            return true;
        }   
        return false;
    }
    
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
    	throws ManagerException {
    	if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        this.linuxManager = addDependentManager(allManagers, activeManagers, galasaTest, ILinuxManagerSpi.class);
        if (this.linuxManager == null) {
            throw new EclipseUbuntuManagerException("The Linux Manager is not available");
        }
        
        this.httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (this.httpManager == null) {
            throw new EclipseUbuntuManagerException("The HTTP Manager is not available");
        }
        
        this.eclipseManager = addDependentManager(allManagers, activeManagers, galasaTest, IEclipseruntimeManagerSpi.class);
        if (this.httpManager == null) {
            throw new EclipseUbuntuManagerException("The Eclipse Manager is not available");
        }
    }
    
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    	//Generates the remaining fields required.
        generateAnnotatedFields(EclipseUbuntuManagerField.class);
        //TODO this method needs to be finished.
    }
    
    @GenerateAnnotatedField(annotation = EclipseInstallUbuntu.class)
    public IEclipseInstallUbuntu createEclipseInstall(Field field, List<Annotation> annotations) throws EclipseUbuntuManagerException {
		//get the annotation from field.
    	EclipseInstallUbuntu annotation = field.getAnnotation(EclipseInstallUbuntu.class);
    	EclipseUbuntuInstallImpl installation;

		installation = new EclipseUbuntuInstallImpl(this, annotation.eclipseVersion(), annotation.eclipseType(), annotation.javaInstallationTag(), annotation.linuxImageTag());
		registerAnnotatedField(field, installation);
		this.install = installation; 
		
    	return installation;
    }
    
    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
    	install.build();
    }

    @Override
    public void provisionDiscard() {
    	install.discard();
    }
    
    public IEclipseruntimeManagerSpi getEclipseManager() {
    	return eclipseManager;
    }
    
    public ILinuxManagerSpi getLinuxManager() {
        return this.linuxManager;
    }
    
    public String getTestName() {
    	if (test == null) {
    		return "unknown";
    	}
    	return test.getName();
    }
    
}

