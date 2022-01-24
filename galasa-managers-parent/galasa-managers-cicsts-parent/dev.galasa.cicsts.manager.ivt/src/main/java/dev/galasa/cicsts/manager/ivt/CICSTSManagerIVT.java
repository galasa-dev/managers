/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.ZosBatchException;

import org.apache.commons.logging.Log;

@Test
public class CICSTSManagerIVT {
	
   @Logger
   public Log logger;

   @CicsRegion
   public ICicsRegion cics;

   @CicsTerminal
   public ICicsTerminal terminal;
  
   @BeforeClass
   public void login() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException {
      terminal.clear();
      terminal.waitForKeyboard();
   }
	
   @Test
   public void testNotNull() {
	   assertThat(logger).isNotNull();
	   assertThat(cics).isNotNull();
	   assertThat(terminal).isNotNull();
   }
   
   /**
    * Tests that the CICS TS Manager retrieves the correct APPLID for the CICS Region
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test 
   public void testGetApplid() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException  {
	   logger.info("Testing that the CICS TS Manager gets the correct APPLID for the CICS Region");
	   String testApplid = cics.getApplid();
	   assertThat(checkTerminalScreenContains(testApplid)).isTrue();
	   logger.info("The correct APPLID was obtained");
   }
   
   /**
    * Tests that the CICS TS Manager retrieves a CICS Resource
    * @throws CicstsManagerException 
    */
   @Test
   public void testCicsResource() throws CicstsManagerException  {
	   logger.info("Testing that the CICS TS Manager retrieves a CICS Resource");
	   ICicsResource cicsResource = cics.cicsResource();
	   assertThat(cicsResource).isNotNull();
	   logger.info("CICS Resource obtained");
   }
   
   /**
    * Tests that the CICS TS Manager retrieves the correct job
    * @throws ZosBatchException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testGetRegionJob() throws CicstsManagerException, ZosBatchException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing that the CICS TS Manager finds the DSE CICS job for the CICS Region - there should be an active job as the CICS Region is running");
	   IZosBatchJob regionJob = cics.getRegionJob();
	   assertThat(regionJob).isNotNull();
	   logger.info("Region job obtained");
	   
	   logger.info("Testing that the CICS Region job information is correct");
	   assertThat(regionJob.getJobname().toString()).isEqualToIgnoringCase(cics.getApplid());
	   assertThat(regionJob.getType()).isEqualToIgnoringCase("JOB");
	   assertThat(regionJob.getStatusString()).isEqualToIgnoringCase("ACTIVE");
	   logger.info("Region job information correct");
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager retrieves the correct CICS Region
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testGetCicsRegion() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing that the CICS Terminal gets the correct CICS Region");
	   String testCicsRegion = terminal.getCicsRegion().toString().replace("CICS Region[", "").replace("]", "");
	   assertThat(checkTerminalScreenContains(testCicsRegion)).isTrue();
	   logger.info("Correct CICS Region obtained");
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager correctly connects to the CICS Region
    * @throws CicstsManagerException
    * @throws TerminalInterruptedException 
    * @throws NetworkException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testConnectToCicsRegion() throws CicstsManagerException, TerminalInterruptedException, KeyboardLockedException, NetworkException, TimeoutException {
	   logger.info("Testing that the CICS Terminal connects to the same CICS Region after being disconnected");
	   
	   terminal.disconnect();
	   assertThat(terminal.isConnected()).isFalse();
	   
	   terminal.connectToCicsRegion();
	   assertThat(terminal.isConnected()).isTrue();
	   assertThat(terminal.isClearScreen()).isTrue();
	   assertThat(terminal.getCicsRegion().toString().replace("CICS Region[", "").replace("]", "")).isEqualTo(cics.getApplid());
	   logger.info("Terminal reconnected to the correct CICS Region"); 
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager resets and clears correctly
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testResetAndClear() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, CicstsManagerException {
	   logger.info("Testing that the CICS Terminal resets and clears screen correctly");
	   terminal.resetAndClear();
	   assertThat(terminal.isClearScreen()).isTrue();
	   logger.info("Reset and clear was successful");
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager sets uppercase translation correctly
    * @throws CicstsManagerException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testUppercaseTranslation() throws CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing that the CICS Terminal sets uppercase translation correctly");
	   logger.info("Setting uppercase translation to true");
	   terminal.setUppercaseTranslation(true);
	   assertThat(terminal.isUppercaseTranslation()).isTrue();
	   
	   logger.info("Testing that uppercase translation is on");
	   String variableName = "INPUT";
	   String variableValue = "lowercasegalasa";
	   startCeciSession();
	   cics.ceci().defineVariableText(terminal, variableName, variableValue);
	   assertThat(cics.ceci().retrieveVariableText(terminal, "&" + variableName)).isUpperCase();
	   logger.info("Lower case characters were translated to upper case");
	   
	   logger.info("Setting uppercase translation to false");
	   terminal.setUppercaseTranslation(false);
	   assertThat(terminal.isUppercaseTranslation()).isFalse();
	   
	   logger.info("Testing that uppercase translation is off");
	   variableName = "INPUT2";
	   startCeciSession();
	   cics.ceci().defineVariableText(terminal, variableName, variableValue);
	   assertThat(cics.ceci().retrieveVariableText(terminal, "&" + variableName)).isEqualTo(variableValue);
	   logger.info("Lower case characters were not translated to upper case");
   }
   
   private boolean checkTerminalScreenContains(String expectedString) throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   terminal.type("CEMT INQUIRE").enter().waitForKeyboard();
	   if (terminal.retrieveScreen().contains(expectedString)) {
		   return true;
	   } else {
		   return false;
	   }
   }
   
   private void startCeciSession() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   terminal.clear();
	   terminal.waitForKeyboard();
	   terminal.type("CECI").enter().waitForKeyboard();
   }
 

}
