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
import dev.galasa.cicsts.ITsqHandler;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.IExecInterfaceBlock;
import dev.galasa.cicsts.ICeci;
import dev.galasa.cicsts.ICeda;
import dev.galasa.cicsts.ICeciResponse;
import dev.galasa.cicsts.CeciException;
import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;


/**
 * Implementation of {@link ITsqHandler}
 */
public class TsqImpl implements ITsqHandler {
    
    private static final Log logger = LogFactory.getLog(TsqImpl.class);
    
    private final ICicsRegion cicsRegion;
    private final ICicsTerminal ceciTerminal;
	private final ICicsTerminal cedaTerminal;
    private final ICicstsManagerSpi cicstsManager;
    private final ICeci ceci;
    private final ICeda ceda;
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
			this.ceciTerminal = cicstsManager.generateCicsTerminal(cicsRegion.getTag());
			this.ceciTerminal.connectToCicsRegion();
			this.ceciTerminal.resetAndClear();
		} catch (CicstsManagerException e) {
			throw new TsqException("Unable to get CECI terminal for CICS region", e);
		}       
		try {
			this.cedaTerminal = cicstsManager.generateCicsTerminal(cicsRegion.getTag());
			this.cedaTerminal.connectToCicsRegion();
			this.cedaTerminal.resetAndClear();
		} catch (CicstsManagerException e) {
			throw new TsqException("Unable to get CEDA terminal for CICS region", e);
		}   
    }
    
    /*
	 * To set the TSQ name used for readQ(), writeQ(), deleteQ() and makeRecoverable() methods
	 */
    @Override
    public void setQName(@NotNull String queueName) throws TsqException{
		this.queueName = queueName;
		// Check if queueName is blank
        if (this.queueName.trim().length() == 0){
			throw new TsqException("TSQ queue name cannot be blanks in setName() method.");
		}
		logger.info("TSQ name set to: " + queueName);
        return;
    }   

    /*
	 * To get the TSQ name used for readQ(), writeQ(), deleteQ() and makeRecoverable() methods
	 */
    @Override
    public String getQName() throws TsqException{
        return this.queueName ;
    } 	

	/*
	 * To read the TSQ with name set using setName() method
	 */
    @Override
    public String readQ(@NotNull int item) throws TsqException{
		// Check if queueName is set
        this.checkQueueName();
        String outData = "";

		// Create READQ command to execute using CECI
		String command = "READQ TS INTO(&OUTDATA)";
		if(this.queueName.length() > 8) {
            // Use QNAME where the TSQ name is greater than 8 characters.
            command += " QNAME(" + this.queueName + ")";
        } else {
            // Otherwise use QUEUE to avoid potentially breaking existing tests.
            command += " QUEUE(" + this.queueName + ")";
        }       
        command += " ITEM(" + item + ")";

		try {
			//Start CECI session in the terminal
			this.ceci.startCECISession(this.ceciTerminal);
			// Read TSQ using CECI command
            ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
            if (resp.isNormal()) {
				try {
					// Retrieve the text read from TSQ
					outData = this.ceci.retrieveVariableText(this.ceciTerminal, "&OUTDATA");
				} catch (CeciException e) {
					throw new TsqException("Read TSQ failed while trying to retrieve the data. ", e);
				}
            } else {
                throw new TsqException("TSQ read failed for queue : " + this.queueName + ". CICS response : " + resp.getResponse());
            }
		} catch (CeciException e) {
			throw new TsqException("Failed to read TSQ Data " + this.queueName + ".", e);
		}	
		
		// Delete the &OUTDATA variable after reading the TSQ 
		try{
			this.ceci.deleteVariable(this.ceciTerminal, "&OUTDATA");
		} catch (CeciException e) {
			throw new TsqException("Failed to delete variable used to read TSQ : " + this.queueName + ".", e);
		}	
		return outData;
    }    

	/*
	 * To write to the TSQ with name set using setName() method
	 */
    @Override
    public void writeQ(String inputData) throws TsqException { 
		// Check if queueName is set
        this.checkQueueName();

		// Check if inputData to write to TSQ is empty
		if (inputData.trim().length() == 0){
			throw new TsqException("Data written to TSQ cannot be empty. ");
		}

		// Create WRITEQ command to execute using CECI
        String command = "WRITEQ TS FROM(&INPUTDATA)";
        if(this.queueName.length() > 8) {
            // Use QNAME where the TSQ name is greater than 8 characters.
            command += " QNAME(" + this.queueName + ")";
        } else {
            // Otherwise use QUEUE to avoid potentially breaking existing tests.
            command += " QUEUE(" + this.queueName + ")";
        }
		        
		try {
			// Start CECI session in the terminal
			this.ceci.startCECISession(this.ceciTerminal);
			
			// Create variable to write data to TSQ
			int varLength = this.ceci.defineVariableText(this.ceciTerminal, "&INPUTDATA", inputData);
			logger.info("Message length written to variable is : " + varLength);
			logger.info("Message written is : " + inputData);

		} catch (CeciException e) {
			throw new TsqException("Failed to create variable for input data to write to TSQ : " + this.queueName + ".", e);
		}

		try {
			// Write to TSQ using using CECI command
			ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
			if (!resp.isNormal()) {
				throw new TsqException("Write to TSQ failed for queue " + this.queueName + ". CICS response: " + resp.getResponse());
			} else {
				logger.info(String.format("Written '%s' to TSQ - %s successful.",inputData , this.queueName ));
			}
		} catch (CeciException e) {
			throw new TsqException("Failed to write to TSQ : " + this.queueName + ".", e);
		} 
		
		// Delete the &INPUTDATA variable after writing to TSQ
		try{
			this.ceci.deleteVariable(this.ceciTerminal, "&INPUTDATA");
		} catch (CeciException e) {
			throw new TsqException("Failed to delete variable used to write to TSQ : " + this.queueName + ".", e);
		}	
        return;
    }          

	/*
	 * To delete the TSQ with name set using setName() method
	 */
    @Override
    public void deleteQ() throws TsqException { 
		// Check if queueName is set
        this.checkQueueName();
		
		// Create DELETEQ command to execute using CECI
		String command = "DELETEQ TS";
		if(this.queueName.length() > 8) {
			// Use QNAME where the TSQ name is greater than 8 characters.
			command += " QNAME(" + this.queueName + ")";
		} else {
			// Otherwise use QUEUE to avoid potentially breaking existing tests.
			command += " QUEUE(" + this.queueName + ")";
		}
        
        try {	
			//Start CECI session in the terminal
			this.ceci.startCECISession(this.ceciTerminal);
            
			// Delete TSQ using using CECI command
			ICeciResponse resp = this.ceci.issueCommand(this.ceciTerminal, command);
			if (!resp.isNormal()) {
                throw new TsqException("Delete TSQ failed for queue " + this.queueName + ". CICS response: " + resp.getResponse());
            }
		} catch(CeciException e) {
			throw new TsqException("Failed to delete TSQ " + this.queueName + ".", e);
		}
		return;
	}

	/*
	 * Make recoverable the TSQ with name set using setName() method
	 */
	@Override
	public void makeRecoverable() throws TsqException {
		// Generate model name 
        String modelName = resolveModelName();
		try {
			// Run CEDA TSMODEL command to make TSQ recoverable
            this.ceda.createResource(this.cedaTerminal, "TSMODEL", modelName, modelName, String.format("PREFIX(%s) RECOVERY(YES)", this.queueName));
			// Install CEDA TSMODEL to make TSQ recoverable
            this.ceda.installResource(this.cedaTerminal, "TSMODEL", modelName, modelName);
		} catch(CedaException e) {
			throw new TsqException("Failed to make TSQ : " + this.queueName + " recoverable.", e);
		}
		return;
	}    

    /**
	 * Check if the queue name is empty and throw error if empty.
	 */    
    public void checkQueueName() throws TsqException {
        if (this.queueName.trim().length() == 0){
          throw new TsqException("TSQ queue name cannot be blanks. Set queue name using setName() method. ");
        }
        return;
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
  
}
