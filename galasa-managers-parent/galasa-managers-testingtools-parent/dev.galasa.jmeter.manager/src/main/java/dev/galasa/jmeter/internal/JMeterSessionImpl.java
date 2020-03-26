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
import dev.galasa.framework.spi.IFramework;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterManagerException;

public class JMeterSessionImpl implements IJMeterSession {

    private final IFramework framework;
    private final JMeterManagerImpl jMeterManager;
    private final int sessionID;
    private String jmxPath;
    private String propPath;
    private String jmxAbsolutePath = "";
    private String propAbsolutePath = "";
    private IDockerContainer container;
    private final long DEFAULT_TIMER = 60000L;
    private IDockerExec exec = null; 

    private Log logger;

    public JMeterSessionImpl(IFramework framework, JMeterManagerImpl jMeterManager, int sessionID, String jmxPath,
            String propPath, IDockerContainer container, Log logger) throws DockerManagerException {
        this.framework = framework;
        this.jMeterManager = jMeterManager;
        this.sessionID = sessionID;
        this.container = container;
        this.jmxPath = jmxPath;
        this.propPath = propPath;
        this.logger = logger;

        container.start();

        logger.info(String.format("Session %d have been succesfully initialised", this.sessionID));
    }

    @Override
    public int getSessionID() {
        return this.sessionID;
    }

    /**
     * Actually executing JMeter with the given JMX that has been set with the default 60s timeout
     */
    @Override
    public void startJmeter() throws JMeterManagerException {

        try {

            int timeout = (int) DEFAULT_TIMER;
            String jtlPath = jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".jtl";
            String logfile = jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".log";

            if (( this.jmxPath.toLowerCase().endsWith(".jmx") ) && ( !this.jmxAbsolutePath.isEmpty() )) {

                if (this.propAbsolutePath.isEmpty()) {

                    exec = container.exec(timeout, "jmeter", "-n", "-t", this.jmxPath, "-l", jtlPath, "-j", logfile);
                    exec.waitForExec(timeout);

                    if ( !(exec.getExitCode() == 0L) ) {
                        logger.info("JMeter commands have failed with exitcode " + exec.getExitCode());
                        throw new JMeterManagerException();
                    }
                    
                } else {
    
                    exec = container.exec(timeout, "jmeter", "-n", "-t", this.jmxPath, "-l", jtlPath, "-p", this.propPath, "-j", logfile);
                    exec.waitForExec(timeout); 

                    if ( !(exec.getExitCode() == 0L) ) {
                        logger.info("JMeter commands have failed with exitcode " + exec.getExitCode());
                        throw new JMeterManagerException();
                    }
                } 
                    
            } else {
                throw new JMeterManagerException("The JmxPath has not been specified correctly of session " + this.sessionID + ".");
            }
        
        } catch (Exception e) {
            throw new JMeterManagerException("JMeter session " + sessionID + " could not be executed properly.");
        }
    }



    /**
     * Actually executing JMeter with the given JMX that has been set with a specified timeout
     */
    @Override
    public void startJmeter(int timeout) throws JMeterManagerException {

        try {


            String jtlPath = jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".jtl";
            String logfile = jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".log";

            if (( this.jmxPath.toLowerCase().endsWith(".jmx") ) && ( !this.jmxAbsolutePath.isEmpty() )) {

                if (this.propAbsolutePath.isEmpty()) {

                    IDockerExec exec = container.exec(timeout, "jmeter", "-n", "-t", this.jmxPath, "-l", jtlPath, "-j", logfile);
                    exec.waitForExec(timeout);

                    if ( !(exec.getExitCode() == 0L) ) {
                        logger.info("JMeter commands have failed with exitcode " + exec.getExitCode());
                        throw new JMeterManagerException();
                    }
                    
                } else {
    
                    IDockerExec exec = container.exec(timeout, "jmeter", "-n", "-t", this.jmxPath, "-l", jtlPath, "-p", this.propPath, "-j", logfile);
                    exec.waitForExec(timeout); 

                    if ( !(exec.getExitCode() == 0L) ) {
                        logger.info("JMeter commands have failed with exitcode " + exec.getExitCode());
                        throw new JMeterManagerException();
                    }
                } 
                    
            } else {
                throw new JMeterManagerException("The JmxPath has not been specified correctly of session " + this.sessionID + ".");
            }
        
        } catch (Exception e) {
            throw new JMeterManagerException("JMeter session " + sessionID + " could not be executed properly.");
        }
    }

