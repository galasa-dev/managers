/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.cemt.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;

import dev.galasa.After;
import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.CemtException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class CEMTManagerIVT {
	
   @CicsRegion
   public ICicsRegion cics;
   
   @CicsTerminal
   public ICicsTerminal cemtTerminal;
   
   @CicsTerminal 
   public ICicsTerminal cedaTerminal;
   
   @CicsTerminal
   public ICicsTerminal resourceTerminal;
   
   @BeforeClass
   public void login() throws Exception {
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
      
      cedaTerminal.clear();
      cedaTerminal.waitForKeyboard();
      
      resourceTerminal.clear();
      resourceTerminal.waitForKeyboard(); 
      
      clearResources();
   }
   
   @Before
   public void before() throws CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  installResources();
   }
   
   @After
   public void after() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException, InterruptedException {
	  clearResources();
   }
   
   @Test
   public void testCEMTIsNotNull() throws CicstsManagerException {
      assertThat(cics).isNotNull();
      assertThat(cics.cemt()).isNotNull();
      assertThat(cics.ceda()).isNotNull();
      assertThat(cedaTerminal).isNotNull();
      assertThat(cemtTerminal).isNotNull();
      assertThat(resourceTerminal).isNotNull();
   }
   
   /**
    * Tests that the inquire resource method retrieves the correct fields
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testInquireResource() throws CemtException, CicstsManagerException {
      HashMap<String, String> resource = cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE");
	  assertThat(resource.get("program").equals("EXAMPLE"));
	  assertThat(resource.get("length").equals("0000000000"));
	  assertThat(resource.get("language").equals("Notdefined"));
	  assertThat(resource.get("progtype").equals("Program"));
	  assertThat(resource.get("status").equals("Enabled"));
	  assertThat(resource.get("sharestatus").equals("Private"));
   }
   
   /**
    * Tests that the inquire resource method retrieves nothing for a resource that doesn't exist
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testInquireResourceThatDoesntExist() throws CemtException, CicstsManagerException {
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") != null);

      cics.ceda().deleteResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
      cics.cemt().discardResource(cemtTerminal, "PROGRAM", "EXAMPLE");
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null);  
   }
   
   /**
    * Tests that the inquire resource method using an invalid resource type throws an exception
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testInquireInvalidResourceType() throws CemtException, CicstsManagerException {
      assertThatThrownBy(() -> {
         cics.cemt().inquireResource(cemtTerminal, "FISH", "example");
      }).isInstanceOf(CemtException.class).hasMessageContaining("Problem with starting CEMT transaction"); 
   }
   
   /**
    * Tests that the inquire resource method using an invalid resource name returns nothing, it should not throw an exception
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testInquireInvalidResourceName() throws CemtException, CicstsManagerException {
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "!!%^") == null); 
   }
   
   /**
    * Tests discarding a resource
    * 
    * @throws CedaException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testDiscardResource() throws CedaException, CicstsManagerException {
      if (cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") != null) {
         cics.ceda().deleteResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
         cics.ceda().createResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup", null);
         cics.ceda().installResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
         if (cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null) {
        	 throw new CedaException("Resource EXAMPLE was not created");
         }
      }

      cics.cemt().discardResource(cemtTerminal, "PROGRAM", "EXAMPLE");
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null);
   }
   
   /**
    * Tests discarding a resource that doesn't exist throws an exception
    */
   @Test
   public void testDiscardResourceThatDoesntExist()  {
      assertThatThrownBy(() -> {
         cics.cemt().discardResource(cemtTerminal, "PROGRAM", "NONEX");
      }).isInstanceOf(CemtException.class).hasMessageContaining("Problem determining the result from the CEMT command");
   }

   /**
    * Tests setting fields in a resource
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testSetResource() throws CemtException, CicstsManagerException {
      HashMap<String, String> resource = cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE");

      if (!resource.get("status").equals("Disabled")) {
         cics.cemt().setResource(cemtTerminal, "PROGRAM", "EXAMPLE", "DISABLED");
         assertThat(resource.get("status").equals("Disabled"));
      } else {
         cics.cemt().setResource(cemtTerminal, "PROGRAM", "EXAMPLE", "ENABLED");
         assertThat(resource.get("status").equals("Enabled"));
      }
   }
   
   /**
    * Tests setting fields in a resource that doesn't exist
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testSetResourceThatDoesntExist() throws CemtException, CicstsManagerException {
	   assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "NONEX") == null);
	   
	   assertThatThrownBy(() -> {
	      cics.cemt().setResource(cemtTerminal, "PROGRAM", "NONEX", "ENABLED");
	   }).isInstanceOf(CemtException.class).hasMessageContaining("Problem determining the result from the CEMT command");
   }
   
   /**
    * Tests setting fields in a resource to invalid values
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testSetResourceToInvalidValues() throws CemtException, CicstsManagerException {
	   assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") != null);
	   
	   assertThatThrownBy(() -> {
	      cics.cemt().setResource(cemtTerminal, "PROGRAM", "EXAMPLE", "FOO");
	   }).isInstanceOf(CemtException.class).hasMessageContaining("Problem determining the result from the CEMT command");
   }
   
   /**
    * Tests inquiring a transaction resource
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testInquireTransaction() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException  {
      installTransactionResource();
      assertThat(cics.cemt().inquireResource(cemtTerminal, "TRANSACTION", "TTRX") != null);

      clearTransactionResource();
   }
   
   /**
    * Tests inquiring a transaction resource with an invalid name returns nothing, it should not throw an exception
    * 
    * @throws CicstsManagerException 
    * @throws CemtException 
    */
   @Test
   public void testInquireTransactionWithInvalidName() throws CemtException, CicstsManagerException {
	   assertThat(cics.cemt().inquireResource(cemtTerminal, "TRANSACTION", "!$%^") == null);
   }
   
   /**
    * Tests performing a system property
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testPerformSystemProperty() throws CemtException, CicstsManagerException {
      assertThat(cics.cemt().performSystemProperty(cemtTerminal, "DUMP", "DUMPCODE(TESTING) TITLE(TESTING)", "RESPONSE: NORMAL"));     
   }
   
   private void installResources() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   resourceTerminal.type("CEDA DEFINE PROGRAM(EXAMPLE) GROUP(EXGROUP)").enter().waitForKeyboard();
	   resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
	   resourceTerminal.type("CEDA INSTALL PROGRAM(EXAMPLE) GROUP(EXGROUP)").enter().waitForKeyboard();
	   resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();  
   }
   
   private void clearResources() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   resourceTerminal.type("CEMT DISCARD PROGRAM(EXAMPLE)").enter().waitForKeyboard();
	   resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
	   resourceTerminal.type("CEDA DELETE PROGRAM(EXAMPLE) GROUP(exGroup)").enter().waitForKeyboard();
	   resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();   
   }

   private void installTransactionResource() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
      resourceTerminal.type("CEDA DEFINE TRANSACTION(TTRX) GROUP(TXGRP) PROGRAM(EX1)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      resourceTerminal.type("CEDA INSTALL TRANSACTION(TTRX) GROUP(TXGRP)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }

   private void clearTransactionResource() throws FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {
      resourceTerminal.type("CEMT DISCARD RESOURCE(TTRX").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      resourceTerminal.type("CEDA DELETE TRANSACTION(TTRX) GROUP(TXGRP)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }
   
}
