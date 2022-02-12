/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.cemt.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.apache.commons.logging.Log;
import org.assertj.core.api.Fail;

import dev.galasa.After;
import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.IResourceString;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.ResourceString;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeciResponse;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.CemtException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zosbatch.ZosBatchException;

@Test
public class CEMTManagerIVT {
	
   @Logger
   public Log logger;
	
   @CicsRegion
   public ICicsRegion cics;
   
   @CicsTerminal
   public ICicsTerminal cemtTerminal;
   
   @CicsTerminal
   public ICicsTerminal resourceTerminal;
   
   @CicsTerminal
   public ICicsTerminal manualTestTerminal;
   
   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "PROG", required = false)
   public String providedResourceString1;
   @ResourceString(tag = "PROG", length = 8)
   public IResourceString resourceString1;
   
   public String programName;
 
   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "TRX", required = false)
   public String providedResourceString2;
   @ResourceString(tag = "TRX", length = 4)
   public IResourceString resourceString2;
   
   public String trxName;
   
   @TestProperty(prefix = "IVT.RESOURCE.STRING", suffix = "GROUP", required = false)
   public String providedResourceString3;
   @ResourceString(tag = "GROUP", length = 8)
   public IResourceString resourceString3;
   
   public String groupName;
   
 
   
   @BeforeClass
   public void login() throws Exception {
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
      
      resourceTerminal.clear();
      resourceTerminal.waitForKeyboard(); 
      
      manualTestTerminal.clear();
      manualTestTerminal.waitForKeyboard();
      
      // Get and set unique resource strings
      if (providedResourceString1 != null) {
    	  programName = providedResourceString1;
      } else {
    	  programName = resourceString1.getString();
      }
      logger.info("Unique Program name to be used in the tests: " + programName);

      if (providedResourceString2 != null) {
    	  trxName = providedResourceString2;
      } else {
    	  trxName = resourceString2.getString();
      }
      logger.info("Unique Transaction name to be used in the tests: " + trxName);
      
      if (providedResourceString3 != null) {
    	  groupName = providedResourceString3;
      } else {
    	  groupName = resourceString3.getString();
      }
      logger.info("Unique Group name to be used in the tests: " + groupName);
   }
   
   @Before
   public void before() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException  {
	  installResources();
   }
   
   @After
   public void after() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  clearResources();
   }
   
   @Test
   public void testCEMTIsNotNull() throws CicstsManagerException {
      assertThat(cics).isNotNull();
      assertThat(cics.cemt()).isNotNull();
      assertThat(cemtTerminal).isNotNull();
      assertThat(resourceTerminal).isNotNull();
      assertThat(manualTestTerminal).isNotNull();
   }
   
   /**
    * Tests that the inquire resource method retrieves the correct fields
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
   public void testInquireResource() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  logger.info("Testing the inquire resource method on Program " + programName);
      CicstsHashMap resource = cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName);
      assertThat(resource.isParameterEquals("program", programName)).isTrue();
      assertThat(resource.isParameterEquals("length", "0000000000")).isTrue();
      assertThat(resource.isParameterEquals("language", "Notdefined")).isTrue();
      assertThat(resource.isParameterEquals("progtype", "Program")).isTrue();
      assertThat(resource.isParameterEquals("status", "Enabled")).isTrue();
      assertThat(resource.isParameterEquals("sharestatus", "Private")).isTrue();
      
      logger.info("Manually testing the CEMT INQUIRE command on Program " + programName);
	  assertThat(manualTestUsingTerminal("CEMT INQUIRE PROGRAM(" + programName + ")", "RESPONSE: NORMAL")).isTrue();
   }
   
   /**
    * Tests that the inquire resource method retrieves nothing for a resource that doesn't exist
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
   public void testDiscardAndInquireResourceThatDoesntExist() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  logger.info("Testing the discard resource method on Program " + programName + ", then the inquire resource method when it no longer exists");
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName)).isNotNull();
      
      cics.cemt().discardResource(cemtTerminal, "PROGRAM", programName);
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName)).isNull();  
      
      logger.info("Manually testing the CEMT INQUIRE command retrieves nothing for Program " + programName);
      assertThat(manualTestUsingTerminal("CEMT INQUIRE PROGRAM(" + programName + ")", "RESPONSE: 1 ERROR")).isTrue();
   }
   
   /**
    * Tests that the inquire resource method using an invalid resource type throws an exception
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
   public void testInquireInvalidResourceType() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
      logger.info("Testing the inquire resource method on invalid resource type 'Fish'");
      assertThatThrownBy(() -> {
         cics.cemt().inquireResource(cemtTerminal, "FISH", "example");
      }).isInstanceOf(CemtException.class).hasMessageContaining("Problem with starting CEMT transaction"); 
      
      logger.info("Manually testing that the CEMT INQUIRE command throws an exception when inquiring on resource type 'Fish'");
      assertThat(manualTestUsingTerminal("CEMT INQUIRE FISH(EXAMPLE)", "E 'FISH' is not valid and is ignored")).isTrue();
   }
   
   /**
    * Tests that the inquire resource method using an invalid resource name returns nothing, it should not throw an exception
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
   public void testInquireInvalidResourceName() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  logger.info("Testing the inquire resource method on a Program resource with an invalid resource name '!!%^'");
      assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "!!%^")).isNull(); 
      
      logger.info("Manually testing that the CEMT INQUIRE command using an invalid resource name retrieves nothing");
      assertThat(manualTestUsingTerminal("CEMT INQUIRE PROGRAM(!!%^)", "RESPONSE: 1 ERROR")).isTrue();
   }
   
   /**
    * Tests discarding a resource that doesn't exist throws an exception
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testDiscardResourceThatDoesntExist() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, CicstsManagerException  {
	  logger.info("Testing the discard resource method on Program 'Nonex' that doesn't exist");
      assertThatThrownBy(() -> {
         cics.cemt().discardResource(cemtTerminal, "PROGRAM", "NONEX");
      }).isInstanceOf(CemtException.class).hasMessageContaining("Problem determining the result from the CEMT command");
      
      logger.info("Manually testing that the CEMT DISCARD command on a resource that doesn't exist returns an error message");
      assertThat(manualTestUsingTerminal("CEMT DISCARD PROGRAM(NONEX)", "RESPONSE: 1 ERROR")).isTrue();
   }

   /**
    * Tests setting fields in a resource
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
   public void testSetResource() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  logger.info("Testing the set resource method on Program " + programName);
      CicstsHashMap resource = cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName);

      if (resource.get("status").equals("Disabled")) {
         cics.cemt().setResource(cemtTerminal, "PROGRAM", programName, "ENABLED");
         resource = cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName);
         resource.checkParameterEquals("status", "Enabled");
         assertThat(manualTestUsingTerminal("CEMT INQUIRE PROGRAM(" + programName + ") ENABLED", "RESPONSE: NORMAL")).isTrue();
         assertThat(manualTestUsingTerminal("CEMT INQUIRE PROGRAM(" + programName + ") DISABLED", "RESPONSE: 1 ERROR")).isTrue();
      } else {
         cics.cemt().setResource(cemtTerminal, "PROGRAM", programName, "DISABLED");
         resource = cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName);
         resource.checkParameterEquals("status", "Disabled");
         assertThat(manualTestUsingTerminal("CEMT INQUIRE PROGRAM(" + programName + ") DISABLED", "RESPONSE: NORMAL")).isTrue();
         assertThat(manualTestUsingTerminal("CEMT INQUIRE PROGRAM(" + programName + ") ENABLED", "RESPONSE: 1 ERROR")).isTrue();
      }
          
   }
   
   /**
    * Tests setting fields in a resource that doesn't exist
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
   public void testSetResourceThatDoesntExist() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing the set resource method on Program 'Nonex' that doesn't exist ");
	   assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", "NONEX")).isNull();
	   
	   assertThatThrownBy(() -> {
	      cics.cemt().setResource(cemtTerminal, "PROGRAM", "NONEX", "ENABLED");
	   }).isInstanceOf(CemtException.class).hasMessageContaining("Problem determining the result from the CEMT command");
	   
	   logger.info("Manually testing that the CEMT SET command on Program 'Nonex' has error message");
	   assertThat(manualTestUsingTerminal("CEMT SET PROGRAM(NONEX) DISABLED", "RESPONSE: 1 ERROR")).isTrue();
   }
   
   /**
    * Tests setting fields in a resource to invalid values
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
   public void testSetResourceToInvalidValues() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing the set resource method on Program " + programName + " using invalid values");
	   // Check the Program exists first
	   assertThat(cics.cemt().inquireResource(cemtTerminal, "PROGRAM", programName)).isNotNull();
	   
	   assertThatThrownBy(() -> {
	      cics.cemt().setResource(cemtTerminal, "PROGRAM", programName, "FOO");
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
	  logger.info("Testing the inquire resource method on Transaction " + trxName);
      installTransactionResource();
      assertThat(cics.cemt().inquireResource(cemtTerminal, "TRANSACTION", trxName)).isNotNull();

      clearTransactionResource();
      assertThat(cics.cemt().inquireResource(cemtTerminal, "TRANSACTION", trxName)).isNull();

      logger.info("Manually testing the CEMT INQUIRE command on Transaction " + trxName + " after deletion");
      assertThat(manualTestUsingTerminal("CEMT INQUIRE TRANSACTION(" + trxName + ")", "RESPONSE: 1 ERROR")).isTrue();
   }
   
   /**
    * Tests inquiring a transaction resource with an invalid name returns nothing, it should not throw an exception
    * 
    * @throws CicstsManagerException 
    * @throws CemtException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testInquireTransactionWithInvalidName() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing the inquire resource method on Transaction with invalid name '!$%^'");
	   assertThat(cics.cemt().inquireResource(cemtTerminal, "TRANSACTION", "!$%^")).isNull();
   }
   
   /**
    * Tests performing a system property
    * 
    * @throws CemtException 
    * @throws CicstsManagerException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    * @throws ZosBatchException 
    */
   @Test
   public void testPerformSystemProperty() throws CemtException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, ZosBatchException {
	  logger.info("Testing the perform system property method with the CEMT PERFORM RESET command");
	   
	  // To manually test this later, this test will check the JESMSGLG for the DFHIC0801 message
	  // This message is logged every night at midnight, so to ensure we are searching for the DFHIC0801 message generated by this test,
	  // CECI is logging a timestamped message to the JESMSGLG now and will search only the section of the JESMSGLG after it
	  resourceTerminal.resetAndClear().waitForKeyboard().type("CECI").enter().waitForKeyboard();
	  String message = "ABOUT TO DO CEMT PERFORM RESET " + Instant.now();
      String command = "EXEC CICS WRITE OPERATOR TEXT('" + message + "')";
	  ICeciResponse resp = cics.ceci().issueCommand(resourceTerminal, command);
	  assertThat(resp.isNormal()).isTrue();
	      
	  assertThat(cics.cemt().performSystemProperty(cemtTerminal, "RESET", "", "RESPONSE: NORMAL")).isTrue();

	  logger.info("Checking that the CEMT PERFORM RESET command worked by looking in the CICS logs");

      String jesMsgLg = cics.getRegionJob().getSpoolFile("JESMSGLG").getRecords();
      if (jesMsgLg.contains(message)) {
    	  jesMsgLg = jesMsgLg.substring(jesMsgLg.lastIndexOf(message));    	  
    	  assertThat(jesMsgLg.contains("DFHIC0801")).isTrue();	  
      } else Fail.fail("Timestamped message was not written to the JESMSGLG");
	  
   }
   
   private boolean manualTestUsingTerminal(String command, String expectedString) throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, CicstsManagerException {
       manualTestTerminal.resetAndClear();
	   manualTestTerminal.type(command).enter().waitForKeyboard(); 
	   if (manualTestTerminal.retrieveScreen().contains(expectedString)) {
		   return true;
	   } else {
		   return false;
	   }
   }
   
   private void installResources() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Now defining Program " + programName + " into the CICS Region");
	   resourceTerminal.type("CEDA DEFINE PROGRAM(" + programName + ") GROUP(" + groupName + ")").enter().waitForKeyboard();
	   resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
	   logger.info("Now installing Program " + programName + " into the CICS Region");
	   resourceTerminal.type("CEDA INSTALL PROGRAM(" + programName + ") GROUP(" + groupName + ")").enter().waitForKeyboard();
	   resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();  
   }
   
   private void clearResources() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Now discarding Program " + programName + " from the CICS Region");
	   resourceTerminal.type("CEMT DISCARD PROGRAM(" + programName + ")").enter().waitForKeyboard();
	   resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
	   logger.info("Now deleting Program " + programName + " from the CICS Region");
	   resourceTerminal.type("CEDA DELETE PROGRAM(" + programName + ") GROUP(" + groupName + ")").enter().waitForKeyboard();
	   resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();   
   }

   private void installTransactionResource() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  logger.info("Now defining Transaction " + trxName + " into the CICS Region");
      resourceTerminal.type("CEDA DEFINE TRANSACTION(" + trxName + ") GROUP(" + groupName + ") PROGRAM(EX1)").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
	  logger.info("Now installing Transaction " + trxName + " into the CICS Region");
      resourceTerminal.type("CEDA INSTALL TRANSACTION(" + trxName + ") GROUP(" + groupName + ")").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }

   private void clearTransactionResource() throws FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, CemtException, CicstsManagerException {
	  logger.info("Now discarding Transaction " + trxName + " from the CICS Region");
	  resourceTerminal.type("CEMT DISCARD TRANSACTION(" + trxName + ")").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      logger.info("Now deleting Transaction " + trxName + " from the CICS Region");
      resourceTerminal.type("CEDA DELETE TRANSACTION(" + trxName + ") GROUP(" + groupName + ")").enter().waitForKeyboard();
      resourceTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
   }
   
}
