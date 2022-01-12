/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.ceci.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Random;

import dev.galasa.AfterClass;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.cicsts.CeciException;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeciResponse;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.ZosBatch;

import org.apache.commons.logging.Log;

@Test
public class CECIManagerIVT {

   @Logger
   public Log logger;

   @CicsRegion
   public ICicsRegion cics;

   @CicsTerminal
   public ICicsTerminal ceciTerminal;
   
   @CicsTerminal
   public ICicsTerminal otherTerminal;

   @ZosBatch(imageTag = "PRIMARY")
   public IZosBatch batch;

   @BeforeClass
   public void login() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException {
	   ceciTerminal.clear().waitForKeyboard();
	   ceciTerminal.type("CECI").enter().waitForKeyboard();
      
       otherTerminal.clear().waitForKeyboard();
   }

   /**
    * Ensures that we have an instance of CECI, CICS and a Terminal.
    * 
    * @throws CicstsManagerException 
    */
   @Test
   public void testNotNull() throws CicstsManagerException  {
      assertThat(logger).isNotNull();
      assertThat(cics).isNotNull();
      assertThat(cics.ceci()).isNotNull();
      assertThat(ceciTerminal).isNotNull();
      assertThat(otherTerminal).isNotNull();
      assertThat(batch).isNotNull();
   } 
   
   /**
    * Defines a variable, then retrieves the variable and tests that the stored text is correct.
    * Then attempts to delete the variable and tests that it is deleted.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    */
  @Test 
   public void testDefineRetrieveAndDeleteVariable() throws CeciException, CicstsManagerException {
	  logger.info("Testing defining, retrieving and deleting a variable using CECI");
      String variableName = "TESTNAME";
      String variableValue = "THIS IS A TEXT STRING";
      
      cics.ceci().defineVariableText(ceciTerminal, variableName, variableValue);
      assertThat(cics.ceci().retrieveVariableText(ceciTerminal, variableName)).isEqualTo(variableValue);

      cics.ceci().deleteVariable(ceciTerminal, variableName);
      assertThatThrownBy(() -> {
    	  cics.ceci().retrieveVariableText(ceciTerminal, variableName);
	  }).isInstanceOf(CeciException.class).hasMessageContaining("Unable to find variable &TESTNAME");
   }
   
   /**
    * Tests that variables defined with a name longer than 10 characters are caught.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    */
  @Test
   public void testLongVariableName() throws CeciException, CicstsManagerException  {
	  logger.info("Testing that defining a variable using CECI with a name longer than 10 characters throws an exception");
      String tenCharacterName = "ABCDEFGHIJ";
      String variableValue = "THIS IS A TEXT STRING";
      
      assertThatThrownBy(() -> {
    	  cics.ceci().defineVariableText(ceciTerminal, tenCharacterName, variableValue);
	  }).isInstanceOf(CeciException.class).hasMessageContaining("CECI variable name \"&ABCDEFGHIJ\" greater than maximum length of 10 characters including the leading \"&\"");
   }

