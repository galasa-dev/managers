/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.jmeter;

import java.io.File;

/**
 * Interface for creation, management, deletion of JMeter sessions
 */
public interface IJMeterSession {
    
    /**
     * Start up a jmeter thread to run through the lifetime of the tests
     * Usage of cps.properties with their own default location for JMX files...
     */
    public void startJmeter();

    /**
     * Giving jmeter a deadline before it needs to be finished and the test gets shutdown
     */
    public void waitForJMeter();

    /**
     * Giving jmeter a timed deadline before it needs to be finished and the test gets shutdown
     * @param timeout specifying a timeout in milliseconds
     */
    public void waitForJMeter(long timeout);

    /**
     * Allows the tester to provide a jmxFile to the running session
     * @param path
     */
    public void setJmxFile(String path);
    /**
     * Returning the jmxFile in UTF-8 encoding in String format
     * The working directory is found through the session instance
     * @param jmxFile the jmx-file
     */
    public String getJmxFile();

    /**
     * Returning the logFile in UTF-8 encoding in String format
     * The working directory is found through the session instance
     * @param logFile the logFile
     */
    public String getLogFile(File logFile);

    /**
     * Returns the consoleOutput in String format
     * @return String of console
     */
    public String getConsoleOutput();

    /**
     * Return the output file of your jmx execution in String format 
     * The working directory is found through the session instance
     * @param fileName the ListenerFile
     */
    public String getListenerFile(String fileName);

    /**
     * Giving jmeter instance a shutdown signal to finish and clean up all running tests
     * @param timeout specifying a timeout in milliseconds
     */
    public void stopTest(long timeout);

    /**
     * Returns the exit code of the the shutdown of the JMeterProcess
     * @return exitcode as a long
     */
    public long getExitCode();
	
}