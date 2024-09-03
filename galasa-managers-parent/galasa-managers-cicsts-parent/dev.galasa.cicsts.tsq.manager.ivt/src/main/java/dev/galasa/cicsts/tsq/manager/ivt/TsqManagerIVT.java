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
import dev.galasa.cicsts.ITsq;
import dev.galasa.cicsts.ITsqFactory;

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

	public ITsqFactory tsqFactoryA; 
	public ITsq tsqRecoverable;
	public ITsq tsqNonRecoverable;

	/**
     * Create TSQ Factory instance and ITsq objects
     * @throws CicstsManagerException 
    */
	@BeforeClass
	public void createTsqInstance() throws CicstsManagerException {
		this.tsqFactoryA = cicsRegionA.getTSQFactory();
		// Create ITsq object for recoverable TSQ
		tsqRecoverable = this.tsqFactoryA.createQueue("GALASAR", true);
		// Create ITsq object for non-recoverable TSQ
		tsqNonRecoverable = this.tsqFactoryA.createQueue("GALASAN", false);
	}

	/**
     * Check if recoverable TSQ exists
	 * 
     * @throws CicstsManagerException 
     **/
	@Test	
	public void testRecoverableTsqNotExists() throws CicstsManagerException {	
		boolean response = tsqRecoverable.exists();
		assertThat(response).as("Recoverable TSQ (GALASAR) is existing.").isEqualTo(false);
	}	

	/**
     * Check if non-recoverable TSQ exists
	 * 
     * @throws CicstsManagerException 
     **/
	@Test	
	public void testNonRecoverableTsqNotExists() throws CicstsManagerException {	
		boolean response = tsqNonRecoverable.exists();
		assertThat(response).as("Non Recoverable TSQ (GALASAN) is existing.").isEqualTo(false);
	}	

	/**
     * Tests that the recoverable TSQ is created and data is written
	 * 
     * @throws CicstsManagerException 
	 * @throws FieldNotFoundException
	 * @throws KeyboardLockedException
	 * @throws NetworkException
	 * @throws TerminalInterruptedException
	 * @throws TimeoutException
     **/
	@Test	
	public void testCreateRecoverableTsq() throws CicstsManagerException, FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {	
		String writeMessage = "001 - This message is written from Galasa to the recoverable TSQ named GALASAR.";
		// Create and write message to TSQ - GALASAR with recoverable status as true
		tsqRecoverable.writeQueue(writeMessage);
		
		// Check if the CICS TSQUEUE - GALASAR is recoverable
		boolean response = tsqRecoverable.isRecoverable();
		assertThat(response).as("TSQ (GALASAR) is not recoverable.").isEqualTo(true);

		// Read message from TSQ and validate
		String readMessage = tsqRecoverable.readQueue(1);
		assertThat(readMessage).as("TSQ (GALASAR) message read is not equal to the message written.").isEqualTo(writeMessage);
	}	

	/**
     * Tests that the data is written if TSQ is existing and recoverable status is not updated
	 * 
     * @throws CicstsManagerException 
	 * @throws FieldNotFoundException
	 * @throws KeyboardLockedException
	 * @throws NetworkException
	 * @throws TerminalInterruptedException
	 * @throws TimeoutException
     **/
	@Test	
	public void testCreateNonRecoverableTsqForExisting() throws CicstsManagerException, FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {	
		String writeMessage = "002 - This message is written from Galasa to the recoverable TSQ named GALASAR.";
		// Create and write message to TSQ - GALASAR with recoverable as false
		tsqRecoverable = tsqFactoryA.createQueue("GALASAR", false);

		// Write second message to TSQ - GALASAR 
		tsqRecoverable.writeQueue(writeMessage);		
		
		// Check if the CICS TSQUEUE - GALASAR is still recoverable
		boolean response = tsqRecoverable.isRecoverable();
		assertThat(response).as("TSQ (GALASAR) is not recoverable.").isEqualTo(true);
		

		// Read second message from TSQ and validate
		String readMessage = tsqRecoverable.readQueue(2);
		assertThat(readMessage).as("TSQ (GALASAR) message read is not equal to the message written.").isEqualTo(writeMessage);
	}	

	/**
     * Check if Recoverable TSQ exists
	 * 
     * @throws CicstsManagerException 
     **/
	@Test	
	public void testRecoverableTsqExists() throws CicstsManagerException {	
		boolean response = tsqRecoverable.exists();
		assertThat(response).as("Recoverable TSQ (GALASAR) is not existing.").isEqualTo(true);
	}

	/**
     * Tests that if non recoverable TSQ is created and data is written
	 * 
     * @throws CicstsManagerException 
	 * @throws FieldNotFoundException
	 * @throws KeyboardLockedException
	 * @throws NetworkException
	 * @throws TerminalInterruptedException
	 * @throws TimeoutException
     **/
	@Test	
	public void testCreateNonRecoverableTsq() throws CicstsManagerException, FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {	
		String writeMessage = "001 - This message is written from Galasa to the non-recoverable TSQ named GALASAN.";

		// Create and write message to TSQ - GALASAN  
		tsqNonRecoverable.writeQueue(writeMessage);
		
		// Check if the CICS TSQUEUE - GALASAN is non recoverable
		boolean response = tsqNonRecoverable.isRecoverable();
		assertThat(response).as("TSQ (GALASAN) is recoverable.").isEqualTo(false);

		// Read message from non-recoverable TSQ and validate
		String readMessage = tsqNonRecoverable.readQueue(1);
		assertThat(readMessage).as("TSQ (GALASAN) message read is not equal to the message written.").isEqualTo(writeMessage);
	}	

	/**
     * Check if NonRecoverable exists
	 * 
     * @throws CicstsManagerException 
     **/
	@Test	
	public void testNonRecoverableTsqExists() throws CicstsManagerException {	
		boolean response = tsqNonRecoverable.exists();
		assertThat(response).as("Non Recoverable TSQ (GALASAN) is not existing.").isEqualTo(true);
	}	
	/**
     * Tests that the non recoverable TSQ is deleted
	 * 
     * @throws CicstsManagerException 
	 * @throws FieldNotFoundException
	 * @throws KeyboardLockedException
	 * @throws NetworkException
	 * @throws TerminalInterruptedException
	 * @throws TimeoutException
     **/
	@Test	
	public void testTsqDelete() throws CicstsManagerException, FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {	

		// Delete TSQ
		tsqNonRecoverable.deleteQueue(); 

		// Check if the TSQ - GALASAN is not existing after deleteQueue
		boolean response = tsqNonRecoverable.exists();
		assertThat(response).as("Non Recoverable TSQ (GALASAN) is existing.").isEqualTo(false);
	}

	/**
     * Tests that the non-recoverable TSQ is created and data is written.
	 * Validate if the readQueueNext() works as expected.
     * 
     * @throws CicstsManagerException 
    */
	@Test	
	public void testTsqReadNext() throws CicstsManagerException {	
		
		// Write 4 messages to TSQ - GALASAN
		String writeMessage1 = "01) This message is written from Galasa to the TSQ named GALASAN.";
		tsqNonRecoverable.writeQueue(writeMessage1);
		String writeMessage2 = "02) This message is written from Galasa to the TSQ named GALASAN.";
		tsqNonRecoverable.writeQueue(writeMessage2);
		String writeMessage3 = "03) This message is written from Galasa to the TSQ named GALASAN.";
		tsqNonRecoverable.writeQueue(writeMessage3);
		String writeMessage4 = "04) This message is written from Galasa to the TSQ named GALASAN.";
		tsqNonRecoverable.writeQueue(writeMessage4);
		
		// Read next and validate the messages starting from item 1
		String readMessage = tsqNonRecoverable.readQueue(1);
		assertThat(readMessage).as("Error in reading the first data.").isEqualTo(writeMessage1);
		readMessage = tsqNonRecoverable.readQueueNext();
		assertThat(readMessage).as("Error in reading the second data.").isEqualTo(writeMessage2);
		readMessage = tsqNonRecoverable.readQueueNext();
		assertThat(readMessage).as("Error in reading the third data.").isEqualTo(writeMessage3);
		readMessage = tsqNonRecoverable.readQueueNext();
		assertThat(readMessage).as("Error in reading the fourth data.").isEqualTo(writeMessage4);
		
		// READ_ERROR when no more items to read.
		readMessage = tsqNonRecoverable.readQueueNext();
		assertThat(readMessage).as("Non Recoverable TSQ (GALASAN) contains more data.").isEqualTo("READ_ERROR");
	}	
}