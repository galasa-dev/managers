/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.vtp.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.CicstsManagerField;
import dev.galasa.cicsts.ICeciResponse;
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
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.IZos3270ManagerSpi;
import dev.galasa.zos3270.spi.NetworkException;

@Component(service = { IManager.class })
public class VtpManagerImpl extends AbstractManager {

	private static final Log logger = LogFactory.getLog(VtpManagerImpl.class);

	public final static String NAMESPACE = "vtp";

	private ICicstsManagerSpi cicsManager;
	private IConfigurationPropertyStoreService cps;
	private Path storedArtifactRoot;

	private HashMap<ICicsRegion,ICicsTerminal> recordingTerminals = new HashMap<>();
	private HashMap<ICicsRegion,char[]> recordingTokens = new HashMap<>();
	
	private static final String VtpApiCommandStart = "LINK PROG(BZUCIDRP) COM(";
	private static final String VtpApiCommandEnd   = ")";
	
	private static final String VtpApiVersionData         = "VERS              ";
	private static final String VtpApiStartRecordingData  = "STRT                    ";
	
	private static final String VtpVersionName  = "&VTPVERS";
	private static final String VtpStartName  = "&VTPSTRT";
	private static final String VtpStopName  = "&VTPSTOP";
	
	private static final int    VtpTokenIndexStart = 16;
	private static final int    VtpTokenIndexEnd   = 24;
	
	private static final String PASSED_RESULT = "Passed";
	
	private boolean skipRecordings = false;

	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		List<AnnotatedField> foundAnnotatedFields = findAnnotatedFields(CicstsManagerField.class);
		for (AnnotatedField annotatedField : foundAnnotatedFields) {
			Field field = annotatedField.getField();
			if (field.getType() == ICicsRegion.class) {
				CicsRegion annotation = field.getAnnotation(CicsRegion.class);
				String tag = annotation.cicsTag();
				ICicsTerminal terminal = cicsManager.generateCicsTerminal(tag);
				ICicsRegion region = cicsManager.locateCicsRegion(tag);
				recordingTerminals.put(region, terminal);
			}
		}
		if (recordingTerminals.size() == 0) {
			logger.info("VTP Recording enabled but test class contains no CICS TS fields - recording will not be attempted");
			skipRecordings = true;
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
		logger.info("In initialise method of VTP Manager");
		this.storedArtifactRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
		try {
			this.cps = getFramework().getConfigurationPropertyService(NAMESPACE);
			VtpPropertiesSingleton.setCps(this.cps);
		} catch (ConfigurationPropertyStoreException e1) {
			throw new VtpManagerException("Unable to access framework services", e1);
		}

		if (galasaTest.isJava()) {
			//If VTP recording is enabled and there are recordable CICS regions then enable the manager 
			if (VtpEnable.get() && findAnnotatedFields(CicstsManagerField.class).size() > 0) {
				if(findAnnotatedFields(CicstsManagerField.class).size() > 0) {
					youAreRequired(allManagers, activeManagers, galasaTest);
				}else {
					logger.info("VTP Recording enabled but test class contains no CICS TS fields - recording will not be attempted");
				}
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
		if (otherManager instanceof ICicstsManagerSpi || otherManager instanceof IZos3270ManagerSpi) {
			return true;
		}

		return super.areYouProvisionalDependentOn(otherManager);
	}

	@Override
	public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
		if(skipRecordings) {
			return;
		}
		if(isTestMethod(galasaMethod)) {
			startRecording();
		}
	}

	@Override
	public String endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull String currentResult,
			Throwable currentException) throws ManagerException {
		if(skipRecordings) {
			return null;
		}
		if(isTestMethod(galasaMethod)) {
			stopRecording();
		}
		
		if(PASSED_RESULT.equalsIgnoreCase(currentResult)) {
			
		}
		return null;
	}
	
	private boolean isTestMethod(GalasaMethod method) {
		if(!method.isJava()) {
			return false;
		}
		for(Annotation a : method.getJavaExecutionMethod().getAnnotations()) {
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
			try {
				startRecording(region);
			} catch (VtpManagerException e) {
				logger.error("Unable to start recording for region tagged: " + region.getTag());
			}
		}
	}
	
