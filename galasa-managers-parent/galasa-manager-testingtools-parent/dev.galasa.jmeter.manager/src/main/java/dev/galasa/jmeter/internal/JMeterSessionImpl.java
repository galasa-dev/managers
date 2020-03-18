/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.jmeter.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.internal.properties.JMeterHost;

import org.apache.jmeter.JMeter;

public class JMeterSessionImpl implements IJMeterSession {

    private final IFramework framework;
    private final JMeterManagerImpl jMeterManager;
    private final int sessionID;
    private Map<String, String> jmxProperties;
    private String[] arguments;
    private String jmxPath;

    // private final DockerManagerSpi dockerManager;
    // private final IDockerContainer container;

    private static final Log logger = LogFactory.getLog(JMeterSessionImpl.class);

    public JMeterSessionImpl(IFramework framework, JMeterManagerImpl jMeterManager, Map<String, String> jmxProperties,
            int sessionID, String jmxPath/* , DockerManagerSpi dockerManager */) {
        this.framework = framework;
        this.jMeterManager = jMeterManager;
        this.sessionID = sessionID;

        applyProperties(jmxProperties);
        setJmxFile(jmxPath);

        // this.dockerManager = dockerManager;

        logger.info(String.format("Session %d have been succesfully initialised", this.sessionID));
    }

    @Override
    public int getSessionID() {
        return this.sessionID;
    }

    @Override
    public void applyProperties(Map<String, String> properties) {

        if ((properties != null) && (properties.size() > 0)) {
            this.jmxProperties = properties;

            logger.info("The JMX-properties have been succesfully applied.");
        }

    }

    @Override
    public void startJmeter() throws JMeterManagerException {

        JMeter jmeter = new JMeter();
        File jmxFile = new File("resources/jmx/" + jmxPath);
        // 1 Specified JMX File or all jmx files
        if ( (jmxPath.toLowerCase().endsWith(".jmx")) || (jmxFile.exists()) ) {
                
            logger.info("test");
            String fileName = jmxPath.substring(0,jmxPath.length() - ".jmx".length());
            String jmxStr = "resources/jmx/" + fileName;
            String jtlStr = "resources/reports/" + fileName + Integer.toString(sessionID) + ".jtl";
            String logStr = "resources/logs/" + fileName + Integer.toString(sessionID) + ".log";
            String propStr = "resources/properties/" + "jmeter.properties";

            String[] args = {"-n", "-t", jmxStr, "-p", propStr, "-d", "/resources/jmeter/",
            "-l", jtlStr, "-j", logStr };

            jmeter.start(args);
            
        } else {
            throw new JMeterManagerException("The JmxPath has not been specified correctly.");
        }
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
         if (!path.isEmpty() && path != null) {
             this.jmxPath = path;
         }

     }

     @Override
     public String getJmxFile() {
         return this.jmxPath;
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