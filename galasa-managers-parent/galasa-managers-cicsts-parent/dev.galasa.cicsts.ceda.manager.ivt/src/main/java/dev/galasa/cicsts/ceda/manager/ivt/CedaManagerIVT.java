/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.ceda.manager.ivt;

import static org.assertj.core.api.Assertions.*;

import org.apache.commons.logging.Log;
import org.assertj.core.api.Fail;

import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.CemtException;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class CedaManagerIVT {
	
   @Logger
   public Log logger;

   @CicsRegion()
   public ICicsRegion cics;

   @CicsTerminal()
   public ICicsTerminal cedaTerminal;

   @CicsTerminal()
   public ICicsTerminal cemtTerminal;

   @CicsTerminal()
   public ICicsTerminal terminal;
   
   @CoreManager
   public ICoreManager coreManager;
   
   private String runName  = new String();
   
   private String programName = new String();
   
   private String programA = new String();
   private String programB = new String();
   private String programC = new String();
   private String programD = new String();
   
   private String transactionName = new String();
   
   private String libraryName = new String();
   
   @BeforeClass
   public void login() throws Exception {
      cedaTerminal.clear();
      cedaTerminal.waitForKeyboard();
      
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
      
      terminal.clear();
      terminal.waitForKeyboard();
      
      runName = coreManager.getRunName();
      logger.info("Using Run ID of: " + runName);
      
      programName = getUniqueName(runName, 8);
      
      programA = getUniqueName(runName, 7) + "A";
      programB = getUniqueName(runName, 7) + "B";
      programC = getUniqueName(runName, 7) + "C";
      programD = getUniqueName(runName, 7) + "D";
      
      // Remove letters in case run name begins with C
      // Transaction names cannot begin with C
      transactionName = getUniqueName(runName.replaceAll("[^\\d.]", ""), 4);
      
      libraryName = getUniqueName(runName, 8);
   }
   
   private String getUniqueName(String runName, int maxLength) {
	   String uniqueName = runName;
	   if (uniqueName.length() > maxLength) {
		   uniqueName = uniqueName.substring(uniqueName.length() - maxLength);
	   }
	   return uniqueName;
   }

   @Before
   public void before() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
      // Making sure that elements that will be used in the tests do not exist in the managers
      terminal.clear().waitForKeyboard();
      terminal.type("CEDA DELETE GROUP(Test) ALL").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEDA DELETE GROUP(IVT) ALL").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEDA DELETE GROUP(noIVT) ALL").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEMT DISCARD prog(Program," + programA + "," + programB + "," + programC + "," + programD + ")").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEMT DISCARD transaction(" + transactionName + ")").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEMT DISCARD LIBRARY(" + libraryName + ")").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }

   @Test
	public void checkCECINotNull() throws CicstsManagerException {
      assertThat(cics).isNotNull();
      assertThat(cics.ceda()).isNotNull();
      assertThat(cics.cemt()).isNotNull();
      assertThat(cemtTerminal).isNotNull();
      assertThat(cedaTerminal).isNotNull();
   }

   @Test
	public void testResourceProgram() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception, InterruptedException, CicstsManagerException {
      String resourceType = "PROGRAM";
      String groupName = "Test";
      String resourceParameters = null;
      boolean response = false;
      
      logger.info("Using unique Program name: " + programName);
      
      try {
         // Testing create and install resource by creating it, installing it and then checking if it appeared on CEMT
         assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, programName, groupName)).isEqualTo(false);

         cics.ceda().createResource(cedaTerminal, resourceType, programName, groupName, resourceParameters);
         assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, programName, groupName)).isEqualTo(true);

         cics.ceda().installResource(cedaTerminal, resourceType, programName, groupName);
         if (cics.cemt().inquireResource(cemtTerminal, resourceType, programName) != null) {
            response = true;
         }
         assertThat(response).isEqualTo(true);
         
         // If resource was installed successfully, then tests the delete method by discarding resource from CEMT,
         // deleting and then trying to install and checking if the resource appeared on CEMT
         if (response) {
            response = false;
            cics.cemt().discardResource(cemtTerminal, resourceType, programName);
            if (cics.cemt().inquireResource(cemtTerminal, resourceType, programName) == null) {
               cics.ceda().deleteResource(cedaTerminal, resourceType, programName, groupName);
               cics.ceda().installResource(cedaTerminal, resourceType, programName, groupName);
               if (cics.cemt().inquireResource(cemtTerminal, resourceType, programName) == null) {
                  response = true;
               }
               assertThat(response).isEqualTo(true);

               } else Fail.fail("Failed to discard resource");

         } else Fail.fail("Failed to install / delete resource");

         } catch (CedaException | CemtException e) {
            e.printStackTrace();
         }
      
   }

   @Test
   public void testResourceTransaction() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception, InterruptedException, CicstsManagerException {
      String resourceType = "TRANSACTION";
      String groupName = "Test";
      String resourceParameters = "PROGRAM(" + programName + ")";
      boolean response = false;
      
      logger.info("Using unique Transaction name: " + transactionName);
      
      try {
          // Testing create and install resource by creating it, installing it and then checking if it appeared on CEMT
          assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, transactionName, groupName)).isEqualTo(false);

          cics.ceda().createResource(cedaTerminal, resourceType, transactionName, groupName, resourceParameters);
          assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, transactionName, groupName)).isEqualTo(true);

          cics.ceda().installResource(cedaTerminal, resourceType, transactionName, groupName);
          if (cics.cemt().inquireResource(cemtTerminal, resourceType, transactionName) != null) {
             response = true;
          }
          assertThat(response).isEqualTo(true);

          // If resource was installed successfully, then tests the delete method by discarding resource from CEMT,
          // deleting and then trying to install and checking if the resource appeared on CEMT

          if (response) {
             response = false;
             cics.cemt().discardResource(cemtTerminal, resourceType, transactionName);
             if (cics.cemt().inquireResource(cemtTerminal, resourceType, transactionName) == null) {
                cics.ceda().deleteResource(cedaTerminal, resourceType, transactionName, groupName);
                cics.ceda().installResource(cedaTerminal, resourceType, transactionName, groupName);
                if (cics.cemt().inquireResource(cemtTerminal, resourceType, transactionName) == null) {
                   response = true;
                }
                assertThat(response).isEqualTo(true);

                } else Fail.fail("Failed to discard resource");

            } else Fail.fail("Failed to install / delete resource");

      } catch (CedaException | CemtException e) {
         e.printStackTrace();
      }

   }

   @Test
   public void testResourceLibrary() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception, InterruptedException, CicstsManagerException {
      String resourceType = "LIBRARY";
      String groupName = "Test";
      String resourceParameters = "DSNAME01(CTS.USER.APPL1.CICS.LOAD)";
      boolean response = false;
      
      logger.info("Using unique Library name: " + libraryName);
      
      try {
         // Testing create and install resource by creating it, installing it and then checking if it appeared on CEMT
         assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, libraryName, groupName)).isEqualTo(false);

         cics.ceda().createResource(cedaTerminal, resourceType, libraryName, groupName, resourceParameters);
         assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, libraryName, groupName)).isEqualTo(true);

         cics.ceda().installResource(cedaTerminal, resourceType, libraryName, groupName);
         if (cics.cemt().inquireResource(cemtTerminal, resourceType, libraryName) != null) {
            response = true;
         }
         assertThat(response).isEqualTo(true);
			
         // If resource was installed successfully, then tests the delete method by discarding resource from CEMT, 
         // deleting and then trying to install and checking if the resource appeared on CEMT

         if (response) {
            response = false;
            cics.cemt().discardResource(cemtTerminal, resourceType, libraryName);

            if (cics.cemt().inquireResource(cemtTerminal, resourceType, libraryName) == null) {
               cics.ceda().deleteResource(cedaTerminal, resourceType, libraryName, groupName);
               cics.ceda().installResource(cedaTerminal, resourceType, libraryName, groupName);
               if (cics.cemt().inquireResource(cemtTerminal, resourceType, libraryName) == null) {
                  response = true;
               }
               assertThat(response).isEqualTo(true);

               } else Fail.fail("Failed to discard resource");

         } else Fail.fail("Failed to install / delete resource");

      } catch (CedaException | CemtException e) {
         e.printStackTrace();
      }

   }

   @Test
   public void testGroup() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception, InterruptedException, CicstsManagerException {
      String resourceType = "prog";
      String groupName = "IVT";
      String groupName2 = "noIVT";
      boolean result = false;
      
      // Creating different resources in two different groups
      cics.ceda().createResource(cedaTerminal, resourceType, programA, groupName, null);
      cics.ceda().createResource(cedaTerminal, resourceType, programB, groupName, null);
      cics.ceda().createResource(cedaTerminal, resourceType, programC, groupName, null);
      /** Different group **/
      cics.ceda().createResource(cedaTerminal, resourceType, programD, groupName2, null);
      // Installing only one group and check if installed group appeared in CEMT and not installed one did not
      cics.ceda().installGroup(cedaTerminal, groupName);
      if (cics.cemt().inquireResource(cemtTerminal, resourceType, programA).containsValue(programA.toUpperCase())
    		  && cics.cemt().inquireResource(cemtTerminal, resourceType, programB).containsValue(programB.toUpperCase())
    		  && cics.cemt().inquireResource(cemtTerminal, resourceType, programC).containsValue(programC.toUpperCase())
    		  && cics.cemt().inquireResource(cemtTerminal, resourceType, programD) == null) {
         result = true;
      }
      assertThat(result).isEqualTo(true);

      // Checking if group delete works by discarding elements from CEMT and deleting group from CEDA, checking by installing group
      if (result) {
        result = false;
        cics.ceda().deleteGroup(cedaTerminal, groupName);
        cics.cemt().discardResource(cemtTerminal, resourceType, programA);
        cics.cemt().discardResource(cemtTerminal, resourceType, programB);
        cics.cemt().discardResource(cemtTerminal, resourceType, programC);
        assertThatThrownBy(() -> {
           cics.ceda().installGroup(cedaTerminal, groupName);
        }).isInstanceOf(CedaException.class).hasMessageContaining("Problem determining the result from the CEDA command");

      } else Fail.fail("CEDA Group Install/Delete failed");
   }

}
