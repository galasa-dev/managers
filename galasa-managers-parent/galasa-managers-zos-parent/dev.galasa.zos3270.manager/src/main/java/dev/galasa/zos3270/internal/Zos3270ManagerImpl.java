/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.IZosManager;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.Zos3270Terminal;
import dev.galasa.zos3270.internal.properties.Zos3270PropertiesSingleton;
import dev.galasa.zos3270.spi.IZos3270ManagerSpi;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Terminal;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

@Component(service = { IManager.class })
public class Zos3270ManagerImpl extends AbstractManager implements IZos3270ManagerSpi {
	protected final static String NAMESPACE = "zos3270";

	private final static Log logger = LogFactory.getLog(Zos3270ManagerImpl.class);

	private IConfigurationPropertyStoreService cps;
	private IDynamicStatusStoreService dss;

	private IZosManagerSpi zosManager;
	
	private ArrayList<Zos3270TerminalImpl> terminals = new ArrayList<>();
	
	private int terminalCount = 0;

	/* (non-Javadoc)
	 * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);

		//*** Check to see if any of our annotations are present in the test class
		//*** If there is,  we need to activate
		List<AnnotatedField> ourFields = findAnnotatedFields(Zos3270ManagerField.class);
		if (!ourFields.isEmpty()) {
			youAreRequired(allManagers, activeManagers);
		}

		try {
			this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
			Zos3270PropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
		} catch (Exception e) {
			throw new Zos3270ManagerException("Unable to request framework services", e);
		}

	}

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
			throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
		zosManager = addDependentManager(allManagers, activeManagers, IZosManagerSpi.class);
		if (zosManager == null) {
			throw new Zos3270ManagerException("The zOS Manager is not available");
		}
	}

	@Override
	public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
		if (otherManager instanceof IZosManager) {
			return true;
		}

		return super.areYouProvisionalDependentOn(otherManager);
	}


	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		//*** Auto generate the fields
		generateAnnotatedFields(Zos3270ManagerField.class);
	}

	@GenerateAnnotatedField(annotation=Zos3270Terminal.class)
	public ITerminal generateTerminal(Field field, List<Annotation> annotations) throws Zos3270ManagerException {
		Zos3270Terminal terminalAnnotation = field.getAnnotation(Zos3270Terminal.class);

		//*** Default the tag to primary
		String tag = defaultString(terminalAnnotation.imageTag(), "primary");

		//*** Ask the zosManager for the image for the Tag
		try {
			IZosImage image = this.zosManager.getImageForTag(tag);
			IIpHost host = image.getIpHost();
			
			String terminaId = "term" + (terminalCount++);
			
			Zos3270TerminalImpl terminal = new Zos3270TerminalImpl(terminaId, host.getHostname(), host.getTelnetPort(), host.isTelnetPortTls(), getFramework());
			
			this.terminals.add(terminal);
			logger.info("Generated a terminal for zOS Image tagged " + tag);
			
			return terminal;
		} catch(Exception e) {
			throw new Zos3270ManagerException("Unable to generate Terminal for zOS Image tagged " + tag, e);
		}
	}

	@Override
	public void provisionBuild() throws ManagerException, ResourceUnavailableException {
		super.provisionBuild();
	}
	
	@Override
	public void provisionStart() throws ManagerException, ResourceUnavailableException {
		if (terminals.isEmpty()) {
			return;
		}
		
		logger.info("Connecting zOS3270 Terminals");
		for(Zos3270TerminalImpl terminal : terminals) {
			try {
				terminal.connect();
				logger.trace("Connected zOS 3270 Terminal " + terminal.getId());
			} catch (NetworkException e) {
				logger.info("Failed to connect zOS 3270 Terminal to " + terminal.getHostPort() ,e);
			}
		}
	}
	
	@Override
	public void provisionStop() {
		logger.trace("Disconnecting terminals");
		for(Terminal terminal : terminals) {
			terminal.disconnect();
		}
	}

	protected IConfigurationPropertyStoreService getCps() {
		return this.cps;
	}

	protected IDynamicStatusStoreService getDss() {
		return this.dss;
	}
}
