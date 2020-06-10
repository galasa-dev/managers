/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.spi;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;

public class Terminal implements ITerminal {

    private final Screen  screen;
    private final Network network;
    private final String  id;
    private NetworkThread networkThread;
    private boolean connected = false;

    private int           defaultWaitTime = 120_000;

    private Log           logger          = LogFactory.getLog(getClass());
    
    private boolean       autoReconnect   = false;

    public Terminal(String id, String host, int port) throws TerminalInterruptedException {
        this(id, host, port, false);
    }

    public Terminal(String id, String host, int port, boolean ssl) throws TerminalInterruptedException {
        network = new Network(host, port, ssl);
        screen = new Screen(80, 24, this.network);
        this.id = id;
    }
    
    public void setAutoReconnect(boolean newAutoReconnect) {
        this.autoReconnect = newAutoReconnect;
    }

    @Override
    public synchronized void connect() throws NetworkException {
        connected = network.connectClient();
        networkThread = new NetworkThread(this, screen, network, network.getInputStream());
        logger.info("starting a new network thread");
        networkThread.start();
    }

    @Override
    public void disconnect() throws TerminalInterruptedException {
        boolean oldAutoReconnect = autoReconnect;
        autoReconnect = false;
        
        connected = false;
        if (network != null) {
            network.close();
        }
        if (networkThread != null) {
            try {
                networkThread.join();
            } catch (InterruptedException e) {
                throw new TerminalInterruptedException("Join of the network thread was interrupted",e);
            }
            networkThread = null;
        }
        
        autoReconnect = oldAutoReconnect;
    }
    
    public void networkClosed() {
        connected = false;
        if (network != null) {
            network.close();
        }
        networkThread = null;
        
        if (autoReconnect) {
            try {
                connect();
            } catch (NetworkException e) {
                logger.error("Auto reconnect failed",e);
            }
        }
    }
    
    @Override
    public boolean isConnected() {
        if (!connected) {
            return false;
        }
        
        if (network == null) {
            return false;
        }
        
        return network.isConnected();
    }

    @Override
    public ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException {
        logger.trace("Waiting for keyboard");
        screen.waitForKeyboard(defaultWaitTime);
        logger.trace("Wait for keyboard complete");
        return this;
    }

    public Screen getScreen() {
        return this.screen;
    }

    @Override
    public ITerminal positionCursorToFieldContaining(@NotNull String text)
            throws TextNotFoundException, KeyboardLockedException {
        screen.positionCursorToFieldContaining(text);
        return this;
    }

    @Override
    public Terminal verifyTextInField(String text) throws TextNotFoundException {
        screen.searchFieldContaining(text);
        return this;
    }

    @Override
    public boolean isTextInField(String text) {
        return screen.isTextInField(text);
    }

    @Override
    public Terminal waitForTextInField(String text) throws TerminalInterruptedException, Zos3270Exception {
        screen.waitForTextInField(text, defaultWaitTime);
        return this;
    }

    @Override
    public ITerminal type(String text) throws KeyboardLockedException, FieldNotFoundException {
        screen.type(text);
        return this;
    }

    @Override
    public ITerminal eraseEof() throws KeyboardLockedException, FieldNotFoundException {
        screen.eraseEof();
        return this;
    }

    @Override
    public ITerminal tab() throws KeyboardLockedException, FieldNotFoundException {
        screen.tab();
        return this;
    }

    @Override
    public ITerminal backTab() throws KeyboardLockedException, FieldNotFoundException {
        screen.backTab();
        return this;
    }

    @Override
    public ITerminal cursorUp() throws KeyboardLockedException, FieldNotFoundException {
        screen.cursorUp();
        return this;
    }

    @Override
    public ITerminal cursorDown() throws KeyboardLockedException, FieldNotFoundException {
        screen.cursorDown();
        return this;
    }

    @Override
    public ITerminal cursorLeft() throws KeyboardLockedException, FieldNotFoundException {
        screen.cursorLeft();
        return this;
    }

