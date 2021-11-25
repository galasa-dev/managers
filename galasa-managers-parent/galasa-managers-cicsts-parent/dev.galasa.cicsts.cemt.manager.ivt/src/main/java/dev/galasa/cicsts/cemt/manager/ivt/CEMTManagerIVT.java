/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.cemt.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.After;
import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.cicsts.CemtException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class CEMTManagerIVT {
	
   @Logger
   public Log logger;
  
   @CicsRegion
   public ICicsRegion cics;
   
   @CicsTerminal
   public ICicsTerminal cemtTerminal;
   
   @CicsTerminal 
   public ICicsTerminal cedaTerminal;
   
   @CicsTerminal
   public ICicsTerminal resourceTerminal;
   
   @CoreManager
   public ICoreManager coreManager;
   
   private String runName  = new String();
   
   private String programName = new String();
   
   @BeforeClass
   public void login() throws Exception {
		  
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
      
      cedaTerminal.clear();
      cedaTerminal.waitForKeyboard();
      
      resourceTerminal.clear();
      resourceTerminal.waitForKeyboard(); 
      
      runName = coreManager.getRunName();
      logger.info("Using Run ID of: " + runName);
      
      programName = runName;
      if (programName.length() > 8) {
    	  programName = programName.substring(programName.length() - 8);
	   }
      logger.info("Using unique name for Programs: " + programName);
   }
   
   @Before
   public void installProgramResource() throws CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
      resourceTerminal.type("CEDA DEFINE PROGRAM(" + programName + ") GROUP(EXGROUP)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      resourceTerminal.type("CEDA INSTALL PROGRAM(" + programName + ") GROUP(EXGROUP)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();  
   }
   
   @After
   public void clearResources() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException, InterruptedException {
      resourceTerminal.type("CEMT DISCARD PROGRAM(" + programName + ")").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      resourceTerminal.type("CEDA DELETE PROGRAM(" + programName + ") GROUP(exGroup)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();   
   }
   
   @Test
   public void testCEMTIsNotNull() throws CicstsManagerException {
      assertThat(cics).isNotNull();
      assertThat(cics.cemt()).isNotNull();
      assertThat(cics.ceda()).isNotNull();
      assertThat(cedaTerminal).isNotNull();
      assertThat(cemtTerminal).isNotNull();
      assertThat(resourceTerminal).isNotNull();
      assertThat(logger).isNotNull();
   }
   
   @Test
   public void testInquireResource() throws InterruptedException, CicstsManagerException{
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName).get("program").equals(programName)); 
   }
   
   @Test
   public void testInquireResourceThatDoesntExist() throws InterruptedException, CicstsManagerException {
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName) != null);
      
      cics.ceda().deleteResource(cedaTerminal, "PROGRAM", programName, "EXGROUP");
      cics.cemt().discardResource(cemtTerminal, "PROGRAM", programName);
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName) == null);  
   }

   @Test
   public void testResourceIsRetrievingProperties() throws CicstsManagerException{
      HashMap<String, String> resource = cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName);
      assertThat(resource.get("program").equals(programName));
      assertThat(resource.get("length").equals("0000000000"));
      assertThat(resource.get("language").equals("Notdefined"));
      assertThat(resource.get("progtype").equals("Program"));
      assertThat(resource.get("status").equals("Enabled"));
      assertThat(resource.get("sharestatus").equals("Private"));
   }
   
   @Test
   public void testDiscardResource() throws Exception {
      if (cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName) != null) {
         cics.ceda().deleteResource(cedaTerminal, "PROGRAM", programName, "EXGROUP");
         cics.ceda().createResource(cedaTerminal, "PROGRAM", programName, "EXGROUP", null);
         cics.ceda().installResource(cedaTerminal, "PROGRAM", programName, "EXGROUP");
         if (cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName) == null) {
        	 throw new Exception("Resource " + programName + " was not created");
         }
      }
      
      cics.cemt().discardResource(cemtTerminal, "PROGRAM", programName);
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName) == null);
   }
   
   @Test
   public void testSetResource() throws CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException{
      HashMap<String, String> resource = cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName);
      
      if (!resource.get("status").equals("Disabled")) {
         cics.cemt().setResource(cemtTerminal, "PROGRAM", programName, "DISABLED");
         assertThat(resource.get("status").equals("Disabled"));
      } else {
         cics.cemt().setResource(cemtTerminal, "PROGRAM", programName, "ENABLED");
         assertThat(resource.get("status").equals("Enabled"));
      }
   }
   
   @Test
   public void testDiscardResourceThatDoesntExist() throws CemtException, InterruptedException {
      assertThatThrownBy(() -> {
         cics.cemt().discardResource(cemtTerminal, "PROGRAM", "NONEX");
      }).isInstanceOf(CemtException.class).hasMessageContaining("Problem determining the result from the CEMT command");
   }
   
   @Test
   public void testInquireTransaction() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  String transactionName = runName.replaceAll("[^\\d.]", "");
	  installTransactionResource(transactionName);
      assertThat(cics.cemt().inquireResource(cemtTerminal, "TRANSACTION", transactionName) != null);
      
      clearTransactionResource(transactionName);
   }
   
   @Test
   public void testPerformSystemProperty() throws InterruptedException, CicstsManagerException {
      assertThat(cics.cemt().performSystemProperty(cemtTerminal, "DUMP", "DUMPCODE(TESTING) TITLE(TESTING)", "RESPONSE: NORMAL"));     
   }
   
   @Test
   public void testInquireInvalidResourceType() throws CemtException, CicstsManagerException {
      assertThatThrownBy(() -> {
         cics.cemt().inquireResource(cemtTerminal, "FISH", "example");
      }).isInstanceOf(CemtException.class).hasMessageContaining("Problem with starting CEMT transaction"); 
   }
   
   public void installTransactionResource(String transactionName) throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
      resourceTerminal.type("CEDA DEFINE TRANSACTION(" + transactionName + ") GROUP(TXGRP) PROGRAM(" + programName + ")").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      resourceTerminal.type("CEDA INSTALL TRANSACTION(" + transactionName + ") GROUP(TXGRP)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }
   
   public void clearTransactionResource(String transactionName) throws FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {
      resourceTerminal.type("CEMT DISCARD RESOURCE(" + transactionName + ")").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
      resourceTerminal.type("CEDA DELETE TRANSACTION(" + transactionName + ") GROUP(TXGRP)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }
   
}
