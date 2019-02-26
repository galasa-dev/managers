package io.ejat.zos3270;

import javax.validation.constraints.NotNull;

import io.ejat.zos3270.spi.DatastreamException;
import io.ejat.zos3270.spi.NetworkException;
import io.ejat.zos3270.spi.Terminal;

public interface ITerminal {
	
	ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException;
	ITerminal positionCursorToFieldContaining(@NotNull String searchText) throws TextNotFoundException, KeyboardLockedException;

	Terminal verifyTextInField(String string) throws TextNotFoundException;

	ITerminal type(String typeText) throws FieldNotFoundException, KeyboardLockedException;
	
	ITerminal tab() throws FieldNotFoundException, KeyboardLockedException;
	ITerminal enter() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal clear() throws DatastreamException, KeyboardLockedException, NetworkException;
	ITerminal pf3() throws DatastreamException, KeyboardLockedException, NetworkException;
	
	
	
	/**
	 * Temporary Print to console
	 * 
	 * @return ITerminal for chaining
	 */
	ITerminal reportScreen();
}
