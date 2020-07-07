/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.core.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ICredentials;
import dev.galasa.ManagerException;
import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.RunName;
import dev.galasa.core.manager.StoredArtifactRoot;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.AbstractGherkinManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IGherkinManager;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class, IGherkinManager.class })
public class CoreManager extends AbstractGherkinManager implements ICoreManager {

	private IConfigurationPropertyStoreService cpsTest;
	private IConfidentialTextService           ctf;

	private final static Log logger = LogFactory.getLog(CoreManager.class);

	private final static Pattern patternStoreVariable = Pattern.compile("<(\\w+)> is test property ([\\w.]+)");
	private final static Pattern patternLog = Pattern.compile("Write to log \"([\\w\\W]+)\"");

	private final static Pattern[] gherkinPatterns = { patternStoreVariable, patternLog };

	/*
	 * (non-Javadoc)
	 * 
	 * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.
	 * IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);

		try {
			this.cpsTest = framework.getConfigurationPropertyService("test");
		} catch (ConfigurationPropertyStoreException e) {
			throw new CoreManagerException("Unable to initialise the CPS for Core Manager",e);
		}
		this.ctf = framework.getConfidentialTextService();

		if(galasaTest.isGherkin()) {
			registerGherkinStatements(galasaTest);
		}

		// *** We always want the Core Manager initialised and included in the Test Run
		activeManagers.add(this);
	}

	private void registerGherkinStatements(GalasaTest galasaTest) throws ManagerException {
		for(IGherkinExecutable gherkinExecutable : galasaTest.getGherkinTest().getAllExecutables()) {
			for(Pattern regexPattern : gherkinPatterns) {
				Matcher gherkinMatcher = regexPattern.matcher(gherkinExecutable.getValue());
				if(gherkinMatcher.matches()) {
					try {
						gherkinExecutable.registerManager((IGherkinManager) this);
					} catch (TestRunException e) {
						throw new ManagerException("Unable to register Manager for Gherkin Statement", e);
					}
				}
			}
		}
	}
	
	@Override
	public void executeGherkin(@NotNull IGherkinExecutable executable, Map<String, Object> testVariables)
			throws ManagerException {
		switch(executable.getKeyword()) {
			case GIVEN:
				Matcher matcherStoreVariable = patternStoreVariable.matcher(executable.getValue());
				if(matcherStoreVariable.matches()) {
					CoreGherkinExecution.storeVariable(matcherStoreVariable, cpsTest, testVariables);
					return;
				}
				break;
				
			case THEN:
				Matcher matcherLog = patternLog.matcher(executable.getValue());
				if(matcherLog.matches()) {
					CoreGherkinExecution.log(matcherLog, testVariables, logger);
					return;
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		generateAnnotatedFields(CoreManagerField.class);
	}

	/**
	 * Generates a Log instance for the Test Class
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = Logger.class)
	public Log createLogField(Field field, List<Annotation> annotations) {
		return LogFactory.getLog(getTestClass());
	}

	/**
	 * Generates a ICoreManager instance for the Test Class
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = dev.galasa.core.manager.CoreManager.class)
	public ICoreManager createICoreManager(Field field, List<Annotation> annotations) {
		return this;
	}

	/**
	 * Generates a test Property
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 * @throws ConfigurationPropertyStoreException 
	 * @throws CoreManagerException 
	 */
	@GenerateAnnotatedField(annotation = TestProperty.class)
	public String createTestproperty(Field field, List<Annotation> annotations) throws ConfigurationPropertyStoreException, CoreManagerException {

		TestProperty testPropertyAnnotation = field.getAnnotation(TestProperty.class);

		String value = nulled(this.cpsTest.getProperty(testPropertyAnnotation.prefix(), 
				testPropertyAnnotation.suffix(), 
				testPropertyAnnotation.infixes()));

		if (testPropertyAnnotation.required() && value == null) { 
			throw new CoreManagerException("Test Property missing for prefix=" 
					+ testPropertyAnnotation.prefix() 
					+ ",suffix=" 
					+ testPropertyAnnotation.suffix()
					+ ",infixes=" 
					+ testPropertyAnnotation.infixes());
		}

		return value;
	}

	/**
	 * Generates a Stored Artifact Root Path instance for the Test Class
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = StoredArtifactRoot.class)
	public Path createrootPath(Field field, List<Annotation> annotations) {
		return getFramework().getResultArchiveStore().getStoredArtifactsRoot();
	}

	/**
	 * Generates a Run Name String instance for the Test Class
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = RunName.class)
	public String createRunName(Field field, List<Annotation> annotations) {
		return getRunName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dev.galasa.core.manager.ICoreManager#getRunName()
	 */
	@Override
	public @NotNull String getRunName() {
		return getFramework().getTestRunName();
	}

	@Override
	public ICredentials getCredentials(@NotNull String credentialsId) throws CoreManagerException {
		try {
			return getFramework().getCredentialsService().getCredentials(credentialsId);
		} catch (CredentialsException e) {
			throw new CoreManagerException("Unable to retrieve credentials for id " + credentialsId, e);
		}
	}

	@Override
	public void registerConfidentialText(String confidentialString, String comment) {
		ctf.registerText(confidentialString, comment);
	}

    @Override
    public boolean doYouSupportSharedEnvironments() {
        return true;   // this manager does not provision resources, therefore support environments 
    }

}
