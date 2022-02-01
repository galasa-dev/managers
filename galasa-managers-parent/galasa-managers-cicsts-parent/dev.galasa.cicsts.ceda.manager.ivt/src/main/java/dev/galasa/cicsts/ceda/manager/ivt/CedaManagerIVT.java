/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.ceda.manager.ivt;

import static org.assertj.core.api.Assertions.*;

import org.apache.commons.logging.Log;

import dev.galasa.After;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class CedaManagerIVT {

   @CicsRegion()
   public ICicsRegion cics;

   @CicsTerminal()
   public ICicsTerminal cedaTerminal;

   @CicsTerminal()
   public ICicsTerminal cemtTerminal;

   @CicsTerminal()
   public ICicsTerminal terminal;
   
   @Logger
   public Log logger;
   
   @BeforeClass
   public void login() throws Exception {
      cedaTerminal.clear();
      cedaTerminal.waitForKeyboard();
      
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
   }

   @Test
   public void checkCECINotNull() throws CicstsManagerException {
      assertThat(cics).isNotNull();
      assertThat(cics.ceda()).isNotNull();
      assertThat(cics.cemt()).isNotNull();
      assertThat(cemtTerminal).isNotNull();
      assertThat(cedaTerminal).isNotNull();
      assertThat(terminal).isNotNull();
      assertThat(logger).isNotNull();
   }
   
   /**
    * Creating and installing a Program resource with CEDA
    * 
    * @throws CicstsManagerException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testResourceProgram() throws CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException  {
      String resourceType = "PROGRAM";
      String resourceName = "Program";
      String groupName = "Test";
      String resourceParameters = null;

      logger.info("Creating and installing a program resource using CEDA, and testing that it worked using CEMT. Finally, deleting the program resource.");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);

      cics.ceda().createResource(cedaTerminal, resourceType, resourceName, groupName, resourceParameters);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(true);

      cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName).get("program")).isEqualToIgnoringCase(resourceName);
      
      logger.info("Manually testing that the resource was installed using CEMT");
      terminal.type("CEMT INQUIRE PROGRAM(PROGRAM)").enter().waitForKeyboard();
      assertThat(terminal.retrieveScreen().contains("RESPONSE: NORMAL")).isTrue();
      
      cics.ceda().deleteResource(cedaTerminal, resourceType, resourceName, groupName);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);
   }

   /**
    * Creating and installing a Transaction resource with CEDA
    * 
    * @throws CicstsManagerException 
    */
   @Test
   public void testResourceTransaction() throws CicstsManagerException{
      String resourceType = "TRANSACTION";
      String resourceName = "trx1";
      String groupName = "Test";
      String resourceParameters = "PROGRAM(PRG1)";
     
      logger.info("Creating and installing a transaction resource using CEDA, and testing that it worked using CEMT. Finally, deleting the transaction resource.");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);

      cics.ceda().createResource(cedaTerminal, resourceType, resourceName, groupName, resourceParameters);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(true);

      cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName).get("transaction")).isEqualToIgnoringCase(resourceName);
      
      cics.ceda().deleteResource(cedaTerminal, resourceType, resourceName, groupName);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);
   }

   /**
    * Creating and installing a Library resource with CEDA
    * 
    * @throws CicstsManagerException 
    */
   @Test
   public void testResourceLibrary() throws CicstsManagerException {
      String resourceType = "LIBRARY";
      String resourceName = "lib1";
      String groupName = "Test";
      String resourceParameters = "DSNAME01(CTS.USER.APPL1.CICS.LOAD)";
     
      logger.info("Creating and installing a library resource using CEDA, and testing that it worked by using CEMT. Finally, deleting the library resource.");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);

      cics.ceda().createResource(cedaTerminal, resourceType, resourceName, groupName, resourceParameters);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(true);

      cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName).get("library")).isEqualToIgnoringCase(resourceName);

      cics.ceda().deleteResource(cedaTerminal, resourceType, resourceName, groupName);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);
   }

   /**
    * Creating and installing resources in different groups with CEDA
    * Deleting a group with CEDA
    * 
    * @throws CedaException 
    * @throws CicstsManagerException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testGroup() throws CedaException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
      String resourceType = "prog";
      String resourceName = "prg1";
      String resourceName2 = "prg2";
      String resourceName3 = "prg3";
      String resourceName4 = "prg4";
      String groupName = "IVT";
      String groupName2 = "noIVT";
      
      logger.info("Creating resources in group 1");
      cics.ceda().createResource(cedaTerminal, resourceType, resourceName, groupName, null);
      cics.ceda().createResource(cedaTerminal, resourceType, resourceName2, groupName, null);
      cics.ceda().createResource(cedaTerminal, resourceType, resourceName3, groupName, null);
      
      logger.info("Creating resources in group 2");
      cics.ceda().createResource(cedaTerminal, resourceType, resourceName4, groupName2, null);
     
      logger.info("Installing only group 1. Testing that group 1 appears in CEMT and group 2 does not");
      cics.ceda().installGroup(cedaTerminal, groupName);     
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)).isNotNull();
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName2)).isNotNull();
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName3)).isNotNull();
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName4)).isNull();

      logger.info("Testing CEDA group delete on group 1");
      cics.ceda().deleteGroup(cedaTerminal, groupName);
      
      logger.info("Manually testing that group delete worked using CEDA EXPAND. Should state that the group is not found");
      cedaTerminal.type("CEDA EXPAND GROUP(" + groupName + ")").enter().waitForKeyboard();
      assertThat(cedaTerminal.retrieveScreen().contains("Group " + groupName + " not found")).isTrue();
      
      // Installing group 1 after deleting it should throw an exception
      assertThatThrownBy(() -> {
         cics.ceda().installGroup(cedaTerminal, groupName);
      }).isInstanceOf(CedaException.class).hasMessageContaining("Problem determining the result from the CEDA command");
   }
   
   @After
   public void cleanUpResources() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  logger.info("Clearing all resources after the test");
	  terminal.clear().waitForKeyboard();
      terminal.type("CEDA DELETE GROUP(Test) ALL").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEDA DELETE GROUP(IVT) ALL").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEDA DELETE GROUP(noIVT) ALL").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEMT DISCARD prog(Program,prg1,prg2,prg3,prg4)").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEMT DISCARD transaction(trx1)").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      terminal.type("CEMT DISCARD LIBRARY(lib1)").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }
}