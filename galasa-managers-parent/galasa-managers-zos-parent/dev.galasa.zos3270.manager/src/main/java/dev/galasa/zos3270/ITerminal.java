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

    void disconnect() throws InterruptedException;

    ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException, InterruptedException;

    ITerminal positionCursorToFieldContaining(@NotNull String searchText)
            throws TextNotFoundException, KeyboardLockedException;

    ITerminal waitForTextInField(String string) throws InterruptedException, Zos3270Exception;

    ITerminal verifyTextInField(String string) throws TextNotFoundException;

    boolean isTextInField(String string);

    ITerminal type(String typeText) throws FieldNotFoundException, KeyboardLockedException;

    ITerminal tab() throws FieldNotFoundException, KeyboardLockedException;

    ITerminal enter() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal clear() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf1() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf2() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf3() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf4() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf5() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf6() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf7() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf8() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf9() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf10() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf11() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf12() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf13() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf14() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf15() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf16() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf17() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf18() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf19() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf20() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf21() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf22() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf23() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pf24() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pa1() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pa2() throws KeyboardLockedException, NetworkException, InterruptedException;

    ITerminal pa3() throws KeyboardLockedException, NetworkException, InterruptedException;

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

}
