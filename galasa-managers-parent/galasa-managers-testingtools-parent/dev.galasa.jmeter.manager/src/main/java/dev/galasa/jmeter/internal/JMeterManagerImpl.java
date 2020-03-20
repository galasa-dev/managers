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
import org.osgi.service.component.annotations.Component;

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
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.jmeter.JMeterSession;
import dev.galasa.jmeter.internal.properties.JMeterPropertiesSingleton;

@Component(service = { IManager.class })
public class JMeterManagerImpl extends AbstractManager {

    protected final String NAMESPACE = "jmeter";

    private static final Log logger = LogFactory.getLog(JMeterManagerImpl.class);
    private List<IJMeterSession> activeSessions;

    private IFramework framework;
    private String jmxPath;
    private String propPath;

    private IDockerManagerSpi dockerManager;
    private List<IDockerContainer> activeContainers;


    private int sessionID = 0;


    @GenerateAnnotatedField(annotation = JMeterSession.class)
    public IJMeterSession generateJMeterSession(Field field, List<Annotation> annotations) throws JMeterManagerException {

        sessionID++;

        IDockerContainer container;

        JMeterSession sess = field.getAnnotation(JMeterSession.class);
        this.jmxPath = sess.jmxPath();
        this.propPath = sess.propPath();

        logger.info(this.jmxPath);
        logger.info(this.propPath);
        
        try {
            container = dockerManager.provisionContainer("jmeter", "egaillardon/jmeter:latest", false, "PRIMARY");
        } catch (DockerManagerException e) {
            throw new JMeterManagerException(String.format("Unable to provision the docker container for session %d", sessionID));
        }


        IJMeterSession session = new JMeterSessionImpl(framework, this, sessionID, this.jmxPath, this.propPath, container, logger);
        activeSessions.add(session);
        activeContainers.add(container);

        return session;
        
    }



    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        
        List<AnnotatedField> ourFields = findAnnotatedFields(JMeterManagerField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers);
        }

        
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

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);

        dockerManager = addDependentManager(allManagers, activeManagers, IDockerManagerSpi.class);
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(JMeterManagerField.class);
    }

    @Override
    public void shutdown() {
        for(IJMeterSession session: activeSessions) {
            try {
                session.stopTest(3000L);
            } catch (JMeterManagerException e) {
               
            }
        }
    }

}