    @Override
    public ITerminal cursorRight() throws KeyboardLockedException, FieldNotFoundException {
        screen.cursorRight();
        return this;
    }

    @Override
    public ITerminal home() throws KeyboardLockedException, FieldNotFoundException {
        screen.home();
        return this;
    }

    @Override
    public ITerminal enter() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.ENTER));
        return this;
    }

    @Override
    public ITerminal clear() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.CLEAR));
        return this;
    }

    @Override
    public ITerminal pf1() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF1));
        return this;
    }

    @Override
    public ITerminal pf2() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF2));
        return this;
    }

    @Override
    public ITerminal pf3() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF3));
        return this;
    }

    @Override
    public ITerminal pf4() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF4));
        return this;
    }

    @Override
    public ITerminal pf5() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF5));
        return this;
    }

    @Override
    public ITerminal pf6() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF6));
        return this;
    }

    @Override
    public ITerminal pf7() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF7));
        return this;
    }

    @Override
    public ITerminal pf8() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF8));
        return this;
    }

    @Override
    public ITerminal pf9() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF9));
        return this;
    }

    @Override
    public ITerminal pf10() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF10));
        return this;
    }

    @Override
    public ITerminal pf11() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF11));
        return this;
    }

    @Override
    public ITerminal pf12() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF12));
        return this;
    }

    @Override
    public ITerminal pf13() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF13));
        return this;
    }

    @Override
    public ITerminal pf14() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF14));
        return this;
    }

    @Override
    public ITerminal pf15() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF15));
        return this;
    }

    @Override
    public ITerminal pf16() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF16));
        return this;
    }

    @Override
    public ITerminal pf17() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF17));
        return this;
    }

    @Override
    public ITerminal pf18() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF18));
        return this;
    }

    @Override
    public ITerminal pf19() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF19));
        return this;
    }

    @Override
    public ITerminal pf20() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF20));
        return this;
    }

    @Override
    public ITerminal pf21() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF21));
        return this;
    }

    @Override
    public ITerminal pf22() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF22));
        return this;
    }

    @Override
    public ITerminal pf23() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF23));
        return this;
    }

    @Override
    public ITerminal pf24() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF24));
        return this;
    }

    @Override
    public ITerminal pa1() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA1));
        return this;
    }

    @Override
    public ITerminal pa2() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA2));
        return this;
    }

    @Override
    public ITerminal pa3() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA3));
        return this;
    }

    @Override
    public ITerminal reportScreen() {
        logger.info("\n" + screen.printScreen());
        return this;
    }

    @Override
    public ITerminal reportScreenWithCursor() {
        logger.info("\n" + screen.printScreenTextWithCursor());
        return this;
    }
    
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Returns the screen print out as a String. For use in edge testing cases.
     * 
     * @return Screen as String
     */
    public String retrieveScreen() {
        return screen.printScreen();
    }

    /**
     * Return a String of the contents of the current Field. Current field is the
     * one which the cursor is at.
     * 
     * @return Current Field as String
     */
    public String retrieveFieldAtCursor() {
        int cursorPos = screen.getCursor();
        Field cursorField = screen.locateFieldAt(cursorPos);
        return cursorField.getFieldWithoutNulls();
    }

    /**
     * Returns a String of text in a Field with a given name.
     * 
     * @param fieldName Name of the field to be extracted from
     * @return String which has been extracted from the field
     * @throws TextNotFoundException
     */
    public String retrieveFieldTextAfterFieldWithString(String fieldName) throws TextNotFoundException {
        return screen.getValueFromFieldContaining(fieldName);
    }

    public String getHostPort() {
        return this.network.getHostPort();
    }

    @Override
    public void setDisplayDatastream(boolean inbound, boolean outbound) {
        NetworkThread.setDisplayInboundDatastream(inbound);
        Screen.setDisplayOutboundDatastream(outbound);
    }

}
