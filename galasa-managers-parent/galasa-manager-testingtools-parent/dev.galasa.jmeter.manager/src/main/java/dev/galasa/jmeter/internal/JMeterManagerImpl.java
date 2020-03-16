/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.jmeter.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.jmeter.IJMeterSession;

public class JMeterManagerImpl extends AbstractManager {

    protected final String          NAMESPACE = "jmeter";

    private static final Log        logger = LogFactory.getLog(JMeterManagerImpl.class);
    private List<IJMeterSession>    activeSessions = new ArrayList<IJMeterSession>();

    private IFramework              framework;
    // implemetation for running each session in a container
    // protected IDockerManagerSpi     dockerManager;




    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
    @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        // Testing: Is this manager actually used for the test when the whole framework launches?
        List<AnnotatedField> ourFields = findAnnotatedFields(JMeterManagerField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers);
        }
    }

    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
        throws ManagerException {
            if (activeManagers.contains(this)) {
                return;
            }

            activeManagers.add(this);
    }

    // public IJMeterSession startJMeterSession() {
    //     IJMeterSession session = new JMeterSession(logger);
    //     activeSessions.add(session);
    //     return session;
    // }



    
}