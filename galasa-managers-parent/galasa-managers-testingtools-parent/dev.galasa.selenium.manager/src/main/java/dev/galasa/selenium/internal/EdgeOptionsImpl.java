/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.selenium.internal;

import org.openqa.selenium.edge.EdgeOptions;

import dev.galasa.selenium.IEdgeOptions;

/**
 * An Inplementation of EdgeOptions as to avoid test code having dependanicies 
 * on org.openqa.selenium*. 
 */

public class EdgeOptionsImpl implements IEdgeOptions {

    public EdgeOptions options;

    public EdgeOptionsImpl() {
        options = new EdgeOptions();
    }

    @Override
    public EdgeOptions getOptions() {
        return this.options;
    }

    @Override
    public void setCapability(String key, Object value) {
        options.setCapability(key, value);
    }

    @Override
    public void setCapability(String key, String value) {
        options.setCapability(key, value);
    }

    @Override
    public void setCapability(String key, Boolean value) {
        options.setCapability(key, value);
    }
    
}