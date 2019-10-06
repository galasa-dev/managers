/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.NetworkException;

public interface ITerminal {
	
	ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException;
	ITerminal positionCursorToFieldContaining(@NotNull String searchText) throws TextNotFoundException, KeyboardLockedException;

	ITerminal waitForTextInField(String string) throws TextNotFoundException;
	ITerminal verifyTextInField(String string) throws TextNotFoundException;
	boolean isTextInField(String string);

	ITerminal type(String typeText) throws FieldNotFoundException, KeyboardLockedException;
	
	ITerminal tab() throws FieldNotFoundException, KeyboardLockedException;
	ITerminal enter() throws DatastreamException, KeyboardLockedException, NetworkException; 
	ITerminal clear() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf1() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf2() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf3() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf4() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf5() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf6() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf7() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf8() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf9() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf10() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf11() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf12() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf13() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf14() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf15() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf16() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf17() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf18() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf19() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf20() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf21() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf22() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf23() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf24() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pa1() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pa2() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pa3() throws DatastreamException, KeyboardLockedException, NetworkException;
	
	
	
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