   /**
    * Defines two variables of the same name and checks that the text is overwritten with the second definition.
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
  @Test
   public void testDefineTwoVariablesWithSameName() throws CeciException, CicstsManagerException {
	  logger.info("Testing that when you define two variables with CECI with the same name that the value is overwritten with the second value");
      String variableName = "NOTUNIQUE";
      String value1 = "A VALUE";
      String value2 = "A LONGER VALUE";
      int length = 0;
      length = cics.ceci().defineVariableText(ceciTerminal, variableName, value1);
      assertThat(length).isEqualTo(value1.length());
      assertThat(cics.ceci().retrieveVariableText(ceciTerminal, variableName)).isEqualTo(value1);

      length = cics.ceci().defineVariableText(ceciTerminal, variableName, value2);
      assertThat(length).isEqualTo(value2.length());
      assertThat(cics.ceci().retrieveVariableText(ceciTerminal, variableName)).isEqualTo(value2);
   }

   /**
    * Tests defining a binary variable
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
  @Test
   public void testBinaryDataTypeVariable() throws CeciException, CicstsManagerException {
	  logger.info("Testing defining a binary data type value with CECI");
      String variableName = "BINARY";
      String variableValue = "BinaryString";
      cics.ceci().defineVariableBinary(ceciTerminal, variableName, variableValue.toCharArray());
      String response = new String(cics.ceci().retrieveVariableBinary(ceciTerminal, variableName));
      assertThat(response).isEqualTo(variableValue);

      cics.ceci().deleteVariable(ceciTerminal, variableName);
      logger.info("Testing that when you define a binary data type variable longer than allowed than an exception is thrown");
      assertThatThrownBy(() -> {
    	  cics.ceci().defineVariableBinary(ceciTerminal, variableName, constructRandomString(32768).toCharArray());
	  }).isInstanceOf(CeciException.class).hasMessageContaining("CECI variable value length 32768 greater than maximum 32767");

   }

   /**
    * Tests defining a double data type variable
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
  @Test
   public void testDoubleDataTypeVariable() throws CeciException, CicstsManagerException {
	  logger.info("Testing defining a double data type variable");
      String variableName = "DOUBLE";
      long variableValue = 9223372036854775807L;
      cics.ceci().defineVariableDoubleWord(ceciTerminal, variableName, variableValue);
      long response = cics.ceci().retrieveVariableDoubleWord(ceciTerminal, variableName);
      assertThat(response).isEqualTo(variableValue);
    }

   /**
    * Tests defining a full data type variable
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
  @Test
   public void testFullDataTypeVariable() throws CeciException, CicstsManagerException {
	  logger.info("Testing defining a full data type variable");
      String variableName = "FULL";
      int variableValue = Integer.MAX_VALUE;
      cics.ceci().defineVariableFullWord(ceciTerminal, variableName, variableValue);
      long response = cics.ceci().retrieveVariableFullWord(ceciTerminal, variableName);
      assertThat(response).isEqualTo(variableValue);
   }

   /**
    * Tests defining a half data type variable
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
  @Test
   public void testHalfDataTypeVariable() throws CeciException, CicstsManagerException {
	  logger.info("Testing defining a half data type variable");
      String variableName = "HALF";
      int variableValue = 32766;
      cics.ceci().defineVariableHalfWord(ceciTerminal, variableName, variableValue);
      int response = cics.ceci().retrieveVariableHalfWord(ceciTerminal, variableName);
      assertThat(response).isEqualTo(variableValue);
   }

   /**
    * Tests the execution of a basic CECI command.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    */
   @Test
   public void testBasicCommand() throws CeciException, CicstsManagerException  {
	  logger.info("Testing the execution of a basic CECI command");
      String userVariable = "USERID";
      String ceciCommand = "ASSIGN USERID(&" + userVariable + ")";
      cics.ceci().issueCommand(ceciTerminal, ceciCommand, false);
      String user = cics.ceci().retrieveVariableText(ceciTerminal, userVariable);
      assertThat(user).isEqualTo("CICSUSER");
   }

   /**
    * Tests the execution of a basic documentation CECI command.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    * @throws ZosBatchException 
    */
   @Test
   public void testDocumentationBasicCommand() throws CeciException, CicstsManagerException, ZosBatchException  {
	  logger.info("Testing the execution of a basic documentation CECI command");
	  String message = "GALASA TEST " + Instant.now().toString();
      String ceciCommand = "EXEC CICS WRITE OPERATOR TEXT('" + message + "')";
      ICeciResponse resp = cics.ceci().issueCommand(ceciTerminal, ceciCommand);
      assertThat(resp.isNormal()).isTrue();

      logger.info("Checking that the message was written to the CICS log");
      boolean messageFound = false;
      List<IZosBatchJob> jobs = batch.getJobs(cics.getApplid(), "*");      
      for (IZosBatchJob job : jobs) {
         String output = job.getSpoolFile("JESMSGLG").getRecords();
         if (output.contains(message)) {
            messageFound = true;
            break;
         }
      }
      assertThat(messageFound).isTrue();
   }
   
