package dev.galasa.jmeter.spi;

public interface IJMeterSessionSpi extends IJMeterManager{

    /**
     * Gives other managers the ability to access the running session instance
     * @return an IJMeterSession instance
     */
    public IJMeterSession getSession(String sessionId);
}