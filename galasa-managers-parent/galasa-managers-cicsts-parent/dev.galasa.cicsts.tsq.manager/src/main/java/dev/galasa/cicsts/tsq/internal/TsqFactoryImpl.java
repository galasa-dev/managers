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
import dev.galasa.cicsts.ITsqFactory;
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
public class TsqFactoryImpl implements ITsqFactory {
    
    private static final Log logger = LogFactory.getLog(TsqFactoryImpl.class);
    
    private final ICicsRegion cicsRegion;
    private final ICicsTerminal cedaTerminal;
	private final ICicsTerminal cemtTerminal;
    private final ICicstsManagerSpi cicstsManager;
    private final TsqManagerImpl tsqManager;
    private final ICeda ceda;
	private final ICemt cemt;
	private String queueName;
    
    public TsqFactoryImpl(TsqManagerImpl manager, ICicsRegion cicsRegion, ICicstsManagerSpi cicstsManager) throws TsqException {
        this.cicsRegion = cicsRegion;
		this.cicstsManager = cicstsManager;
        this.tsqManager = manager;
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
			this.cedaTerminal = cicstsManager.generateCicsTerminal(cicsRegion.getTag());
			this.cedaTerminal.connectToCicsRegion();
			this.cedaTerminal.clear();
		} catch (CicstsManagerException | KeyboardLockedException | NetworkException | TerminalInterruptedException e) {
			throw new TsqException("Unable to get CEDA terminal for CICS region", e);
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
	 * Create the CICS resource TSMODEL if TSQ is recoverable.
	 * Create and return the ITsq object for the queueName provided.
	 */
    @Override
	public ITsq createQueue(String queueName, boolean recoverable) throws TsqException {
		this.queueName = queueName;
        checkQueueName();

		// If recoverable is true create a TSMODEL to make the TSQ as recoverable
		if (recoverable) {
			this.setRecoverable(queueName);
		}

		// Create new ITsq object for the queueName
		try {
			ITsq response = this.createQueue(queueName);
            return response;
		} catch (TsqException e) {
			throw new TsqException(String.format("Failed to create TSQ : (%s)", queueName), e);
		}
	}

    /*
	 * Create and return the ITsq object for the queueName provided. 
	 */
    @Override
	public ITsq createQueue(String queueName) throws TsqException {
		this.queueName = queueName;
        checkQueueName();
		
		// Check if TSQ is already exiting. 
		try {
			CicstsHashMap cemtInquireTSQ = this.cemt.inquireResource(this.cemtTerminal, "TSQUEUE", queueName);
			// Issue WARNING if TSQ is already existing
			if( cemtInquireTSQ != null ) {
				logger.warn(String.format("TSQ is already existing. Recoverable status will be based on existing TSQ : (%s).", queueName));
			}
		} catch ( CemtException e ) {
			throw new TsqException("Failed to inquire the existence of TSQ : " + queueName, e);
		}
		
		// Create new ITsq object for the queueName
		try {
            ITsq response = new TsqImpl(tsqManager, cicsRegion, cicstsManager, queueName);
			return response;
		} catch (TsqException e) {
			throw new TsqException("Failed to write to TSQ : " + queueName, e);
		}
	}	

	/*
	 * Create TSMODEL to make TSQ recoverable 
	 */
	private void setRecoverable(String queueName) throws TsqException {
		// Generate model name 
        String modelName = resolveModelName();

		// Check if TSMODEL is already exiting. 
		try {
			CicstsHashMap cemtInquireTSModel = this.cemt.inquireResource(this.cemtTerminal, "TSMODEL", modelName);
			// Issue a warning if TSMODEL is already existing
			if( cemtInquireTSModel != null ) {
				logger.warn(String.format("TSQMODEL (%s) is already existing for TSQ (%s). Recoverable status will be based on existing TSMODEL.", modelName, queueName));
			} else {
				try {
					// Create TSMODEL using CEDA command to make TSQ recoverable
					this.ceda.createResource(this.cedaTerminal, "TSMODEL", modelName, modelName, String.format("PREFIX(%s) RECOVERY(YES)", queueName));
					// Install TSMODEL using CEDA command to make TSQ recoverable
					this.ceda.installResource(this.cedaTerminal, "TSMODEL", modelName, modelName);
				} catch(CedaException e) {
					throw new TsqException(String.format("Failed to make TSQ : (%s) recoverable.", queueName), e);
				}
			}
		} catch ( CemtException e ) {
			throw new TsqException("Failed to inquire the existence of TSMODEL : " + modelName, e);
		}
		return;
	}    

	/**
	 * Generates a TS model name based on the TSQ name. 
	 * If the TSQ name is less than 8 characters long, it is appened with 'M'. Otherwise the 8th character is changed to be an 'M'.
	 * 
	 * If the TSQ name is: 'QUEUE   ', then the model name is: 'QUEUEM  '.
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

}
