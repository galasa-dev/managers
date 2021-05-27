/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
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
    
    private String applid = "IYK2ZNB5";

    @Test
    public void checkInjection() {
        assertThat(logger).as("Logger Field").isNotNull();
        assertThat(image).as("zOS Image Field").isNotNull();
        assertThat(terminal).as("zOS 3270 Terminal Field").isNotNull();
        assertThat(terminal.isConnected()).isTrue();
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
    public void screenControlTest() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, TextNotFoundException {
    	//Enter CEMT and position cursor to the HELP field
    	//pressing tab should take us to END so pressing enter closes CEMT
    	terminal.wfk().type("CEMT I SYS").enter().wfk();
    	terminal.positionCursorToFieldContaining("HELP");
    	terminal.tab().enter().wfk();
    	assertThat(terminal.retrieveScreen()).contains("STATUS:  SESSION ENDED");    	
    	}
}
