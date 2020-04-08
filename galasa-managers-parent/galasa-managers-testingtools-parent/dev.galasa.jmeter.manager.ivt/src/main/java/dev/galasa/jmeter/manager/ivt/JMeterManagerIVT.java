package dev.galasa.jmeter.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterSession;
import dev.galasa.jmeter.JMeterManagerException;

/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

 @Test
 public class JMeterManagerIVT {

    @Logger
    public Log logger;

    @BundleResources
    public IBundleResources resources;

    @JMeterSession(jmxPath = "test.jmx", propPath = "jmeter.properties")
    public IJMeterSession session;

    @JMeterSession(jmxPath = "test.jmx")
    public IJMeterSession session2;

    @Test
    public void provisionedNotNull() throws Exception {

      assertThat(logger).isNotNull();
      assertThat(session).isNotNull();
      assertThat(session).isNotNull();
    
    }

    @Test
    public void startJMeterTestWithProperties() throws JMeterManagerException, TestBundleResourceException {
      InputStream jmxStream = resources.retrieveFile("/test.jmx");

      InputStream propStream = resources.retrieveFile("/jmeter.properties");

      session.setJmxFile(jmxStream);
      session.applyProperties(propStream);
      session.startJmeter();

      assertThat(session.statusTest()).isTrue();
    }

    @Test
    public void startJMeterTestWithoutProperties() throws JMeterManagerException, TestBundleResourceException {
      InputStream jmxStream = resources.retrieveFile("/test.jmx");

      session2.setJmxFile(jmxStream);
      session2.startJmeter();

      assertThat(session2.statusTest()).isTrue();
    }

  
 }