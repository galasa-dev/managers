/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.ceci.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

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
   public ICicsTerminal terminal;

   @ZosBatch(imageTag = "PRIMARY")
   public IZosBatch batch;

   @BeforeClass
   public void login() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException {
      terminal.clear();
      terminal.waitForKeyboard();
      terminal.type("CECI").enter().waitForKeyboard();
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
      assertThat(terminal).isNotNull();
      assertThat(batch).isNotNull();
   }
   
   /**
    * Tests that variables defined with a name longer than 10 characters are caught.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    */
   @Test
   public void testLongVariableName() throws CeciException, CicstsManagerException  {
      String nineCharacterName = "ABCDEFGHI";
      String tenCharacterName = "ABCDEFGHIJ";
      String variableText = "THIS IS A TEXT STRING";

      int length = cics.ceci().defineVariableText(terminal, nineCharacterName, variableText);
      assertThat(length).isEqualTo(variableText.length());

      boolean exceptionCaught = false;
      try {
         cics.ceci().defineVariableText(terminal, tenCharacterName, variableText);
      } catch (CeciException ce) {
         exceptionCaught = true;
         assertThat(ce.getMessage()).isEqualTo("CECI variable name \"&ABCDEFGHIJ\" greater than maximum length of 10 characters including the leading \"&\"");
      }
      assertThat(exceptionCaught).isTrue();
   }

   /**
    * Defines a variable, then deletes the variable, and tests that it is deleted.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    */
   @Test
   public void testDeleteVariable() throws CeciException, CicstsManagerException  {
      String variableName = "TESTNAME";
      String variableValue = "THIS IS A TEXT STRING";

      cics.ceci().defineVariableText(terminal, variableName, variableValue);
      cics.ceci().deleteVariable(terminal, variableName);
      boolean exceptionCaught = false;
      try {
         cics.ceci().retrieveVariableText(terminal, variableName);
      } catch (CeciException ce){
         exceptionCaught = true;
         assertThat(ce.getMessage()).isEqualTo("Unable to find variable &TESTNAME");
      }
      assertThat(exceptionCaught).isTrue();
   }
   
   /**
    * Defines a variable, then retrieves the variable and tests that the stored text is correct.
    * Then attempts to delete the variable and tests that it is deleted.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    */
   @Test 
   public void testDefineVariable() throws CeciException, CicstsManagerException {
      String variableName = "TESTNAME";
      String variableText = "THIS IS A TEXT STRING";
      cics.ceci().defineVariableText(terminal, variableName, variableText);
      assertThat(cics.ceci().retrieveVariableText(terminal, variableName)).isEqualTo(variableText);

      cics.ceci().deleteVariable(terminal, variableName);
      boolean exceptionCaught = false;
      try {
         cics.ceci().retrieveVariableText(terminal, variableName);
      } catch (CeciException ce) {
         exceptionCaught = true;
         assertThat(ce.getMessage()).isEqualTo("Unable to find variable &TESTNAME");
      }
      assertThat(exceptionCaught).isTrue();
   }

   /**
    * Defines two variables of the same name and checks that the text is overwritten with the second definition.
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testDefineTwoVariablesWithSameName() throws CeciException, CicstsManagerException {
      String variableName = "NOTUNIQUE";
      String value1 = "A value";
      String value2 = "A longer value";
      int length = 0;
      length = cics.ceci().defineVariableText(terminal, variableName, value1);
      assertThat(length).isEqualTo(value1.length());

      length = cics.ceci().defineVariableText(terminal, variableName, value2);
      assertThat(length).isEqualTo(value2.length());
      
      assertThat(cics.ceci().retrieveVariableText(terminal, variableName).equals(value2));
   }

   /**
    * Tests defining a binary variable
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testBinaryDataTypeVariable() throws CeciException, CicstsManagerException {
      String variableName = "BINARY";
      String variableValue = "BinaryString";
      cics.ceci().defineVariableBinary(terminal, variableName, variableValue.toCharArray());
      String response = new String(cics.ceci().retrieveVariableBinary(terminal, variableName));
      assertThat(response).isEqualTo(variableValue);

      // Define a variable larger than allowed
      cics.ceci().deleteVariable(terminal, variableName);
      boolean exceptionCaught = false;
      try {
         cics.ceci().defineVariableBinary(terminal, variableName, constructRandomString(32768).toCharArray());
      } catch (CeciException ce){
         exceptionCaught = true;
          assertThat(ce.getMessage()).contains("CECI variable value length 32768 greater than maximum 32767");
      }
      assertThat(exceptionCaught).isTrue();
   }

   /**
    * Tests defining a double data type variable
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testDoubleDataTypeVariable() throws CeciException, CicstsManagerException {
      String variableName = "DOUBLE";
      long variableValue = 9223372036854775807L;
      cics.ceci().defineVariableDoubleWord(terminal, variableName, variableValue);
      long response = cics.ceci().retrieveVariableDoubleWord(terminal, variableName);
      assertThat(response).isEqualTo(variableValue);
    }

   /**
    * Tests defining a full data type variable
    * 
    * @throws CeciException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testFullDataTypeVariable() throws CeciException, CicstsManagerException{
      String variableName = "FULL";
      int variableValue = Integer.MAX_VALUE;
      cics.ceci().defineVariableFullWord(terminal, variableName, variableValue);
      long response = cics.ceci().retrieveVariableFullWord(terminal, variableName);
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
      String variableName = "HALF";
      int variableValue = 32766;
      cics.ceci().defineVariableHalfWord(terminal, variableName, variableValue);
      int response = cics.ceci().retrieveVariableHalfWord(terminal, variableName);
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
      String userVariable = "USERID";
      String ceciCommand = "ASSIGN USERID(&" + userVariable + ")";

      ICeciResponse resp = cics.ceci().issueCommand(terminal, ceciCommand, false);
      String user = cics.ceci().retrieveVariableText(terminal, userVariable);
      logger.info("Retrieved user was: " + user);
      logger.info("Response from command was: " + resp.getResponse());
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
	  String message = "GALASA TEST " + Instant.now().toString();
      String ceciCommand = "EXEC CICS WRITE OPERATOR TEXT('" + message + "')";
      
      ICeciResponse resp = cics.ceci().issueCommand(terminal, ceciCommand);
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
    * * Writes data to a Temporary Storage Queue, checks that it was written to the queue and then cleans up the queue. 
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
   public void testWriteToTSQ() throws CeciException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException  {
      String queueName = "QUEUE1";
      String variableName = "TSQDATA";
      String dataToWrite = "THIS IS A GALASA TEST";

      cics.ceci().defineVariableText(terminal, variableName, dataToWrite);
      String command = "WRITEQ TS QUEUE('" + queueName + "') FROM(&" + variableName + ")";
      cics.ceci().issueCommand(terminal, command);

      terminal.resetAndClear();
      terminal.type("CEBR " + queueName).enter().waitForKeyboard();
      assertThat(terminal.retrieveScreen()).containsIgnoringCase(dataToWrite);
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
      // Log back into CECI session
      logger.info("Logging back into CECI session");
      terminal.type("CECI").enter().waitForKeyboard();
      command = "DELETEQ TS QUEUE('" + queueName + "')";
      cics.ceci().issueCommand(terminal, command);

      terminal.resetAndClear();
      terminal.type("CEBR " + queueName).enter().waitForKeyboard();
      assertThat(terminal.retrieveScreen()).contains("DOES NOT EXIST");
      terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
      // Log back into CECI session for next test
      logger.info("Logging back into CECI session");
      terminal.type("CECI").enter().waitForKeyboard();
   }

   /**
    * Test link to program channel.
    * 
    * @throws CicstsManagerException 
    * @throws CeciException 
    */
   @Test
   public void linkToProgramChannel() throws CeciException, CicstsManagerException  {
	  String variableName = "INPUT";
      String variableValue = constructRandomString(25000);
      
      cics.ceci().defineVariableText(terminal, variableName, variableValue);
      cics.ceci().issueCommand(terminal, "PUT CONTAINER(HOBBIT) FROM(&input) CHANNEL(HOBBITCHAN)");
      cics.ceci().issueCommand(terminal, "LINK PROGRAM(CONTTEST) CHANNEL(HOBBITCHAN)");
      cics.ceci().issueCommand(terminal, "GET CONTAINER(HOBBIT) INTO(&output) CHANNEL(HOBBITCHAN)");
      
      String outputData = cics.ceci().retrieveVariableText(terminal, "&OUTPUT");
      assertThat(outputData).isUpperCase();
      assertThat(outputData).isEqualToIgnoringCase(variableValue);
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
      cics.ceci().deleteAllVariables(terminal);
   }
   
}