   /**
    * Writes data to a Temporary Storage Queue, checks that it was written to the queue and then cleans up the queue. 
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    * @throws FieldNotFoundException 
    */
  @Test
   public void testWriteToTSQ() throws CeciException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	  logger.info("Testing writing data to a Temporary Storage Queue");
      String queueName = "QUEUE1";
      String variableName = "TSQDATA";
      String dataToWrite = "THIS IS A GALASA TEST";
      
      cics.ceci().defineVariableText(ceciTerminal, variableName, dataToWrite);
      String command = "WRITEQ TS QUEUE('" + queueName + "') FROM(&" + variableName + ")";
      cics.ceci().issueCommand(ceciTerminal, command);
      
      otherTerminal.type("CEBR " + queueName).enter().waitForKeyboard();
      assertThat(otherTerminal.retrieveScreen()).containsIgnoringCase(dataToWrite);
      otherTerminal.pf3().waitForKeyboard();

      command = "DELETEQ TS QUEUE('" + queueName + "')";
      cics.ceci().issueCommand(ceciTerminal, command);

      otherTerminal.type("CEBR " + queueName).enter().waitForKeyboard();
      assertThat(otherTerminal.retrieveScreen().contains("DOES NOT EXIST")).isTrue();
   }

   /**
    * Test putting and getting data from container and linking a program to a channel.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testPutAndGetDataFromContainer() throws CeciException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException  {
	  // Retrieving the use count of Program 'CONTTEST' to compare with use count after the test case
	  int programUseCountBefore = Integer.parseInt(cics.cemt().inquireResource(otherTerminal, "PROGRAM", "CONTTEST").get("usecount"));
	   
	  otherTerminal.setUppercaseTranslation(false);
	  String channelName = "MY-CHANNEL";
	  String containerName = "HOBBIT";
	  String content = "my-content";
	  
	  ICeciResponse resp = cics.ceci().putContainer(ceciTerminal, channelName, containerName, content, null, null, null);
      assertThat(resp.isNormal()).isTrue();
	  
	  String programName = "CONTTEST";
      resp = cics.ceci().linkProgramWithChannel(ceciTerminal, programName, channelName, null, null, false);
      assertThat(resp.isNormal()).isTrue();
	  
	  String variableName = "&OUTPUT";
      resp = cics.ceci().getContainer(ceciTerminal, channelName, containerName, variableName, null, null);
      assertThat(resp.isNormal()).isTrue();
      assertThat(cics.ceci().retrieveVariableText(ceciTerminal, variableName)).isUpperCase();
      assertThat(cics.ceci().retrieveVariableText(ceciTerminal, variableName)).startsWith(content.toUpperCase());
      
      int programUseCountAfter = Integer.parseInt(cics.cemt().inquireResource(otherTerminal, "PROGRAM", "CONTTEST").get("usecount"));
	  assertThat(programUseCountBefore < programUseCountAfter).isTrue();
   }

   /**
    * Test linking a program to a comm area.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException
    */
   @Test
   public void testLinkToProgramToCommarea() throws CeciException, CicstsManagerException  {
      String variableName = "INPUT";
      String variableValue = "galasa";
      cics.ceci().defineVariableText(ceciTerminal, variableName, variableValue);
     
      ICeciResponse resp = cics.ceci().linkProgram(ceciTerminal, "APITEST", "&" + variableName, null, null, false);
      assertThat(resp.getEIBRESP()).isZero();

      String outputData = cics.ceci().retrieveVariableText(ceciTerminal, "&" + variableName);
      assertThat(outputData).isUpperCase();
      assertThat(outputData).startsWith(variableValue.toUpperCase());
   } 

   private String constructRandomString(int length) {
      String alphabet = "abcdefghijklmnopqrstuvwxyz";
      StringBuilder sb = new StringBuilder();
      Random r = new Random();
      for (int a = 0; a < length; a++){
         sb.append(alphabet.charAt(r.nextInt(26)));
      }
      return sb.toString();
   }


   @AfterClass
      public void afterClass() throws CicstsManagerException {
      cics.ceci().deleteAllVariables(ceciTerminal);
   }
   
}
