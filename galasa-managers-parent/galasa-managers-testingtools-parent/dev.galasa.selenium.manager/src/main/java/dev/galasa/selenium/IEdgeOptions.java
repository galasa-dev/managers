/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium;

import org.openqa.selenium.edge.EdgeOptions;

/**
 * A Options pass through interface for the Selenium Options object
 * 
 * @author jamesdavies
 *
 */
public interface IEdgeOptions {

    public EdgeOptions getOptions();

    public void setCapability(String key, Object value);

    public void setCapability(String key, String value);

    public void setCapability(String key, Boolean value);
    
}