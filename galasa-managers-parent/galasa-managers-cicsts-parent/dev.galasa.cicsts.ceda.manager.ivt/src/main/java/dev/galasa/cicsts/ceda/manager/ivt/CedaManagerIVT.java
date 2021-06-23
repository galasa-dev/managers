/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.cicsts.ceda.manager.ivt;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Fail;

import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.CemtException;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class CedaManagerIVT {

	@CicsRegion()
	public ICicsRegion cics;

	@CicsTerminal()
	public ICicsTerminal cedaTerminal;

	@CicsTerminal()
	public ICicsTerminal cemtTerminal;

	@CicsTerminal()
	public ICicsTerminal terminal;

	@BeforeClass
	public void login() throws InterruptedException, Zos3270Exception {
		// Logon to the CICS Region


		cedaTerminal.clear();
		cedaTerminal.waitForKeyboard();
		cemtTerminal.clear();
		cemtTerminal.waitForKeyboard();
	
	}
	@Before
	public void before() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
		// making sure that elements that will be used in the tests do not exist in the managers
		terminal.clear().waitForKeyboard();
		terminal.type("CEDA DELETE GROUP(Test) ALL").enter().waitForKeyboard();
		terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
		terminal.type("CEDA DELETE GROUP(IVT) ALL").enter().waitForKeyboard();
		terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
		terminal.type("CEDA DELETE GROUP(noIVT) ALL").enter().waitForKeyboard();
		terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
		terminal.type("CEMT DISCARD prog(Program,prg1,prg2,prg3,prg4)").enter().waitForKeyboard();
		terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
		terminal.type("CEMT DISCARD transaction(trx1)").enter().waitForKeyboard();
		terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
		terminal.type("CEMT DISCARD LIBRARY(lib1)").enter().waitForKeyboard();
		terminal.pf3().waitForKeyboard().clear().waitForKeyboard();
	}

	@Test
	public void checkCECINotNull() throws CicstsManagerException {
		
		assertThat(cics).isNotNull();
		assertThat(cics.ceda()).isNotNull();
		assertThat(cics.cemt()).isNotNull();
		assertThat(cemtTerminal).isNotNull();
		assertThat(cedaTerminal).isNotNull();
	}


	@Test
	public void testResourceProgram() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception, InterruptedException, CicstsManagerException {
		String resourceType = "PROGRAM";
		String resourceName = "Program";
		String groupName = "Test";
		String resourceParameters = null;
		boolean response = false;
		try {
			// testing create and install resource by creating it, installing it and then checking if it appeared on CEMT
			assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);
			
			cics.ceda().createResource(cedaTerminal, resourceType, resourceName, groupName, resourceParameters);

			assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(true);

			cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);

			if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)!=null) {
				response = true;
			}
			assertThat(response).isEqualTo(true);
			// if resource was installed successfully, then tests the delete method by discarding resource from CEMT, deleting and then trying to install and checking if the resource appeared on CEMT
			if (response) {
				response=false;
				cics.cemt().discardResource(cemtTerminal, resourceType, resourceName);

				if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)==null) {

					cics.ceda().deleteResource(cedaTerminal, resourceType, resourceName, groupName);

					cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);

					if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)==null) {
						response = true;
					}
					assertThat(response).isEqualTo(true);

				}else Fail.fail("Failed to discard resource");

			}else Fail.fail("Failed to intsall / delete resource");

		} catch (CedaException | CemtException e) {
			e.printStackTrace();
		}


	}

	@Test
	public void testResourceTransaction() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception, InterruptedException, CicstsManagerException {
		String resourceType = "TRANSACTION";
		String resourceName = "trx1";
		String groupName = "Test";
		String resourceParameters = "PROGRAM(PRG1)";
		boolean response = false;
		try {

			// testing create and install resource by creating it, installing it and then checking if it appeared on CEMT
			assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);
			
			cics.ceda().createResource(cedaTerminal, resourceType, resourceName, groupName, resourceParameters);

			assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(true);

			cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);

			if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)!=null) {
				response = true;
			}
			assertThat(response).isEqualTo(true);

			// if resource was installed successfully, then tests the delete method by discarding resource from CEMT, deleting and then trying to install and checking if the resource appeared on CEMT

			if (response) {
				response=false;
				cics.cemt().discardResource(cemtTerminal, resourceType, resourceName);

				if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)==null) {

					cics.ceda().deleteResource(cedaTerminal, resourceType, resourceName, groupName);

					cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);

					if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)==null) {
						response = true;
					}
					assertThat(response).isEqualTo(true);

				}else Fail.fail("Failed to discard resource");

			}else Fail.fail("Failed to intsall / delete resource");

		} catch (CedaException | CemtException e) {
			e.printStackTrace();
		}


	}

	@Test
	public void testResourceLibrary() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception, InterruptedException, CicstsManagerException {
		String resourceType = "LIBRARY";
		String resourceName = "lib1";
		String groupName = "Test";
		String resourceParameters = "DSNAME01(CTS.USER.APPL1.CICS.LOAD)";
		boolean response = false;
		try {

			// testing create and install resource by creating it, installing it and then checking if it appeared on CEMT
			assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(false);
			
			cics.ceda().createResource(cedaTerminal, resourceType, resourceName, groupName, resourceParameters);

			assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, resourceName, groupName)).isEqualTo(true);

			cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);

			if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)!=null) {
				response = true;
			}
			assertThat(response).isEqualTo(true);
			
			// if resource was installed successfully, then tests the delete method by discarding resource from CEMT, deleting and then trying to install and checking if the resource appeared on CEMT

			if (response) {
				response=false;
				cics.cemt().discardResource(cemtTerminal, resourceType, resourceName);

				if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)==null) {

					cics.ceda().deleteResource(cedaTerminal, resourceType, resourceName, groupName);

					cics.ceda().installResource(cedaTerminal, resourceType, resourceName, groupName);

					if(cics.cemt().inquireResource(cemtTerminal, resourceType, resourceName)==null) {
						response = true;
					}
					assertThat(response).isEqualTo(true);

				}else Fail.fail("Failed to discard resource");

			}else Fail.fail("Failed to intsall / delete resource");

		} catch (CedaException | CemtException e) {
			e.printStackTrace();
		}


	}


	@Test
	public void testGroup() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception, InterruptedException, CicstsManagerException {
		String resourceType = "prog";
		String resourceName = "prg1";
		String resourceName2 = "prg2";
		String resourceName3 = "prg3";
		String resourceName4 = "prg4";
		String groupName = "IVT";
		String groupName2 = "noIVT";
		boolean result =false;
		// creating different resources in two different groups
		cics.ceda().createResource(cedaTerminal, resourceType, resourceName, groupName, null);
		cics.ceda().createResource(cedaTerminal, resourceType, resourceName2, groupName, null);
		cics.ceda().createResource(cedaTerminal, resourceType, resourceName3, groupName, null);
		/** different group**/
		cics.ceda().createResource(cedaTerminal, resourceType, resourceName4, groupName2, null);
		// installing only one group and check if installed group appeared in CEMT and not installed one did not
		cics.ceda().installGroup(cedaTerminal, groupName);
		if (cics.cemt().inquireResource(cemtTerminal,resourceType, resourceName).containsValue(resourceName.toUpperCase())&&cics.cemt().inquireResource(cemtTerminal,resourceType, resourceName2).containsValue(resourceName2.toUpperCase())&&cics.cemt().inquireResource(cemtTerminal,resourceType, resourceName3).containsValue(resourceName3.toUpperCase())&&cics.cemt().inquireResource(cemtTerminal,resourceType, resourceName4)==null) {
			result =true;
		}
		assertThat(result).isEqualTo(true);

		//Checking if group delete works by discarding elements from CEMT and deleting group from CEDA, checking by installing group
		if(result) {
			result=false;
			cics.ceda().deleteGroup(cedaTerminal, groupName);
			cics.cemt().discardResource(cemtTerminal, resourceType, resourceName);
			cics.cemt().discardResource(cemtTerminal, resourceType, resourceName2);
			cics.cemt().discardResource(cemtTerminal, resourceType, resourceName3);
			assertThatThrownBy(() ->{
				cics.ceda().installGroup(cedaTerminal, groupName);
			}).isInstanceOf(CedaException.class).hasMessageContaining("Problem determining the result from the CEDA command");


		}else Fail.fail("CEDA Group Install/Delete failed");
	}


}
