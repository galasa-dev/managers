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

public class CursorTest extends Zos3270TestBase {

	@Test
	public void goldenPath() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();

		setScreenOrders(screen);

		terminal.setCursorPosition(2, 5);

		int bufferPos = screen.getCursor();
		assertThat(bufferPos).as("Buffer position after setCursorPosition").isEqualTo(14);
	}

	@Test
	public void exceedsBuffer() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();

		setScreenOrders(screen);

		try {
			terminal.setCursorPosition(3, 1);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("exceeds number of rows");
		}

		try {
			terminal.setCursorPosition(2, 11);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("exceeds number of columns");
		}
	}

	@Test
	public void invalidPositions() throws KeyboardLockedException, Zos3270Exception {
		Terminal terminal = CreateTestTerminal();
		Screen screen = terminal.getScreen();

		setScreenOrders(screen);

		try {
			terminal.setCursorPosition(-1, 1);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("Invalid cursor position");
		}

		try {
			terminal.setCursorPosition(0, 1);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("Invalid cursor position");
		}

		try {
			terminal.setCursorPosition(1, -1);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("Invalid cursor position");
		}
		try {
			terminal.setCursorPosition(1, 0);
			Assert.fail("Should have thrown an exception");
		} catch(Zos3270Exception e) {
			assertThat(e).as("Exception message after invalid cursor position").hasMessageContaining("Invalid cursor position");
		}
	}
}
