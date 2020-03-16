/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.jmeter.internal;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.jmeter.IJMeterSession;

import org.apache.jmeter.*;

 public class JMeterSessionImpl implements IJMeterSession {

    private final IFramework            framework;
    private final JMeterManagerImpl     jMeterManager;
    private final int                   sessionID;
    private final Map<String, String>   jmxProperties;
    // private final DockerManagerSpi      dockerManager;
    // private final IDockerContainer       container;

    private static final Log            logger = LogFactory.getLog(JMeterSessionImpl.class);
    
    public JMeterSessionImpl(IFramework framework, JMeterManagerImpl jMeterManager, Map<String, String> jmxProperties, int sessionID/*, DockerManagerSpi dockerManager*/) {
        this.framework      = framework;
        this.jMeterManager  = jMeterManager;
        this.sessionID      = sessionID;
        this.jmxProperties  = jmxProperties;
        // this.dockerManager  = dockerManager;

        logger.info(String.format("Session %d has been succesfully initialised", this.sessionID));
    }

    @Override
    public int getSessionID() {
        return this.sessionID;
    }
    
    @Override
     public void applyProperties(Map<String, String> properties) {
         // TODO Auto-generated method stub

     }

     @Override
     public void startJmeter() {
         // TODO Auto-generated method stub

     }

     @Override
     public void waitForJMeter() {
         // TODO Auto-generated method stub

     }

     @Override
     public void waitForJMeter(long timeout) {
         // TODO Auto-generated method stub

     }

     @Override
     public void setJmxFile(String path) {
         // TODO Auto-generated method stub

     }

     @Override
     public String getJmxFile() {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public String getLogFile(File logFile) {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public String getConsoleOutput() {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public String getListenerFile(String fileName) {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public void stopTest(long timeout) {
         // TODO Auto-generated method stub

     }

     @Override
     public long getExitCode() {
         // TODO Auto-generated method stub
         return 0;
     }

}