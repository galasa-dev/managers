/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.ceda.manager.ivt;

import static org.assertj.core.api.Assertions.*;

import org.apache.commons.logging.Log;

import dev.galasa.After;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.IResourceString;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.ResourceString;
import dev.galasa.core.manager.TestProperty;
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
   
   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "PROG1", required = false)
   public String providedResourceString1;
   @ResourceString(tag = "PROG1", length = 8)
   public IResourceString resourceString1;
   public String programName1;

   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "PROG2", required = false)
   public String providedResourceString2;
   @ResourceString(tag = "PROG2", length = 8)
   public IResourceString resourceString2;
   public String programName2;
   
   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "TRX", required = false)
   public String providedResourceString3;
   @ResourceString(tag = "TRX", length = 4)
   public IResourceString resourceString3;
   public String trxName;
   
   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "LIB", required = false)
   public String providedResourceString4;
   @ResourceString(tag = "LIB", length = 4)
   public IResourceString resourceString4;
   public String libName;
   
   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "GROUP1", required = false)
   public String providedResourceString5;
   @ResourceString(tag = "GROUP1", length = 8)
   public IResourceString resourceString5;
   public String groupName1;
   
   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "GROUP2", required = false)
   public String providedResourceString6;
   @ResourceString(tag = "GROUP2", length = 8)
   public IResourceString resourceString6;
   public String groupName2;
   
   @BeforeClass
   public void login() throws Exception {
      cedaTerminal.clear();
      cedaTerminal.waitForKeyboard();
      
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
      
   // Get and set unique resource strings
      if (providedResourceString1 != null) {
    	  programName1 = providedResourceString1;
      } else {
    	  programName1 = resourceString1.getString();
      }
      logger.info("Unique Program name 1 to be used in the tests: " + programName1);

      if (providedResourceString2 != null) {
    	  programName2 = providedResourceString2;
      } else {
    	  programName2 = resourceString2.getString();
      }
      logger.info("Unique Program name 2 to be used in the tests: " + programName2);
      
      if (providedResourceString3 != null) {
    	  trxName = providedResourceString3;
      } else {
    	  trxName = resourceString3.getString();
      }
      logger.info("Unique Transaction name to be used in the tests: " + trxName);

      if (providedResourceString4 != null) {
    	  libName = providedResourceString4;
      } else {
    	  libName = resourceString4.getString();
      }
      logger.info("Unique Library name to be used in the tests: " + libName);
      
      if (providedResourceString5 != null) {
    	  groupName1 = providedResourceString5;
      } else {
    	  groupName1 = resourceString5.getString();
      }
      logger.info("Unique Group name 1 to be used in the tests: " + groupName1);
      
      if (providedResourceString6 != null) {
    	  groupName2 = providedResourceString6;
      } else {
    	  groupName2 = resourceString6.getString();
      }
      logger.info("Unique Group name 2 to be used in the tests: " + groupName2);
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
    * Creating, installing and deleting a Program resource with CEDA
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

      logger.info("Now checking that the Program " + programName1 + " does not already exist");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, programName1, groupName1)).isEqualTo(false);

      logger.info("Now creating the Program " + programName1 + " using CEDA, and checking that it does exist");
      cics.ceda().createResource(cedaTerminal, resourceType, programName1, groupName1, null);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, programName1, groupName1)).isEqualTo(true);

      logger.info("Now installing the Program " + programName1 + " into the CICS using CEDA, and inquiring on it using CEMT");
      cics.ceda().installResource(cedaTerminal, resourceType, programName1, groupName1);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, programName1).get("program")).isEqualToIgnoringCase(programName1);
      
      logger.info("Manually testing that Program " + programName1 + " was installed using CEMT");
      terminal.type("CEMT INQUIRE PROGRAM(" + programName1 + ")").enter().waitForKeyboard();
      assertThat(terminal.retrieveScreen().contains("RESPONSE: NORMAL")).isTrue();
      
      logger.info("Now deleting the Program " + programName1 + " using CEDA, and checking that it does not exist");
      cics.ceda().deleteResource(cedaTerminal, resourceType, programName1, groupName1);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, programName1, groupName1)).isEqualTo(false);
   }

   /**
    * Creating, installing and deleting a Transaction resource with CEDA
    * 
    * @throws CicstsManagerException 
    */
   @Test
   public void testResourceTransaction() throws CicstsManagerException{
      String resourceType = "TRANSACTION";
      String resourceParameters = "PROGRAM(" + programName1 + ")";
     
      logger.info("Now checking that the Transaction " + trxName + " does not already exist");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, trxName, groupName1)).isEqualTo(false);

      logger.info("Now creating the Transaction " + trxName + " using CEDA, and checking that it does exist");
      cics.ceda().createResource(cedaTerminal, resourceType, trxName, groupName1, resourceParameters);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, trxName, groupName1)).isEqualTo(true);

      logger.info("Now installing the Transaction " + trxName + " into the CICS using CEDA, and inquiring on it using CEMT");
      cics.ceda().installResource(cedaTerminal, resourceType, trxName, groupName1);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, trxName).get("transaction")).isEqualToIgnoringCase(trxName);
      
      logger.info("Now deleting the Transaction " + trxName + " using CEDA, and checking that it does not exist");
      cics.ceda().deleteResource(cedaTerminal, resourceType, trxName, groupName1);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, trxName, groupName1)).isEqualTo(false);
   }

   /**
    * Creating, installing and deleting a Library resource with CEDA
    * 
    * @throws CicstsManagerException 
    */
   @Test
   public void testResourceLibrary() throws CicstsManagerException {
      String resourceType = "LIBRARY";
      String resourceParameters = "DSNAME01(CTS.USER.APPL1.CICS.LOAD)";
     
      logger.info("Now checking that the Library " + libName + " does not already exist");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, libName, groupName1)).isEqualTo(false);

      logger.info("Now creating the Library " + libName + " using CEDA, and checking that it does exist");
      cics.ceda().createResource(cedaTerminal, resourceType, libName, groupName1, resourceParameters);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, libName, groupName1)).isEqualTo(true);

      logger.info("Now installing the Library " + libName + " into the CICS using CEDA, and inquiring on it using CEMT");
      cics.ceda().installResource(cedaTerminal, resourceType, libName, groupName1);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, libName).get("library")).isEqualToIgnoringCase(libName);

      logger.info("Now deleting the Library " + libName + " using CEDA, and checking that it does not exist");
      cics.ceda().deleteResource(cedaTerminal, resourceType, libName, groupName1);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, libName, groupName1)).isEqualTo(false);
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
      String resourceType = "PROGRAM";
      
      logger.info("Creating resources in the first Group " + groupName1);
      cics.ceda().createResource(cedaTerminal, resourceType, programName1, groupName1, null);
      
      logger.info("Creating resources in the second Group " + groupName2);
      cics.ceda().createResource(cedaTerminal, resourceType, programName2, groupName2, null);
     
      logger.info("Installing only Group " + groupName1 + ". Testing that Group " + groupName1 + " appears in CEMT and Group " + groupName2 + " does not");
      cics.ceda().installGroup(cedaTerminal, groupName1);     
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, programName1)).isNotNull();
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, programName2)).isNull();

      logger.info("Testing CEDA group delete on Group " + groupName1);
      cics.ceda().deleteGroup(cedaTerminal, groupName1);
      
      logger.info("Manually testing that group delete worked using CEDA EXPAND. Should state that the Group " + groupName1 + " is not found");
      cedaTerminal.type("CEDA EXPAND GROUP(" + groupName1 + ")").enter().waitForKeyboard();
      assertThat(cedaTerminal.retrieveScreen().contains("Group " + groupName1 + " not found")).isTrue();
      
      // Installing a Group after deleting it should throw an exception
      assertThatThrownBy(() -> {
         cics.ceda().installGroup(cedaTerminal, groupName1);
      }).isInstanceOf(CedaException.class).hasMessageContaining("Problem determining the result from the CEDA command");
   }
   
   @After
   public void cleanUpResources() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  terminal.clear().waitForKeyboard();
	  
	  logger.info("Deleting all resources in Group " + groupName1 + " from the CICS Region");
      terminal.type("CEDA DELETE GROUP(" + groupName1 + ") ALL").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
	  logger.info("Deleting all resources in Group " + groupName2 + " from the CICS Region");
      terminal.type("CEDA DELETE GROUP(" + groupName2 + ") ALL").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
      logger.info("Discarding Program " + programName1 + " from the CICS Region");
      terminal.type("CEMT DISCARD prog(" + programName1 + ")").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
      logger.info("Discarding Program " + programName2 + " from the CICS Region");
      terminal.type("CEMT DISCARD prog(" + programName2 + ")").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
      logger.info("Discarding Transaction " + trxName + " from the CICS Region");
      terminal.type("CEMT DISCARD transaction(" + trxName + ")").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
      logger.info("Discarding Library " + libName + " from the CICS Region");
      terminal.type("CEMT DISCARD LIBRARY(" + libName + ")").enter().waitForKeyboard();
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }
}