/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.cicsts.ceci.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

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
    public ICicsTerminal cebrTerminal;
	
	@BeforeClass
	public void login() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException {
		ceciTerminal.clear();
		ceciTerminal.waitForKeyboard();
		ceciTerminal.type("CECI").enter().waitForKeyboard();
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

        int length = cics.ceci().defineVariableText(ceciTerminal, nineCharacterName, variableText);
        assertThat(length).isEqualTo(variableText.length());
        
        boolean exceptionCaught = false;
        try {
            cics.ceci().defineVariableText(ceciTerminal, tenCharacterName, variableText);
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
        
        cics.ceci().defineVariableText(ceciTerminal, variableName, variableValue);
        cics.ceci().deleteVariable(ceciTerminal, variableName);
        boolean exceptionCaught = false;
        try {
            cics.ceci().retrieveVariableText(ceciTerminal, variableName);
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
		cics.ceci().defineVariableText(ceciTerminal, variableName, variableText);
		assertThat(cics.ceci().retrieveVariableText(ceciTerminal, variableName)).isEqualTo(variableText);
		
		cics.ceci().deleteVariable(ceciTerminal, variableName);
		boolean exceptionCaught = false;
		try {
			cics.ceci().retrieveVariableText(ceciTerminal, variableName);
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
        length = cics.ceci().defineVariableText(ceciTerminal, variableName, value1);
        assertThat(length).isEqualTo(value1.length());
        
        length = cics.ceci().defineVariableText(ceciTerminal, variableName, value2);
        assertThat(length).isEqualTo(value2.length());
        
        assertThat(cics.ceci().retrieveVariableText(ceciTerminal, variableName).equals(value2));
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
        cics.ceci().defineVariableBinary(ceciTerminal, variableName, variableValue.toCharArray());
        String response = new String(cics.ceci().retrieveVariableBinary(ceciTerminal, variableName));
        assertThat(response).isEqualTo(variableValue);

        // Define a variable larger than allowed
        cics.ceci().deleteVariable(ceciTerminal, variableName);
        boolean exceptionCaught = false;
        try {
            cics.ceci().defineVariableBinary(ceciTerminal, variableName, constructRandomString(32768).toCharArray());
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
    public void testFullDataTypeVariable() throws CeciException, CicstsManagerException{
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
		String userVariable = "USERID";
		String ceciCommand = "ASSIGN USERID(&" + userVariable + ")";
		
		ICeciResponse resp = cics.ceci().issueCommand(ceciTerminal, ceciCommand, false);
        String user = cics.ceci().retrieveVariableText(ceciTerminal, userVariable);
        logger.info("Retrieved user was: " + user);
        logger.info("Response from command was: " + resp.getResponse());
        assertThat(user).isEqualTo("CICSUSER");
	}
	
	/**
     * Tests the execution of a basic documentation CECI command.
     * 
     * @throws CicstsManagerException 
     * @throws CeciException 
     */
	@Test
    public void testDocumentationBasicCommand() throws CeciException, CicstsManagerException {
        String ceciCommand = "EXEC CICS WRITE OPERATOR TEXT('About to execute Galasa Test...')";
        ICeciResponse resp = cics.ceci().issueCommand(ceciTerminal, ceciCommand);
        assertThat(resp.isNormal()).isTrue();
    }
	
    /**
     * Put data to a queue and retrieve it again.
     * 
     * @throws CeciException 
     * @throws CicstsManagerException  
     */ 
    @Test
    public void testPutAndGetDataToAContainer() throws CeciException, CicstsManagerException {
        String channelName   = "CHAN1";
        String containerName = "CONT1";
        String variableName = "&OP";
        String containerContent = "THIS IS SOME CONTAINER DATA";

        cics.ceci().putContainer(ceciTerminal, channelName, containerName, containerContent, "CHAR", null, null);
        cics.ceci().getContainer(ceciTerminal, channelName, containerName, variableName, null, null);
        assertThat(cics.ceci().retrieveVariableText(ceciTerminal, variableName).equals(containerContent));
    }  
    
    private String constructRandomString(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for(int a = 0; a<length; a++){
            sb.append(alphabet.charAt(r.nextInt(26)));
        }
        return sb.toString();
    }
	
	@AfterClass
    public void afterClass() throws CicstsManagerException {
        cics.ceci().deleteAllVariables(ceciTerminal);
    }

}
