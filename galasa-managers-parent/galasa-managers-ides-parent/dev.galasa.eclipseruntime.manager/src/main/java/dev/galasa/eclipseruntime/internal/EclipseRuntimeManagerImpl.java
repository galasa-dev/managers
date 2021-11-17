/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.eclipseruntime.internal;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.IHttpManager;
import dev.galasa.http.spi.IHttpManagerSpi;

import java.nio.file.Path;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.eclipseruntime.IEclipseInstall;
import dev.galasa.eclipseruntime.manager.internal.properties.EclipsePropertiesSingleton;
import dev.galasa.eclipseruntime.spi.IEclipseruntimeManagerSpi;
import dev.galasa.eclipseruntime.EclipseManagerException;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.java.internal.JavaManagerImpl;
import dev.galasa.java.spi.IJavaManagerSpi;
import dev.galasa.linux.spi.ILinuxManagerSpi;

@Component(service = {IManager.class})
public class EclipseRuntimeManagerImpl extends AbstractManager implements IEclipseruntimeManagerSpi {
	protected final String NAMESPACE = "eclipseruntime";	
	//private final static Log logger = LogFactory.getLog(EclipseRuntimeManagerImpl.class); Possibly Needed later on when Impl is written.
	private IJavaManagerSpi javaManager;
	private IHttpManagerSpi httpManager;
	private ILinuxManagerSpi linuxManager;

	
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
        	EclipsePropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
        	throw new EclipseManagerException("Failed to set configuration property service");
        }
	}
	
	public IJavaManagerSpi getJavaManager() {
		return this.javaManager;
	}
	
	 public IHttpManagerSpi getHttpManager() {
	        return this.httpManager;
	}
	
	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
		if(activeManagers.contains(this)) {
			return;
		}
		activeManagers.add(this);
		
		this.javaManager = addDependentManager(allManagers, activeManagers, galasaTest, IJavaManagerSpi.class);
		if (this.javaManager == null){
			throw new EclipseManagerException("The Java Manager is Unavailable");
		}
		
		this.httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
		if (this.httpManager == null){
			throw new EclipseManagerException("The Http Manager is Unavailable");
		}
		
		this.linuxManager = addDependentManager(allManagers, activeManagers, galasaTest, ILinuxManagerSpi.class);
    }	
	
	@Override
	public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager)
	{
		if(otherManager instanceof IHttpManager | otherManager instanceof IJavaInstallation){
			return true;
		}
		return false;
	}

	@Override
	public @NotNull Path getEclipseInstallLocation() {
		// TODO Auto-generated method stub
		return null;
	}
}

