/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.tsq.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.cicsts.TsqException;
import dev.galasa.cicsts.TsqManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ITsq;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.IExecInterfaceBlock;
import dev.galasa.cicsts.ICeci;
import dev.galasa.cicsts.ICeda;
import dev.galasa.cicsts.ICemt;
import dev.galasa.cicsts.ICeciResponse;
import dev.galasa.cicsts.CeciException;
import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.CemtException;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.cicsts.CicstsHashMap;

/**
 * Implementation of {@link ITsq}
 */
public class TsqImpl implements ITsq {
    
    private static final Log logger = LogFactory.getLog(TsqImpl.class);
    
    private final ICicsRegion cicsRegion;
    private final ICicsTerminal ceciTerminal;
	private final ICicsTerminal cemtTerminal;
    private final ICicstsManagerSpi cicstsManager;
    private final ICeci ceci;
	private final ICemt cemt;
	private String queueName;
    
    public TsqImpl(TsqManagerImpl manager, ICicsRegion cicsRegion, ICicstsManagerSpi cicstsManager, String queueName) throws TsqException {
        this.cicsRegion = cicsRegion;
		this.cicstsManager = cicstsManager;
		this.queueName = queueName;
		try {
			this.ceci = cicsRegion.ceci();
		} catch (CicstsManagerException e) {
			throw new TsqException("Unable to get CECI instance for CICS region", e);
		}
		try {
			this.cemt = cicsRegion.cemt();
		} catch (CicstsManagerException e) {
			throw new TsqException("Unable to get CEMT instance for CICS region", e);
		}
		try {
			this.ceciTerminal = cicstsManager.generateCicsTerminal(cicsRegion.getTag());
			this.ceciTerminal.connectToCicsRegion();
			this.ceciTerminal.clear();
		} catch (CicstsManagerException | KeyboardLockedException | NetworkException | TerminalInterruptedException e) {
			throw new TsqException("Unable to get CECI terminal for CICS region", e);
		}        
		try {
			this.cemtTerminal = cicstsManager.generateCicsTerminal(cicsRegion.getTag());
			this.cemtTerminal.connectToCicsRegion();
			this.cemtTerminal.resetAndClear();
		} catch (CicstsManagerException e) {
			throw new TsqException("Unable to get CEMT terminal for CICS region", e);
		}  
    }

	/*
	 * Check the exitence of TSQ
	 */
	@Override
    public boolean exists() throws TsqException { 
		boolean response = true;
		try {
			// Check if TSQ is already exiting. 
			CicstsHashMap cemtInquireTSQ = this.cemt.inquireResource(this.cemtTerminal, "TSQUEUE", this.queueName);
			if( cemtInquireTSQ == null ) {
				response = false;
			}
		} catch ( CemtException e ) {
			throw new TsqException("Failed to inquire the existence of TSQ : " + queueName, e);
		}
		return response;
	}

	/*
	 * Check if the TSQ is recoverable
	 */
	@Override
    public boolean isRecoverable() throws TsqException { 
		boolean response = false;
		try {
			CicstsHashMap cemtInquireTSQ = this.cemt.inquireResource(this.cemtTerminal, "TSQUEUE", this.queueName);
			if( cemtInquireTSQ != null ) {
				// Check if TSQ is recoverable
				if (cemtInquireTSQ.get("recovstatus").equals("Recoverable")) {
					response = true;
				} 
			} else {
				throw new TsqException(String.format("The TSQ (%s) does not exist, so cannot check if it is recoverable.", queueName));
			}
		} catch ( CemtException e ) {
			throw new TsqException("Failed to inquire the existence of TSQ : " + queueName, e);
		}
		return response;
	}

	/*
	 * Write to the TSQ 
	 */
    @Override
    public void writeQueue(String data) throws TsqException { 
		// Check if queueName is not empty
        this.checkQueueName();
		// Check if data to write to TSQ is not blank 
		this.checkData(data);

		// Create WRITEQ command to execute using CECI
        String command = String.format("WRITEQ TS FROM(&INPUTDATA) %s", this.setQueueNameforCECI());
		        
		try {
			// Start CECI session in the terminal
			this.ceci.startCECISession(this.ceciTerminal);
			
			// Create variable to write data to TSQ
			int varLength = this.ceci.defineVariableText(this.ceciTerminal, "&INPUTDATA", data);
			logger.info(String.format("Message length written to variable is : %s", varLength));
			logger.info(String.format("Message written is : %s", data));

		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to create variable for input data to write to TSQ : (%s).", this.queueName), e);
		}

		try {
			// Write to TSQ using using CECI command
			ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
			if (!resp.isNormal()) {
				throw new TsqException(String.format("Write failed for TSQ : (%s). CICS response: %s", this.queueName, resp.getResponse()));
			} else {
				logger.info(String.format("Written '%s' to TSQ - %s successful.",data , this.queueName ));
			}
		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to write to TSQ : (%s).", this.queueName) , e);
		} 

		// Delete the &INPUTDATA variable after writing to TSQ for next WRITEQ command
		try{
			this.ceci.deleteVariable(this.ceciTerminal, "&INPUTDATA");
		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to delete variable used to write to TSQ : (%s).", this.queueName), e);
		}	
		return;
    }          