	private void startRecording(ICicsRegion region) throws VtpManagerException {
		ICicsTerminal terminal = recordingTerminals.get(region);
		try {
			logger.info("Starting VTP Recording");
			region.ceci().defineVariableText(terminal, VtpStartName, VtpApiStartRecordingData);
			ICeciResponse response = region.ceci().issueCommand(terminal, VtpApiCommandStart + VtpStartName + VtpApiCommandEnd);
			if(!response.isNormal()) {
				throw new VtpManagerException("Non normal response from CECI while starting recording, EIBRESP: " + response.getEIBRESP() + " EIBRESP2:" + response.getEIBRESP2());
			}
			
			String responseData = region.ceci().retrieveVariableText(terminal, VtpStartName);
			char[] binaryResponse = region.ceci().retrieveVariableBinary(terminal, VtpStartName);
			char[] recordingToken = Arrays.copyOfRange(binaryResponse, VtpTokenIndexStart, VtpTokenIndexEnd);
			this.recordingTokens.remove(region);
			this.recordingTokens.put(region, recordingToken);
		} catch (CicstsManagerException e) {
			throw new VtpManagerException("Unable to start VTP recording",e);
		} 
	}
	
	private void stopRecording() {
		stopRecordingAllTerminals();
	}
	
	private void stopRecordingAllTerminals() {
		for(ICicsRegion region : recordingTerminals.keySet()) {
			try {
				stopRecording(region);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording");
			}
		}
	}
	
	private void stopRecording(ICicsRegion region) throws VtpManagerException {
		ICicsTerminal terminal = recordingTerminals.get(region);
		try {
			logger.info("Stopping VTP Recording");
			//obtain recording token
			char[] recordingToken = recordingTokens.get(region);
			if(recordingToken == null) {
				logger.error("No recording token saved for region tagged: " + region.getTag() + ". Not attempting to stop recording");
				return;
			}
			
			//fill in the API data 
			char[] recordingData  = new char[24];
			char blank = 0;
			Arrays.fill(recordingData, blank);
			//first four characters are STOP
			System.arraycopy(new char[]{226,227,214,215}, 0, recordingData, 0, 4);
			//put the recording token at the end
			System.arraycopy(recordingToken, 0, recordingData, 16, 8);
			
			region.ceci().defineVariableBinary(terminal, VtpStopName, recordingData);
			ICeciResponse response = region.ceci().issueCommand(terminal, VtpApiCommandStart + VtpStopName + VtpApiCommandEnd);
			
			if(!response.isNormal()) {
				throw new VtpManagerException("Non normal response from CECI while stopping recording, EIBRESP: " + response.getEIBRESP() + " EIBRESP2:" + response.getEIBRESP2());
			}
			String responseData = region.ceci().retrieveVariableText(terminal, VtpStopName);
		} catch (CicstsManagerException e) {
			throw new VtpManagerException("Unable to start VTP recording",e);
		} 
	}
	
	private void exportRecording(ICicsRegion region) {
		
	}
	
	private void getVTPVersion() throws VtpManagerException {
		ICicsRegion region = recordingTerminals.keySet().iterator().next();
		ICicsTerminal terminal = recordingTerminals.get(region);
		try {
			logger.info("Checking VTP API Version");
			region.ceci().defineVariableText(terminal, VtpVersionName, VtpApiVersionData);
			ICeciResponse response = region.ceci().issueCommand(terminal, VtpApiCommandStart + VtpVersionName + VtpApiCommandEnd);
			if(!response.isNormal()) {
				throw new VtpManagerException("Non normal response from CECI while inquiring VTP API Version, EIBRESP: " + response.getEIBRESP() + " EIBRESP2:" + response.getEIBRESP2());
			}
			String responseData = region.ceci().retrieveVariableText(terminal, VtpVersionName);
			String vtpVersion = responseData.substring(responseData.length()-2);
			logger.info("VTP returned version: " + vtpVersion);
		} catch (CicstsManagerException e) {
			throw new VtpManagerException("Unable to obtain VTP Version",e);
		} 
		
	}

	/**
	 * Before the test starts, ensure that the recording terminal
	 * is connected and moved to the CECI screen.
	 * Then obtain the VTP API version
	 */
	@Override
	public void startOfTestClass() throws ManagerException {
		
		for(ICicsRegion region : recordingTerminals.keySet()) {
			ICicsTerminal terminal = recordingTerminals.get(region);
			try {
				if(!terminal.isConnected()) {
					terminal.connect();
				}
				terminal.type("CECI").enter().wfk();
			} catch (TimeoutException | KeyboardLockedException | TerminalInterruptedException | NetworkException
					| FieldNotFoundException e) {
				// TODO Auto-generated catch block
				throw new VtpManagerException("Unable to initiate CECI on a recording terminal",e);
			}
		}
		getVTPVersion();
	}
}