    /**
     * Uses the annotation of JmxPath to store it in the container with the ArtifactManager
     * @param jmxStream
     * @throws JMeterManagerException
    */
    @Override
    public void setJmxFile(InputStream jmxStream) throws JMeterManagerException{
    
        try {
            this.jmxAbsolutePath = "/jmeter/" + this.jmxPath;
            container.storeFile(this.jmxAbsolutePath, jmxStream);
            logger.info(jmxPath + " has been stored in the container.");
            
        } catch (Exception e) {
            throw new JMeterManagerException("Could not store the .jmx file correctly.",e);
        }
    } 

    /**
     * If the the property annotation is filled in, the custom property file gets used
     * Uses the annotation of PropPath to store it in the container with the use ArtifactManager
     * @param propStream
     * @throws JMeterManagerException
     */
    @Override
    public void applyProperties(InputStream propStream) throws JMeterManagerException {
        try {
            this.propAbsolutePath = "/jmeter/" + propPath;
            container.storeFile(this.propAbsolutePath, propStream);

            logger.info(propPath + " has been stored in the container.");
        } catch (Exception e) {
            throw new JMeterManagerException("Could not store the .jmx file correctly.", e);
        }
    }
    
     
    /**
     * @return the jmxFile gets returned as a string like "cat" would in a linux container
     * @throws JMeterManagerException 
     */
    @Override
    public String getJmxFile() throws JMeterManagerException {
        try{
            String jmx = "/jmeter/" + this.jmxPath;
            String jmxStr = "";
            if ( container.isRunning() ) {
                jmxStr = container.retrieveFileAsString(jmx);
            }

            return jmxStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the jmx file from the container.");
        }
    }

    /**
     * @return the logFile gets returned as a string like "cat" would in a linux container
     * @throws JMeterManagerException 
     */
    @Override
    public String getLogFile() throws JMeterManagerException {
        try{
            String logPath = "/jmeter/" + this.jmxPath.substring(0, jmxPath.indexOf(".jmx")) + ".log";
            String logAsStr = "";
            if ( container.isRunning() ) {
                logAsStr = container.retrieveFileAsString(logPath);
            }
            
            return logAsStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the log file from the container.");
        }

    }

    /**
     * @return the consoleOutput gets returned as a string
     * @throws JMeterManagerException 
     */
    @Override
    public String getConsoleOutput() throws JMeterManagerException {
        try{
            String consoleStr = "";
            if ( container.isRunning() ) {
                consoleStr = container.retrieveStdOut();
            }

            return consoleStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve the console file from the container.");
        }
    }

    /**
     * @param fileName
     * @return the specified file gets returned as a string
     * @throws JMeterManagerException 
     */
    @Override
    public String getListenerFile(String fileName) throws JMeterManagerException {
        try{
            String filePath = "/jmeter/" + fileName;
            String listenerStr = "";
            if ( container.isRunning() ) {        
                listenerStr = container.retrieveFileAsString(filePath);
            }

            return listenerStr;
        } catch (Exception e) {
            throw new JMeterManagerException("Could not retrieve " + fileName + " from the container.");
        }
    }

    /**
     * @return if the test has been succesfull or not
     */
    @Override
    public boolean statusTest() throws JMeterManagerException {
        String logOutput = this.getLogFile();
        boolean test = false;

        if ( logOutput.contains("Loading file: " + this.jmxPath) && logOutput.contains("Running test") && logOutput.contains("Notifying test listeners of end of test") ) {
            test = true;
        } else {
            throw new JMeterManagerException("The test didn't succeed with the given jmx for the session " + sessionID);
        }

        return test;
    }

     /**
     * Kills off this full session 
     * @throws JMeterManagerException 
     */
    @Override
    public void stopTest() throws JMeterManagerException {   
        try {
            container.stop();
        } catch (DockerManagerException e) {
            throw new JMeterManagerException("Issue with the shutdown of the container and JMeter session" + sessionID);
        } 
    }

    /**
     * @return the exit code from the last executed command of the container
     */
    @Override
    public long getExitCode() throws JMeterManagerException {     
        try {
            return container.getExitCode();
        } catch (DockerManagerException e) {
            throw new JMeterManagerException("Issue with retrieving the latest exit code" + sessionID);
        } 
    }

    public void disconnect() { 
        if ( exec != null) {
            exec.getConnection().disconnect();
            logger.info("Closed the connection for session " + sessionID);
        } 
       
    }

}