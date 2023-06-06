/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270.terminal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.spi.Terminal;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class RetrieveTextTest extends Zos3270TestBase {

	@Test
	public void goldenPathByPosition() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();

		setScreenOrders(screen);

		String text = terminal.retrieveText(2, 1, 10);
		
		assertThat(text).as("Check text in field 2 is only 1234 without nulls").isEqualTo(" 1234    ");
		
		text = terminal.retrieveText(1, 1, 20);
		
		assertThat(text).as("Check all text in screen without nulls").isEqualTo(" abcd      1234    ");
	}

	@Test
	public void goldenPathByCursor() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();

		setScreenOrders(screen);
		
		terminal.setCursorPosition(2, 1);

		String text = terminal.retrieveTextAtCursor(10);
		
		assertThat(text).as("Check text in field 2 is only 1234 without nulls").isEqualTo(" 1234    ");
		
		assertThat(terminal.searchText("1234")).isTrue();

		terminal.setCursorPosition(1, 1);

		text = terminal.retrieveTextAtCursor(20);
		
		assertThat(text).as("Check all text in screen without nulls").isEqualTo(" abcd      1234    ");
	}

	@Test
	public void tooLongByPosition() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();
		
		setScreenOrders(screen);

		try {
			terminal.retrieveText(2, 1, 11);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid length").hasMessageContaining("Invalid length, it would exceed the screen buffer");
		}
		
		try {
			terminal.retrieveText(1, 1, 21);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid length").hasMessageContaining("Invalid length, it would exceed the screen buffer");
		}
	}

	@Test
	public void tooLongByCursor() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();
		
		setScreenOrders(screen);

		terminal.setCursorPosition(2, 1);
		try {
			terminal.retrieveTextAtCursor(11);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid length").hasMessageContaining("Invalid length, it would exceed the screen buffer");
		}
		
		terminal.setCursorPosition(1, 1);
		try {
			terminal.retrieveTextAtCursor(21);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid length").hasMessageContaining("Invalid length, it would exceed the screen buffer");
		}
	}

}
