/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.jmeter;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import dev.galasa.jmeter.JMeterManagerException;

/**
 * Interface for creation, management, deletion of JMeter sessions
 */
public interface IJMeterSession {
    
    /**
     * This method gets called before start of a session providing necessary properties
     * to the JMeter session
     * @param properties A map of Strings with provided properties
     * @throws JMeterManagerException
     */
    public void applyProperties(InputStream propStream) throws JMeterManagerException;

    /**
     * Start up a jmeter thread to run through the lifetime of the tests with a default timeout of 60 seconds
     * Usage of cps.properties with their own default location for JMX files...
     * @throws JMeterManagerException
     */
    public void startJmeter() throws JMeterManagerException;

    /**
     * Start up a jmeter thread to run through the lifetime of the tests with a specified timeout
     * Usage of cps.properties with their own default location for JMX files...
     * @throws JMeterManagerException
     */
    public void startJmeter(int timeout) throws JMeterManagerException;

    /**
     * Allows the tester to provide a jmxFile to the running session
     * @param jmxStream
     * @throws JMeterManagerException
     */
    public void setJmxFile(InputStream jmxStream) throws JMeterManagerException;
    /**
     * Returning the jmxFile in UTF-8 encoding in String format
     * The working directory is found through the session instance
     * @param jmxFile the jmx-file
     * @throws JMeterManagerException
     */
    public String getJmxFile() throws JMeterManagerException;

    /**
     * Returning the logFile in UTF-8 encoding in String format
     * The working directory is found through the session instance
     * @param logFile the logFile
     * @throws JMeterManagerException
     */
    public String getLogFile() throws JMeterManagerException;

    /**
     * Returns the consoleOutput in String format
     * @return String of console
     * @throws JMeterManagerException
     */
    public String getConsoleOutput() throws JMeterManagerException;

    /**
     * Return the output file of your jmx execution in String format 
     * The working directory is found through the session instance
     * @param fileName the ListenerFile
     * @throws JMeterManagerException
     */
    public String getListenerFile(String fileName) throws JMeterManagerException;

    /**
     * 
     * @return the logFile gets returned as a string like "cat" would in a linux container
     * @throws JMeterManagerException
     */
    public boolean statusTest() throws JMeterManagerException;

    /**
     * Giving jmeter instance a shutdown signal to finish and clean up all running tests
     * @param timeout specifying a timeout in milliseconds
     * @throws JMeterManagerException
     */
    public void stopTest() throws JMeterManagerException;

    /**
     * Returns the exit code of the the shutdown of the JMeterProcess
     * @return exitcode as a long
     * @throws JMeterManagerException
     */
    public long getExitCode() throws JMeterManagerException;

    public int getSessionID();
       
}