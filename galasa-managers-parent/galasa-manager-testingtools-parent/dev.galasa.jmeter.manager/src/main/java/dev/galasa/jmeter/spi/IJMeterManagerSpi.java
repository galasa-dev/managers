/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.jmeter.spi;

import dev.galasa.jmeter.IJMeterManager;
import dev.galasa.jmeter.IJMeterSession;

/**
 * Allowing other managers to access a specific JMeter session to interact with it
 */
public interface IJMeterManagerSpi extends IJMeterManager {

    /**
     * Gives other managers the ability to access the running session instance
     * @return an IJMeterSession instance
     */
    public IJMeterSession getSession(String sessionId);
}