/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.cicsts.tsq.manager.ivt;
		
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import org.apache.commons.logging.Log;

import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.ITsqHandler;

import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
	
@Test	
public class TsqManagerIVT {	
	
	@Logger
	public Log logger;
		
	@CicsRegion(cicsTag="A")
	public ICicsRegion cicsRegionA; 
	
	@CicsTerminal(cicsTag="A")
	public ICicsTerminal cemtTerminalA; 

	public ITsqHandler tsqA; 
	
	/**
     * Validate if the ITsqHandler object is not NULL
     * 
     * @throws CicstsManagerException 
    */	
	@BeforeClass
	public void checkTsqLoaded() throws CicstsManagerException {
		tsqA = cicsRegionA.tsq();
      	assertThat(tsqA).isNotNull();
    }

	/**
     * Set the TSQ name for performing ITsqHandler methods
     * 
     * @throws CicstsManagerException 
    */		
	@BeforeClass
	public void setTsqName() throws CicstsManagerException {
		// Set the TSQ name as GALASAQ for readQ(), writeQ() and deleteQ() methods.
      	tsqA.setQName("GALASAQ");
    }

	/**
     * Check if getQName() returns the TSQ name
     * 
     * @throws CicstsManagerException 
    */		
	@Test
	public void getTsqName() throws CicstsManagerException {
		// Set the TSQ name as GALASAQ for readQ(), writeQ() and deleteQ() methods.
		assertThat(tsqA.getQName()).isEqualTo("GALASAQ");
    }

	/**
     * Tests that the Text is written to TSQ and read from TSQ
     * 
     * @throws CicstsManagerException 
    */
	@Test	
	public void testTsqWriteRead() throws CicstsManagerException {	
		String writeMessage = "This message is written from Galasa to the TSQ named GALASAQ.";
		// Write message to TSQ - GALASAQ
		tsqA.writeQ(writeMessage);
		logger.info("Text written to TSQ GALASAQ : " + writeMessage);

		//Read message from TSQ - GALASAQ
		String readMessage = tsqA.readQ(1);
		logger.info("Text read from TSQ GALASAQ  : " + readMessage);

		// Validate if the message read from TSQ is same as the message written
		assertThat(readMessage).isEqualTo(writeMessage);
	}	

	/**
     * Tests that the TSQ is made recoverable
     * 
     * @throws CicstsManagerException 
	 * @throws FieldNotFoundException
	 * @throws KeyboardLockedException
	 * @throws NetworkException
	 * @throws TerminalInterruptedException
	 * @throws TimeoutException
    */	
	@Test
	public void testTsqMakeRevoverable() throws CicstsManagerException, FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {
		// Make the TSQ named GALASAQ recoverable
		tsqA.makeRecoverable();

		// Inquire the CICS TSMODEL resource GALASAQM 
		String tsModelProps = cemtTerminalA.resetAndClear().waitForKeyboard().type("CEMT INQUIRE TSMODEL(GALASAQM)").enter().waitForKeyboard().tab().type("s").enter().waitForKeyboard().retrieveScreen();
		// Validate if the TSMODEL sets the recover status to recoverable
		assertThat(tsModelProps).contains("Tsmodel(GALASAQM)","Prefix(GALASAQ)","Recovstatus(Recoverable)");
	}	

	/**
     * Tests that the TSQ is deleted
     * 
     * @throws CicstsManagerException 
	 * @throws FieldNotFoundException
	 * @throws KeyboardLockedException
	 * @throws NetworkException
	 * @throws TerminalInterruptedException
	 * @throws TimeoutException
    */		
	@Test
	public void testTsqDeleteQ() throws CicstsManagerException, FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {
		String tsqPropsBeforeDelete = cemtTerminalA.resetAndClear().waitForKeyboard().type("CEMT INQUIRE TSQUEUE(GALASAQ)").enter().waitForKeyboard().retrieveScreen();
		// Validate if the TSQ - GALASAQ is existing
		assertThat(tsqPropsBeforeDelete).contains("Tsq(GALASAQ ","NORMAL");
		assertThat(tsqPropsBeforeDelete).doesNotContain("NOT FOUND");
		
		// Delete the TSQ GALASAQ
		tsqA.deleteQ();

		// Inquire the CICS TSMODEL resource GALASAQM 
		String tsqPropsAfterDelete = cemtTerminalA.resetAndClear().waitForKeyboard().type("CEMT INQUIRE TSQUEUE(GALASAQ)").enter().waitForKeyboard().retrieveScreen();
		// Validate if the TSQ - GALASAQ is deleted
		assertThat(tsqPropsAfterDelete).contains("Tsq(GALASAQ ","NOT FOUND","ERROR");
	}	
}