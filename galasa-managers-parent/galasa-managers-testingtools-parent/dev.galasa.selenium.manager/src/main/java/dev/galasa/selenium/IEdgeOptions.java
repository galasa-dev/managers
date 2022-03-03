/*
* Copyright contributors to the Galasa project 
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