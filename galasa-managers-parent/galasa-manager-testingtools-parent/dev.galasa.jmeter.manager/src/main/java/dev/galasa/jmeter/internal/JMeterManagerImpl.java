/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.jmeter.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
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

    protected final String          NAMESPACE = "jmeter";

    private static final Log        logger = LogFactory.getLog(JMeterManagerImpl.class);
    private List<IJMeterSession>    activeSessions;

    private IFramework              framework;
    // implemetation for running each session in a container
    // protected IDockerManagerSpi     dockerManager;

    private boolean                 required = false;
    private int                     sessionID = 0;



    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
    @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        // Testing: Is this manager actually used for the test when the whole framework launches?
        List<AnnotatedField> ourFields = findAnnotatedFields(JMeterManagerField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers);
        }

        if (this.required) {
            try {
                JMeterPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
            }catch(ConfigurationPropertyStoreException e) {
                throw new JMeterManagerException("Failed to set the cps with the jmeter namespace");
            }

            this.framework = framework;
            this.activeSessions = new ArrayList<IJMeterSession>();
            
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
            // HERE, implement the DockerManagerSpi so it gets marked as a dependency and active
            //dockerManager = addDependentManager(allManagers, activeManagers, IDockerManagerSpi.class);
    }

    public IJMeterSession startJMeterSession() {
        Map<String, String> jmxProperties = new HashMap<>();
        sessionID++;
        IJMeterSession session = new JMeterSessionImpl(framework, this, jmxProperties, sessionID/*, dockerManager*/);
        activeSessions.add(session);

        return session;
    }

    public void stopJMeterSession(int sessionID, long timeout) {
        for(IJMeterSession session: activeSessions) {
            if(session.getSessionID() == sessionID) {
                session.stopTest(timeout);
            }
        }
    }



    
}