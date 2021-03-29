/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.spi.NetworkException;

public interface ITerminal {

    boolean isConnected();

    void connect() throws NetworkException;

    void disconnect() throws TerminalInterruptedException;
    
    public boolean isSwitchedSSL();

    public void setDoStartTls(boolean doStartTls);

    ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException;

    ITerminal wfk() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException;

    ITerminal positionCursorToFieldContaining(@NotNull String searchText)
            throws TextNotFoundException, KeyboardLockedException;
    
    boolean isClearScreen();

    ITerminal waitForTextInField(String string) throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception;

    /**
     * @param ok - An array of text strings to find on the screen
     * @param error - An array of text strings deemed to be errors
     * @return the index of the ok string that was found
     * @throws TerminalInterruptedException - If the wait was interrupted for some reason
     * @throws TextNotFoundException - None of the ok or error strings were found before the timeout
     * @throws ErrorTextFoundException - One of the error strings were found, index of which is in the exception
     * @throws Zos3270Exception - general zos 3270 error
     */
    ITerminal waitForTextInField(String[] ok, String[] error) throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception;

    /**
     * @param ok - An array of text strings to find on the screen
     * @param error - An array of text strings deemed to be errors
     * @param timeoutInMilliseconds - timeout
     * @return the index of the ok string that was found
     * @throws TerminalInterruptedException - If the wait was interrupted for some reason
     * @throws TextNotFoundException - None of the ok or error strings were found before the timeout
     * @throws ErrorTextFoundException - One of the error strings were found
     * @throws Zos3270Exception - general zos 3270 error
     */
    ITerminal waitForTextInField(String[] ok, String[] error, long timeoutInMilliseconds) throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception;

    ITerminal verifyTextInField(String string) throws TextNotFoundException;

    boolean isTextInField(String string);

    boolean isTextInField(String string, long timeoutInMilliseconds) throws TerminalInterruptedException;

    ITerminal type(String typeText) throws FieldNotFoundException, KeyboardLockedException;
    
    ITerminal eraseEof() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal eraseInput() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal tab() throws FieldNotFoundException, KeyboardLockedException;
    
    ITerminal backTab() throws FieldNotFoundException, KeyboardLockedException;
    
    ITerminal cursorUp() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal cursorDown() throws KeyboardLockedException, FieldNotFoundException;
    
    ITerminal cursorLeft() throws KeyboardLockedException, FieldNotFoundException;
    
    ITerminal cursorRight() throws KeyboardLockedException, FieldNotFoundException;
    
    ITerminal backSpace() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal home() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal newLine() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal enter() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal clear() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf1() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf2() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf3() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf4() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf5() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf6() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf7() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf8() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf9() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf10() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf11() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf12() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf13() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf14() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf15() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf16() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf17() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf18() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf19() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf20() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf21() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf22() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf23() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf24() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pa1() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pa2() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pa3() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    /**
     * Temporary Print to console
     * 
     * @return ITerminal for chaining
     */
    ITerminal reportScreen();

    ITerminal reportScreenWithCursor();

    String retrieveScreen();

    String retrieveFieldAtCursor();

    String retrieveFieldTextAfterFieldWithString(String string) throws TextNotFoundException;
    
    String getId();
    
    void registerDatastreamListener(IDatastreamListener listener);
    void unregisterDatastreamListener(IDatastreamListener listener);

}
