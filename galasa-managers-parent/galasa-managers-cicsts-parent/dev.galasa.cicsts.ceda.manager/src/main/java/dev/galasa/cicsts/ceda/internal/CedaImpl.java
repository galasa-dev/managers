/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceda.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.ICeda;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

public class CedaImpl implements ICeda{

	private ICicsRegion cicsRegion;
	
	private ICicsTerminal terminal;
	
	public CedaImpl(ICicsRegion cicsRegion) {
		this.cicsRegion = cicsRegion;
	}
	

	@Override
	public void createResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String groupName, String resourceParameters) throws CedaException{
		
		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

		this.terminal = terminal;

		try {
			this.terminal.clear();
			this.terminal.waitForKeyboard();
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Problem starting transaction", e);
		}

		try {
			this.terminal.waitForKeyboard();
			if(resourceParameters==null){
				this.terminal.type("CEDA DEFINE " + resourceType + "(" + resourceName +
						") GROUP(" + groupName + ") ").enter().waitForKeyboard();
			}else{

				this.terminal.type("CEDA DEFINE " + resourceType + "(" + resourceName +
						") GROUP(" + groupName + ") " + resourceParameters).enter().waitForKeyboard();
			}
		}catch(TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
			throw new CedaException("Problem with starting the CEDA transaction", e);
		}

		try {
			if(this.terminal.retrieveScreen().contains("DEFINE SUCCESSFUL")){
				if(terminal.retrieveScreen().contains("MESSAGES:")) {
					terminal.pf9();
				}
			}
		}catch (Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command", e);
		}

		try {
			this.terminal.pf3();
			this.terminal.waitForKeyboard();
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}

	}

	@Override
	public void installGroup(@NotNull ICicsTerminal terminal, @NotNull String groupName) throws CedaException {
		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

		this.terminal = terminal;
		try{
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(TimeoutException | KeyboardLockedException | TerminalInterruptedException | NetworkException e){
			throw new CedaException(
					"Unable to prepare for the CEDA install group", e);
		}

		try {
			terminal.type("CEDA INSTALL GROUP(" + groupName + ")").enter().waitForKeyboard();

		}catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction");
		}

		try {
			if(!this.terminal.retrieveScreen().contains("INSTALL SUCCESSFUL")) {
				this.terminal.pf9();
				this.terminal.pf3();
				this.terminal.clear();
				this.terminal.waitForKeyboard();
				throw new CedaException("Errors detected whilst installing group");
			}
		}catch(Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command", e);
		}

		try {
			this.terminal.pf3();
			this.terminal.waitForKeyboard();
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}
	}

	@Override
	public void installResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String cedaGroup)
			throws CedaException {

		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

		this.terminal = terminal;
		try {
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Problem starting transaction", e);
		}

		try {

			this.terminal.type("CEDA INSTALL " + resourceType + "(" + resourceName + ") GROUP(" +
					cedaGroup + ")").enter().waitForKeyboard();

		}catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction", e);
		}

		try {
			boolean error = false;
			try {
				if (this.terminal.retrieveScreen().contains("USE P9 FOR S MSGS")) {
					error = true;

					//if the terminal contains the error then error = true elseif it contains
					//the success then error = false
				}else if(!this.terminal.retrieveScreen().contains("INSTALL SUCCESSFUL")) {
					error = true;
				}

				if(error) {
					this.terminal.pf9();
					this.terminal.waitForKeyboard();
					throw new CedaException("Errors detected whilst installing group");
				}
			}catch(Exception e) {
				error = true;
			}
		}catch(Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command");
		}

		try {
			this.terminal.pf3();
			this.terminal.waitForKeyboard();
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}

	}

	@Override
	public void deleteGroup(@NotNull ICicsTerminal terminal, @NotNull String groupName) throws CedaException {
		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

		this.terminal = terminal;

		try {
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Problem starting transaction", e);
		}

		try {
			this.terminal.type("CEDA DELETE GROUP(" + groupName + ") ALL").enter().waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction");
		}

		try {
			if(!this.terminal.retrieveScreen().contains("DELETE SUCCESSFUL")) {
				this.terminal.pf9();
				this.terminal.pf3();
				this.terminal.clear();
				this.terminal.waitForKeyboard();

				throw new CedaException("Errors detected whilst discarding group");
			}
		}catch(Exception e) {
			throw new CedaException("Problem determining the result from the CEDA command", e);
		}

		try {
			this.terminal.pf3();
			this.terminal.waitForKeyboard();
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}
	}

	@Override
	public void deleteResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, 
            @NotNull String resourceName, @NotNull String groupName)
			throws CedaException {

		if(cicsRegion != terminal.getCicsRegion()) {
			 throw new CedaException("The provided terminal is not from the correct CICS Region");
		}

		this.terminal = terminal;
		try {
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Problem starting transaction", e);
		}

		try {

			this.terminal.waitForKeyboard();
			this.terminal.type("CEDA DELETE " + resourceType + "("
					+ resourceName + ") GROUP(" + groupName + ")").enter();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Problem with starting the CEDA transaction", e);
		}

		try {
			if(!this.terminal.retrieveScreen().contains("DELETE SUCCESSFUL")) {
				this.terminal.pf9()
				.pf3().clear()
				.waitForKeyboard();
				throw new CedaException("Errors detected whilst discarding group");
			}
		}catch(Exception e) {
			throw new CedaException("Problem determinign the result from the CEDA command)", e);

		}
		try {
			this.terminal.pf3();
			this.terminal.waitForKeyboard();
			this.terminal.clear();
			this.terminal.waitForKeyboard();
		}catch(Exception e) {
			throw new CedaException("Unable to return terminal back into reset state", e);
		}

	}

}
