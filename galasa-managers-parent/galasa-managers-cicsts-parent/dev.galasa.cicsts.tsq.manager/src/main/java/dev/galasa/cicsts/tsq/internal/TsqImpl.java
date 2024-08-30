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
	private final ICicsTerminal cedaTerminal;
	private final ICicsTerminal cemtTerminal;
    private final ICicstsManagerSpi cicstsManager;
    private final ICeci ceci;
    private final ICeda ceda;
	private final ICemt cemt;
	private String queueName;
    
    public TsqImpl(TsqManagerImpl manager, ICicsRegion cicsRegion, ICicstsManagerSpi cicstsManager) throws TsqException {
        this.cicsRegion = cicsRegion;
		this.cicstsManager = cicstsManager;
		try {
			this.ceci = cicsRegion.ceci();
		} catch (CicstsManagerException e) {
			throw new TsqException("Unable to get CECI instance for CICS region", e);
		}
		try {
			this.ceda = cicsRegion.ceda();
		} catch (CicstsManagerException e) {
			throw new TsqException("Unable to get CEDA instance for CICS region", e);
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
			this.cedaTerminal = cicstsManager.generateCicsTerminal(cicsRegion.getTag());
			this.cedaTerminal.connectToCicsRegion();
			this.cedaTerminal.clear();
		} catch (CicstsManagerException | KeyboardLockedException | NetworkException | TerminalInterruptedException e) {
			throw new TsqException("Unable to get CEDA terminal for CICS region", e);
		}   
		try {
			this.cemtTerminal = cicstsManager.generateCicsTerminal(cicsRegion.getTag());
			this.cemtTerminal.connectToCicsRegion();
			this.cemtTerminal.clear();
		} catch (CicstsManagerException | KeyboardLockedException | NetworkException | TerminalInterruptedException e) {
			throw new TsqException("Unable to get CEMT terminal for CICS region", e);
		}  
    }
    
    /*
	 * Create the TSQ and write data. Make the TSQ recoverable based on recoverable param.
	 */
    @Override
	public String createQueue(String queueName, String data, boolean recoverable) throws TsqException {
		this.queueName = queueName;
		String response = "";
		// If recoverable is true create a TSMODEL to set TSQ as recoverable
		if (recoverable) {
			this.setRecoverable(queueName);
		}

		// Create TSQ and write to TSQ
		try {
			response = this.createQueue(queueName, data);
		} catch (TsqException e) {
			throw new TsqException(String.format("Failed to create TSQ : (%s)", queueName), e);
		}

		return response;
	}

    /*
	 * Create the TSQ and write data. 
	 */
    @Override
	public String createQueue(String queueName, String data) throws TsqException {
		this.queueName = queueName;
		String response = "OK";

		// Check if TSQ is already exiting. 
		try {
			CicstsHashMap cemtInquireTSQ = this.cemt.inquireResource(this.cemtTerminal, "TSQUEUE", queueName);

			if( cemtInquireTSQ != null ) {
				logger.warn(String.format("TSQ is already existing. The data will be written to existing TSQ : (%s).", queueName));
				response = "ALREADY_EXISTING";
			}
		} catch ( CemtException e ) {
			throw new TsqException("Failed to inquire the existence of TSQ : " + queueName, e);
		}
		try {
			this.writeQueue(queueName, data);
		} catch (TsqException e) {
			throw new TsqException("Failed to write to TSQ : " + queueName, e);
		}
		return response;
	}	

	/*
	 * Write to the TSQ 
	 */
    @Override
    public void writeQueue(String queueName, String data) throws TsqException { 
		this.queueName = queueName;
		// Check if queueName is set
        this.checkQueueName();
		// Check if data to write to TSQ is blank or not
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
			throw new TsqException(String.format("Failed to create variable for input data to write to TSQ : (%s).", queueName), e);
		}

		try {
			// Write to TSQ using using CECI command
			ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
			if (!resp.isNormal()) {
				throw new TsqException(String.format("Write failed for TSQ : (%s). CICS response: %s", queueName, resp.getResponse()));
			} else {
				logger.info(String.format("Written '%s' to TSQ - %s successful.",data , queueName ));
			}
		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to write to TSQ : (%s).", queueName) , e);
		} 

		// Delete the &INPUTDATA variable after writing to TSQ
		try{
			this.ceci.deleteVariable(this.ceciTerminal, "&INPUTDATA");
		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to delete variable used to write to TSQ : (%s).", queueName), e);
		}	
		return;
    }          

	/*
	 * Read the TSQ based on the item number
	 */
    @Override
    public String readQueue(String queueName, int item) throws TsqException{
		this.queueName = queueName;
		// Check if queueName is set
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
				data = "READ_ERROR";
				logger.error(String.format("There is no item (%s) to read from TSQ : (%s). CICS response : %s ", item, queueName, resp.getResponse()));
			} else {
                throw new TsqException(String.format("TSQ read failed for queue : (%s). CICS response : %s.", queueName, resp.getResponse()));
            }
		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to read TSQ : (%s).", queueName), e);
		}	
		return data;
    }    

	/*
	 * Read next entry in TSQ 
	 */
    @Override
    public String readQueueNext(String queueName) throws TsqException{
		this.queueName = queueName;
		// Check if queueName is set
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
				data = "READ_ERROR";
				logger.error(String.format("There is no item to read from TSQ : (%s). CICS response : %s ", queueName, resp.getResponse()));
			} 
			else {
                throw new TsqException(String.format("TSQ read failed for queue : (%s). CICS response : %s.", queueName, resp.getResponse()));
            }
		} catch (CeciException e) {
			throw new TsqException(String.format("Failed to read TSQ : (%s).", queueName), e);
		}	
		return data;
    }  	

	/*
	 * To delete TSQ 
	 */
    @Override
    public void deleteQueue(String queueName) throws TsqException { 
		// Check if queueName is set
        this.checkQueueName();
		
		// Create DELETEQ command to execute using CECI
		String command = String.format("DELETEQ TS %s", this.setQueueNameforCECI());
		        
        try {	
			//Start CECI session in the terminal
			this.ceci.startCECISession(this.ceciTerminal);
            
			// Delete TSQ using using CECI command
			ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
			if (!resp.isNormal()) {
                throw new TsqException(String.format("Delete failed for TSQ : (%s). CICS response: %s", queueName, resp.getResponse()));
            }
		} catch(CeciException e) {
			throw new TsqException(String.format("Failed to delete TSQ : (%s).", queueName), e);
		}
		return;
	}

	/*
	 * Create TSMODEL to make TSQ recoverable 
	 */
	private void setRecoverable(String queueName) throws TsqException {
		// Generate model name 
        String modelName = resolveModelName();
		try {
			// Run CEDA TSMODEL command to make TSQ recoverable
            this.ceda.createResource(this.cedaTerminal, "TSMODEL", modelName, modelName, String.format("PREFIX(%s) RECOVERY(YES)", queueName));
			// Install CEDA TSMODEL to make TSQ recoverable
            this.ceda.installResource(this.cedaTerminal, "TSMODEL", modelName, modelName);
		} catch(CedaException e) {
			throw new TsqException(String.format("Failed to make TSQ : (%s) recoverable.", queueName), e);
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
			// Otherwise use QUEUE to avoid potentially breaking existing tests.
			command.append(String.format("QUEUE(%s)", this.queueName));
		}
		return command.toString();
	}

	/**
	 * Generates a TS model based on the TSQ name. If the TSQ name is less than 8 characters long, it is appened with 'M'. Otherwise the 8th character is changed to be an 'M'.
	 * <p>
	 * If the TSQ name is: 'QUEUE   ', then the model name is: 'QUEUEM  '.
	 * <br />
	 * If the TSQ name is: 'FEATUREQ', then the model name is: 'FEATUREM'.
	 */
	private String resolveModelName() {
		if(this.queueName.length() < 8) {
			return this.queueName + "M";
		} else {
			return this.queueName.substring(0,7) + "M";
		}
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
