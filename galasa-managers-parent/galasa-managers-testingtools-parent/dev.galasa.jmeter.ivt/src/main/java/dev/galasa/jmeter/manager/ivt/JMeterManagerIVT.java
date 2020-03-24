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

    @JMeterSession(jmxPath = "test.jmx", propPath = "jmeter.properties")
    public IJMeterSession session;


    @Test
    public void provisionedNotNull() throws Exception {

      assertThat(logger).isNotNull();
      assertThat(session).isNotNull();

    }

    @Test
    public void startJMeterTestWithProperties() throws JMeterManagerException, TestBundleResourceException {
       
      IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
      InputStream jmxStream = bundleResources.retrieveFile("/test.jmx");

      session.setJmxFile(jmxStream);
      session.startJmeter(60000);
      String logOutput = session.getLogFile();

      session.stopTest();

      assertThat(logOutput).contains("Loading file: test.jmx");
      assertThat(logOutput).contains("Running test");
      assertThat(logOutput).contains("Notifying test listeners of end of test");
    }
  
 }