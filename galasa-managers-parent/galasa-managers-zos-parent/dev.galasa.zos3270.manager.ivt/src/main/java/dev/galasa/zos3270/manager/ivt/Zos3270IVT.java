/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.Zos3270Terminal;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class Zos3270IVT {

    @Logger
    public Log       logger;

    @ZosImage(imageTag = "PRIMARY")
    public IZosImage image;

    @Zos3270Terminal(imageTag = "PRIMARY")
    public ITerminal terminal;
    
    @CoreManager
    public ICoreManager coreManager;
    
    @TestProperty(prefix = "IVT.RUN",suffix = "NAME", required = false)
    public String providedRunName;
    private String runName  = new String();
    
    private String applid = "IYK2ZNB5";

    @Test
    public void checkInjection() {
        assertThat(logger).as("Logger Field").isNotNull();
        assertThat(image).as("zOS Image Field").isNotNull();
        assertThat(terminal).as("zOS 3270 Terminal Field").isNotNull();
        assertThat(terminal.isConnected()).isTrue();
        if (providedRunName != null) {
        	runName = providedRunName;
        } else {
        	runName = coreManager.getRunName();
        }
        logger.info("Using Run ID of: " + runName);
    }

    @Test
    public void logon() throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, TimeoutException, KeyboardLockedException, Zos3270Exception {
    	terminal.wfk().waitForTextInField("HIT ENTER FOR LATEST STATUS");
    	terminal.type("logon applid(" +applid+ ")").enter();
    	terminal.waitForTextInField("CICS FOR GALASA TEST").clear(); 	
    }
    
    @Test
    public void reconnectTest() throws TextNotFoundException, ErrorTextFoundException, TimeoutException, KeyboardLockedException, Zos3270Exception {
    	terminal.disconnect();
    	assertThat(terminal.isConnected()).isFalse();
    	terminal.connect();
    	logon();
    	assertThat(terminal.isConnected()).isTrue();
    }
    
    @Test
    public void clearScreenCheck() {
    	//we should be at a clear screen - so lets check that 
    	assertThat(terminal.isClearScreen()).isTrue();
    }
    
    @Test 
    public void cursorControlTest() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, TextNotFoundException {
    	terminal.wfk().type("CEMT I SYS").enter().wfk();
    	//tab to the next two fields checking we get to the right place
    	terminal.tab();
    	assertThat(terminal.retrieveFieldAtCursor()).contains("00500");
    	terminal.tab();
    	assertThat(terminal.retrieveFieldAtCursor()).contains("04000");
    	//go back to the previous item
    	terminal.backTab();
    	assertThat(terminal.retrieveFieldAtCursor()).contains("00500");
    	
    	//going home and then down and right should take us to the STATUS: text
    	terminal.home();
    	terminal.cursorRight().cursorDown();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("STATUS: ");
    	
    	//re-position to the output of the Aging: field going up and left should take us to the System field
    	terminal.positionCursorToFieldContaining("00500");
    	terminal.cursorUp().cursorLeft().cursorLeft().cursorLeft().cursorLeft().cursorLeft().cursorLeft().cursorLeft();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("System");
    	
    	//let's wrap around the terminal
    	terminal.home().cursorUp();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("PF");
    	terminal.cursorDown().cursorRight();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith(" I SYS");
    	
    	terminal.positionCursorToFieldContaining("PF").cursorLeft().cursorLeft().cursorLeft();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("TIME:");
    	terminal.cursorRight().cursorRight().cursorRight();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("PF");
    	
    	terminal.pf3().wfk().clear().wfk();	
    }
    
    @Test
    public void mustBeABetterWayToTestEOF() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
    	//hear me out.........
    	//create a TSQ called Hobbit containing THE RING
    	terminal.type("CECI WRITEQ TS QNAME(HOBBIT" + runName + ") FROM('THE RING')").enter().wfk().enter().wfk().pf3().wfk().clear().wfk();
    	//try and browse the queue - but get the name wrong - no rings here
    	terminal.type("CEBR BILBOBAGGINS").enter().wfk();
    	assertThat(terminal.retrieveScreen()).doesNotContain("THE RING");
    	//so erase that and try the hobbit queue
    	terminal.home().eraseEof().type("HOBBIT"+runName).enter().wfk();
    	assertThat(terminal.retrieveScreen()).contains("THE RING");
    	//purge the queue so we can run again (YES I KNOW THIS WILL NOT  RUN IN PARALELL
    	terminal.home().tab().tab().type("purge").enter().wfk().pf3().wfk().clear();
    }
    
    @Test
    public void reportScreenTests() {
    	
    }
    
}