	/*
	 * Read the TSQ based on the item number
	 */
    @Override
    public String readQueue(int item) throws TsqException{
		// Check if queueName is not empty
        this.checkQueueName();
        String data = "";

		// Create READQ command to execute using CECI
		String command = String.format("READQ TS INTO(&OUTDATA) %s ITEM(%s)", this.setQueueNameforCECI(), item);

		try {
			//Start CECI session in the terminal
			this.ceci.startCECISession(this.ceciTerminal);
			// Read TSQ using CECI command
            ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
            if (resp.isNormal()) {
				try {
					// Retrieve the text read from TSQ
					data = this.ceci.retrieveVariableText(this.ceciTerminal, "&OUTDATA");
				} catch (CeciException e) {
					throw new TsqException("Read TSQ failed while trying to retrieve the data. ", e);
				}
            } else if (resp.getResponse().equals("ITEMERR")) {
				// If invalid ITEM number in READQ command
				data = "READ_ERROR";
				logger.error(String.format("There is no item (%s) to read from TSQ : (%s). CICS response : %s ", item, this.queueName, resp.getResponse()));
			} else {
                throw new TsqException(String.format("TSQ read failed for queue : (%s). CICS response : %s.", this.queueName, resp.getResponse()));
            }
		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to read TSQ : (%s).", this.queueName), e);
		}	
		return data;
    }    

	/*
	 * Read next entry in TSQ 
	 */
    @Override
    public String readQueueNext() throws TsqException{
		// Check if queueName is not empty
        this.checkQueueName();
        String data = "";

		// Create READQ command to execute using CECI
		String command = String.format("READQ TS INTO(&OUTDATA) %s NEXT", this.setQueueNameforCECI());

		try {
			//Start CECI session in the terminal
			this.ceci.startCECISession(this.ceciTerminal);
			// Read TSQ using CECI command
            ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
            if (resp.isNormal()) {
				try {
					// Retrieve the text read from TSQ
					data = this.ceci.retrieveVariableText(this.ceciTerminal, "&OUTDATA");
				} catch (CeciException e) {
					throw new TsqException("Read TSQ failed while trying to retrieve the data. ", e);
				}
            } else if (resp.getResponse().equals("ITEMERR")) {
				// If no more data in TSQ
				data = "READ_ERROR";
				logger.error(String.format("There is no item to read from TSQ : (%s). CICS response : %s ", this.queueName, resp.getResponse()));
			} 
			else {
                throw new TsqException(String.format("TSQ read failed for queue : (%s). CICS response : %s.", this.queueName, resp.getResponse()));
            }
		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to read TSQ : (%s).", this.queueName), e);
		}	
		return data;
    }  	

	/*
	 * To delete TSQ 
	 */
    @Override
    public void deleteQueue() throws TsqException { 
		// Check if queueName is not empty
        this.checkQueueName();
		
		// Create DELETEQ command to execute using CECI
		String command = String.format("DELETEQ TS %s", this.setQueueNameforCECI());
		        
        try {	
			// Start CECI session in the terminal
			this.ceci.startCECISession(this.ceciTerminal);
            
			// Delete TSQ using using CECI command
			ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
			if (!resp.isNormal()) {
                throw new TsqException(String.format("Delete failed for TSQ : (%s). CICS response: %s", this.queueName, resp.getResponse()));
            }
		} catch(CeciException e) {
			throw new TsqException(String.format("Failed to delete TSQ : (%s).", this.queueName), e);
		}
		return;
	}

	// Set Queue name for CECI commands
	private String setQueueNameforCECI() {
		StringBuilder command = new StringBuilder();
		if(this.queueName.length() > 8) {
			// Use QNAME where the TSQ name is greater than 8 characters.
			command.append(String.format("QNAME(%s)", this.queueName));
		} else {
			// Use QUEUE where the TSQ name is less than or equal to 8 characters.
			command.append(String.format("QUEUE(%s)", this.queueName));
		}
		return command.toString();
	}

    /**
	 * Check if the queue name is empty and throw error if empty.
	 */    
    public void checkQueueName() throws TsqException {
        if (this.queueName.trim().length() == 0){
          throw new TsqException("TSQ queue name cannot be blank.");
        }
        return;
    } 
	
	/**
	 * Check if the data is empty and throw error if empty.
	 */    
    public void checkData(String data) throws TsqException {
        if (data.trim().length() == 0){
          throw new TsqException(String.format("Data cannot be blank to write to TSQ : (%s)", this.queueName));
        }
        return;
    } 
}
