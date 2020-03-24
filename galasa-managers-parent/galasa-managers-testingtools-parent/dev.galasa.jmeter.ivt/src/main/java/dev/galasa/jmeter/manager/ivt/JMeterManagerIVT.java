package dev.galasa.jmeter.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.ArtifactManager;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.IDockerContainer;
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

    @ArtifactManager
    public IArtifactManager artifactManager;

    @JMeterSession(jmxPath = "test.jmx")
    public IJMeterSession session;

    @JMeterSession(jmxPath = "test.jmx", propPath = "jmeter.properties")
    public IJMeterSession session2;


    @Test
    public void provisionedNotNull() throws Exception {

      assertThat(logger).isNotNull();
      assertThat(session).isNotNull();
      assertThat(session2).isNotNull();

    }

    @Test
    public void startJMeterTestWithoutProperties() throws JMeterManagerException, TestBundleResourceException {
       
      IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
      InputStream jmxStream = bundleResources.retrieveFile("/test.jmx");

      session.setJmxFile(jmxStream);
      session.startJmeter(60000);
      String logOutput = session.getLogFile();

      session.stopTest();

      assertThat(logOutput).contains("Created the tree successfully using test.jmx");
      assertThat(logOutput).contains("... end of run");
    }

    @Test
    public void startJMeterTestWithProperties() throws JMeterManagerException, TestBundleResourceException{
       
      IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
      InputStream jmxStream = bundleResources.retrieveFile("/test.jmx");
      InputStream propStream = bundleResources.retrieveFile("/jmeter.properties");

      session2.setJmxFile(jmxStream);
      session2.applyProperties(propStream);
      session2.startJmeter(60000);
      String logOutput = session2.getLogFile();

      session.stopTest();

      assertThat(logOutput).contains("Created the tree successfully using test.jmx");
      assertThat(logOutput).contains("... end of run");
    }

  
 }