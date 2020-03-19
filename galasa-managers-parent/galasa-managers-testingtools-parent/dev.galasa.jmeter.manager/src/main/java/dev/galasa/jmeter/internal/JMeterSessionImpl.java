/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.jmeter.internal;


import java.io.InputStream;

import org.apache.commons.logging.Log;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;

public class JMeterSessionImpl implements IJMeterSession {

    private final IFramework framework;
    private final JMeterManagerImpl jMeterManager;
    private final int sessionID;
    private String jmxPath;
    private String propPath;
    private IDockerContainer container;
    private final long DEFAULT_TIMER = 30000L;

    private Log logger;

    public JMeterSessionImpl(IFramework framework, JMeterManagerImpl jMeterManager,
            int sessionID, String jmxPath, String propPath, IDockerContainer container, Log logger) {
        this.framework = framework;
        this.jMeterManager = jMeterManager;
        this.sessionID = sessionID;
        this.container = container;
        this.jmxPath = jmxPath;
        this.propPath = propPath;
        this.logger = logger;

        logger.info(String.format("Session %d have been succesfully initialised", this.sessionID));
    }

    @Override
    public int getSessionID() {
        return this.sessionID;
    }

    @Override
    public void applyProperties(InputStream propStream) throws JMeterManagerException {
        try {
            container.storeFile("/prop/" + propPath, propStream);
        } catch (Exception e) {
            throw new JMeterManagerException("Could not store the .jmx file correctly.",e);
        }
    }

    @Override
    public void startJmeter() throws JMeterManagerException {
        
        try {
            if (!container.isRunning()) {
                
                String jtlPath = "/jmx/" + jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".jtl";
                String prop = "/prop/" + propPath;
                
                if ((jmxPath.toLowerCase().endsWith(".jmx")) && container.isRunning()) {

                    container.start();
                    logger.info("Container from session " + sessionID + " has started.");

                    if ( propPath.isEmpty()) {
                        container.exec("-n", "-t", "/jmx/" + jmxPath, "-l", jtlPath);
                    } else {
                        container.exec("-n", "-t", "/jmx/" + jmxPath, "-l", jtlPath, "-p", prop);
                    }
                    
                    logger.info("Container from session " + sessionID + " has exectuted the JMeter commands.");
                    
                } else {
                    throw new JMeterManagerException("The JmxPath has not been specified correctly.");
                }
            } else {
                throw new JMeterManagerException("Container of session " + sessionID + " is already running and in the process of executing JMeter.");
            }
        } catch (DockerManagerException e) {
            throw new JMeterManagerException("JMeter session " + sessionID + " could not be started", e);
        }
        

     }

     @Override
     public void waitForJMeter() throws JMeterManagerException {
    
        if ( container == null) {
            return;
        }
        long timeout = DEFAULT_TIMER;
        long  endTime = System.currentTimeMillis() + timeout;
               long  heartbeat = System.currentTimeMillis() + 500;
               while(System.currentTimeMillis() < endTime) {
                       if (System.currentTimeMillis() > heartbeat) {
                               logger.info("Waiting for JMeter session " + sessionID + " to finish");
                               heartbeat = System.currentTimeMillis() + 30000;
                       }
                       try {
                               if (!container.isRunning()) {
                                       logger.info("JMeter session " + sessionID + " has finished");
                                       return;
                               }
                               Thread.sleep(1000);
                       } catch (Exception e) {
                               throw new JMeterManagerException("Problem with checking if JMeter container is still running", e);
                       }
               }
     }

     @Override
     public void waitForJMeter(long timeout) throws JMeterManagerException {
        if ( container == null) {
            return;
        }
        long  endTime = System.currentTimeMillis() + timeout;
               long  heartbeat = System.currentTimeMillis() + 500;
               while(System.currentTimeMillis() < endTime) {
                       if (System.currentTimeMillis() > heartbeat) {
                               logger.info("Waiting for JMeter session " + sessionID + " to finish");
                               heartbeat = System.currentTimeMillis() + 30000;
                       }
                       try {
                               if (!container.isRunning()) {
                                       logger.info("JMeter session " + sessionID + " has finished");
                                       return;
                               }
                               Thread.sleep(1000);
                       } catch (Exception e) {
                               throw new JMeterManagerException("Problem with checking if JMeter container is still running", e);
                       }
               }
     }

     /**
      * Needs to happen first!!!
      */
     @Override
     public void setJmxFile(InputStream jmxStream) throws JMeterManagerException{
        
        try {
            container.storeFile("/jmx/" + jmxPath, jmxStream);
        } catch (Exception e) {
            throw new JMeterManagerException("Could not store the .jmx file correctly.",e);
        }

     }

     @Override
     public String getJmxFile() throws JMeterManagerException {
        try{
            if ( container == null ) {
                return null;
            }

            String jmx = "/jmx/" + jmxPath;
            return container.retrieveFileAsString(jmx);

        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the log file from the container.");
        }
     }

     @Override
     public String getLogFile() throws JMeterManagerException {
        try{
            if ( container == null ) {
                return null;
            }

            String logPath = "/jmx/" + jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".log";
            return container.retrieveFileAsString(logPath);

        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the log file from the container.");
        }
        
     }

     @Override
     public String getConsoleOutput() throws JMeterManagerException {
        try{
            if ( container == null ) {
                return null;
            }

            String logPath = "/jmx/" + jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".log";
            return container.retrieveFileAsString(logPath);

        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the log file from the container.");
        }
     }

     @Override
     public String getListenerFile(String fileName) throws JMeterManagerException {
        try{
            if ( container == null ) {
                return null;
            }

            String filePath = "/jmx/" + fileName;
            return container.retrieveFileAsString(filePath);

        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the log file from the container.");
        }
     }

     @Override
     public void stopTest(long timeout) throws JMeterManagerException {
        
        try {
            container.stop();
        } catch (DockerManagerException e) {
            throw new JMeterManagerException("Issueing the shutdown of the container and JMeter session" + sessionID);
        } 
        
     }

     @Override
     public long getExitCode() throws JMeterManagerException {
        
        try {
            return container.getExitCode();
        } catch (DockerManagerException e) {
            throw new JMeterManagerException("Issueing the shutdown of the container and JMeter session" + sessionID);
        } 

     }

}