/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.vtp.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.mq.internal.properties.VtpEnable;
import dev.galasa.mq.internal.properties.VtpPropertiesSingleton;
import dev.galasa.vtp.manager.VtpManagerException;

@Component(service = { IManager.class })
public class VtpManagerImpl extends AbstractManager {

	private static final Log logger = LogFactory.getLog(VtpManagerImpl.class);

	public final static String NAMESPACE = "vtp";

	private ICicstsManagerSpi cicsManager;
	private IConfigurationPropertyStoreService cps;
	private Path storedArtifactRoot;

	private HashMap<ICicsRegion,ICicsTerminal> recordingTerminals = new HashMap<>();

	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		List<AnnotatedField> foundAnnotatedFields = findAnnotatedFields(CicsRegion.class);
		for (AnnotatedField annotatedField : foundAnnotatedFields) {
			Field field = annotatedField.getField();
			if (field.getType() == ICicsRegion.class) {
				try {
					ICicsRegion region = (ICicsRegion) field.get(ICicsRegion.class.newInstance());
					ICicsTerminal terminal = cicsManager.generateCicsTerminal(region.getTag());
					recordingTerminals.put(region, terminal);
				} catch (Exception e) {
					throw new VtpManagerException("Unable to access CICS object for field: " + field.getName(), e);
				}

			}
		}
		if (recordingTerminals.size() == 0) {
			throw new VtpManagerException("VTP Manager is enabled but no recordable CICS regions found in the test");
		}
	}

	@Override
	public void provisionStart() throws ManagerException, ResourceUnavailableException {

	}

	@Override
	public void provisionDiscard() {

	}

	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);

		this.storedArtifactRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
		try {
			this.cps = getFramework().getConfigurationPropertyService(NAMESPACE);
			VtpPropertiesSingleton.setCps(this.cps);
		} catch (ConfigurationPropertyStoreException e1) {
			throw new VtpManagerException("Unable to access framework services", e1);
		}

		if (galasaTest.isJava()) {
			if (VtpEnable.get()) {
				youAreRequired(allManagers, activeManagers, galasaTest);
			}
		}
	}

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
			@NotNull GalasaTest galasaTest) throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
		cicsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
		if (cicsManager == null) {
			throw new VtpManagerException("The CICS Manager is not available");
		}
	}

	@Override
	public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
		if (otherManager instanceof ICicstsManagerSpi) {
			return true;
		}

		return super.areYouProvisionalDependentOn(otherManager);
	}

	@Override
	public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
		if(isTestMethod(galasaMethod)) {
			startRecording();
		}
	}

	@Override
	public String endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull String currentResult,
			Throwable currentException) throws ManagerException {
		if(isTestMethod(galasaMethod)) {
			stopRecording();
		}
		return null;
	}
	
	private boolean isTestMethod(GalasaMethod method) {
		if(!method.isJava()) {
			return false;
		}
		
		for(Annotation a : method.getJavaTestMethod().getAnnotations()) {
			if(a instanceof Test) {
				return true;
			}
		}
		return false;
	}
	
	private void startRecording() {
		startRecordingAllTerminals();
	}
	
	private void startRecordingAllTerminals() {
		for(ICicsRegion region : recordingTerminals.keySet()) {
			startRecording(recordingTerminals.get(region));
		}
	}
	
	private void startRecording(ICicsTerminal t) {
		
	}
	
	private void stopRecording() {
		stopRecordingAllTerminals();
	}
	
	private void stopRecordingAllTerminals() {
		for(ICicsRegion region : recordingTerminals.keySet()) {
			stopRecording(recordingTerminals.get(region));
		}
	}
	
	private void stopRecording(ICicsTerminal t) {
		
	}

}
