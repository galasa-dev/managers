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
import dev.galasa.docker.IDockerExec;
import dev.galasa.docker.IDockerImage;
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
                
                String jtlPath = "/" + jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".jtl";
                String prop = "/prop/" + propPath;
 
                
                if (jmxPath.toLowerCase().endsWith(".jmx")) {

                    logger.info("Container from session " + sessionID + " has started.");

                    if ( propPath.isEmpty()) {

                        IDockerExec exec = container.exec("jmeter", "-n", "-t", jmxPath, "-l", jtlPath);
                        
                        if ( exec.getExitCode() == 0) {
                            logger.info(container.retrieveFileAsString("/jmeter/test.jtl"));
                        } else {
                            throw new JMeterManagerException("The Jmeter command has failed");
                        }
                        logger.info(container.retrieveFileAsString("/jmeter/test.jtl"));
                    } else {
                        
                        IDockerExec exec = container.exec("jmeter", "-n", "-t", "/jmx/" + jmxPath, "-l", jtlPath, "-p", prop);
                    
                        if ( exec.getExitCode() == 0) {
                            logger.info(container.retrieveFileAsString("/jmeter/test.jtl"));
                        } else {
                            throw new JMeterManagerException("The Jmeter command has failed");
                        }
                    }
                    
                    logger.info("Container from session " + sessionID + " has executed the JMeter commands.");
                    
                } else {
                    throw new JMeterManagerException("The JmxPath has not been specified correctly.");
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
                    container.start();
                    container.storeFile("/jmeter/" + jmxPath, jmxStream);
                    logger.info(jmxPath + " has been stored in the container.");
                
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
            throw new JMeterManagerException("Could not retrieve the jmx file from the container.");
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
            throw new JMeterManagerException("Could not retrieve the console file from the container.");
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
            throw new JMeterManagerException("Could not retrieve " + fileName + " from the container.");
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