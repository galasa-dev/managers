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
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobname;
import dev.galasa.zosbatch.IZosBatchJobname;

import org.apache.commons.logging.Log;

@Test
public class CICSTSManagerIVT {
	
   @Logger
   public Log logger;

   @CicsRegion
   public ICicsRegion cics;

   @CicsTerminal
   public ICicsTerminal terminal;
   
   @ZosBatch(imageTag = "PRIMARY")
   public IZosBatch batch;
   
   @ZosBatchJobname(imageTag = "PRIMARY")
   public IZosBatchJobname jobName;
   
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
	   logger.info("APPLID retrieved by the CICS TS Manager: " + testApplid);
	   terminal.type("CEMT INQUIRE").enter().waitForKeyboard();
	   String terminalScreen = terminal.retrieveScreen();
	   assertThat(terminalScreen.contains(testApplid)).isTrue();
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
	   logger.info("Testing that the CICS TS Manager finds the DSE CICS job for the CICS Region");
	   logger.info("There should be an active job as the CICS Region is running");
	   boolean exceptionThrown = false;
	   IZosBatchJob regionJob = null;
	   try {
		   regionJob = cics.getRegionJob();
	   } catch (CicstsManagerException e) {
		   exceptionThrown = true;
	   }
	   assertThat(exceptionThrown).isFalse();
	   assertThat(regionJob).isNotNull();	   
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
	   logger.info("CICS Region retrieved by the ICicsTerminal: " + testCicsRegion);
	   terminal.type("CEMT INQUIRE").enter().waitForKeyboard();
	   assertThat(terminal.retrieveScreen().contains(testCicsRegion)).isTrue();
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
	   logger.info("Testing that the CICS Terminal connects to the CICS Region");
	   logger.info("First, need to disconnect from the CICS Region");
	   terminal.disconnect();
	   assertThat(terminal.isConnected()).isFalse();
	   terminal.connectToCicsRegion();
	   assertThat(terminal.isConnected()).isTrue();
	   assertThat(terminal.isClearScreen()).isTrue();
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager resets and clears correctly
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testResetAndClear() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing that the CICS Terminal resets and clears screen correctly");
	   boolean exceptionThrown = false;
	   try {
		   terminal.resetAndClear();
	   } catch (CicstsManagerException e) {
		   exceptionThrown = true;
	   }
	   assertThat(exceptionThrown).isFalse();
	   assertThat(terminal.isClearScreen()).isTrue();
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager sets uppercase translation correctly
    * @throws CicstsManagerException 
    */
   @Test
   public void testUppercaseTranslation() throws CicstsManagerException {
	   logger.info("Testing that the CICS Terminal sets uppercase translation correctly");
	   boolean exceptionThrown = false;
	   try {
		   terminal.setUppercaseTranslation(true);
	   } catch (Exception e) {
		   exceptionThrown = true;
	   }
	   assertThat(exceptionThrown).isFalse();
	   assertThat(terminal.isUppercaseTranslation()).isTrue();
	   
	   try {
		   terminal.setUppercaseTranslation(false);
	   } catch (Exception e) {
		   exceptionThrown = true;
	   }
	   assertThat(exceptionThrown).isFalse();
	   assertThat(terminal.isUppercaseTranslation()).isFalse();
   }
 

}
