package dev.voras.common.zos3270;

import javax.validation.constraints.NotNull;

import dev.voras.common.zos3270.spi.DatastreamException;
import dev.voras.common.zos3270.spi.NetworkException;

public interface ITerminal {
	
	ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException;
	ITerminal positionCursorToFieldContaining(@NotNull String searchText) throws TextNotFoundException, KeyboardLockedException;

	ITerminal waitForTextInField(String string) throws TextNotFoundException;
	ITerminal verifyTextInField(String string) throws TextNotFoundException;

	ITerminal type(String typeText) throws FieldNotFoundException, KeyboardLockedException;
	
	ITerminal tab() throws FieldNotFoundException, KeyboardLockedException;
	ITerminal enter() throws DatastreamException, KeyboardLockedException, NetworkException; //NOSONAR
	ITerminal clear() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf1() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf2() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf3() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf4() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf5() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf6() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf7() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf8() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf9() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf10() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf11() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf12() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf13() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf14() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf15() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf16() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf17() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf18() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf19() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf20() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf21() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf22() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf23() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pf24() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pa1() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pa2() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	ITerminal pa3() throws DatastreamException, KeyboardLockedException, NetworkException;//NOSONAR
	
	
	
	/**
	 * Temporary Print to console
	 * 
	 * @return ITerminal for chaining
	 */
	ITerminal reportScreen();
	ITerminal reportScreenWithCursor();
	
	String retrieveScreen();
	String retrieveFieldAtCursor();
	String retrieveFieldTextAfterFieldWithString(String string) throws TextNotFoundException, KeyboardLockedException, FieldNotFoundException;
}
