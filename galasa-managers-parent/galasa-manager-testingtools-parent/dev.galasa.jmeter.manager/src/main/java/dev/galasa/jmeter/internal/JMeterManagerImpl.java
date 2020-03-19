/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.jmeter.internal;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterSession;
import dev.galasa.jmeter.internal.properties.JMeterPropertiesSingleton;

public class JMeterManagerImpl extends AbstractManager {

    protected final String NAMESPACE = "jmeter";

    private static final Log logger = LogFactory.getLog(JMeterManagerImpl.class);
    private List<IJMeterSession> activeSessions;

    private IFramework framework;
    private String jmxPath;
    private String propPath;

    private IDockerManagerSpi dockerManager;
    private List<IDockerContainer> activeContainers;

    private boolean required = false;
    private int sessionID = 0;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        
        List<AnnotatedField> ourFields = findAnnotatedFields(JMeterManagerField.class);
        if (!ourFields.isEmpty()) {
            if (ourFields.get(0).toString() != null) {
                this.jmxPath = ourFields.get(0).toString();
            }
            if (ourFields.get(1).toString() != null) {
                this.propPath = ourFields.get(0).toString();
            }

            youAreRequired(allManagers, activeManagers);
        }

        if (this.required) {
            try {
                JMeterPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
            } catch (ConfigurationPropertyStoreException e) {
                throw new JMeterManagerException("Failed to set the cps with the jmeter namespace");
            }

            this.framework = framework;
            this.activeSessions = new ArrayList<IJMeterSession>();
            this.activeContainers = new ArrayList<IDockerContainer>();

            logger.info("JMeter manager has been succesfully initialised.");

        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);

        dockerManager = addDependentManager(allManagers, activeManagers, IDockerManagerSpi.class);
    }

    public IJMeterSession generateJMeterSession() throws JMeterManagerException {

        
        sessionID;

        IDockerContainer container;
        
        try {
            container = dockerManager.getDockerContainer("egaillardon/jmeter");
        } catch (DockerManagerException e) {
            throw new JMeterManagerException(String.format("Unable to provision the docker container for session %d", sessionID));
        }


        IJMeterSession session = new JMeterSessionImpl(framework, this, sessionID, this.jmxPath, this.propPath, container);
        activeSessions.add(session);
        activeContainers.add(container);

        return session;
    }


    public void stopJMeterSession(int sessionID, long timeout) throws JMeterManagerException {
        for(IJMeterSession session: activeSessions) {
            if(session.getSessionID() == sessionID) {
                session.stopTest(30000L);
            }
        }
    }

    
}