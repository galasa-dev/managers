package dev.galasa.jmeter.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.jmeter.IJMeterSession;
import dev.galasa.jmeter.JMeterSession;
import dev.galasa.jmeter.internal.JMeterManagerException;

/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

 @Test
 public class JMeterManagerIVT {

   @Logger
   public Log logger;

    @JMeterSession(jmxPath = "jmeter-example.jmx")
    public IJMeterSession session;

    @Test
    public void provisionedNotNull() throws JMeterManagerException {

      assertThat(logger).isNotNull();
      assertThat(session).isNotNull();

      
    }

    @Test
    public void startJMeterTest() throws JMeterManagerException{
      session.startJmeter();
    }
 }