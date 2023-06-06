/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270.util;

import java.nio.charset.Charset;
import java.util.ArrayList;

import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.internal.LogScannerImpl;
import dev.galasa.textscan.internal.TextScannerImpl;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.spi.Terminal;

public class Zos3270TestBase {

	protected static final Charset ebcdic = Charset.forName("Cp037");

	protected Terminal CreateTestTerminal() throws TerminalInterruptedException {
		TerminalSize terminalSize = new TerminalSize(10, 2);
		TerminalSize alternateTerminalSize = new TerminalSize(0, 0);
		ITextScanner textScanner = new TextScannerImpl();
		ILogScanner logScanner = new LogScannerImpl();

		ITextScannerManagerSpi mockTextScannerManager = new ITextScannerManagerSpi() {

			@Override
			public ITextScanner getTextScanner() throws TextScanManagerException {
				return textScanner;
			}

			@Override
			public ILogScanner getLogScanner() throws TextScanManagerException {
				return logScanner;
			}
			
		};
		return new Terminal("test", "", 0, false, terminalSize, alternateTerminalSize, mockTextScannerManager, ebcdic);
	}

    protected Screen CreateTestScreen() throws TerminalInterruptedException {
        return CreateTestScreen(80, 24, null);
    }

    protected static Screen CreateTestScreen(int columns, int rows, Network network) throws TerminalInterruptedException {
		TerminalSize terminalSize = new TerminalSize(columns, rows);
		TerminalSize alternateTerminalSize = new TerminalSize(0, 0);

        return new Screen(terminalSize, alternateTerminalSize, network, ebcdic);
    }

    protected void setScreenOrders(Screen screen) throws DatastreamException {
		ArrayList<AbstractOrder> orders = new ArrayList<>();
		orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
		orders.add(new OrderStartField(false, false, false, false, false, false));
		orders.add(new OrderText("abcd", ebcdic));
		orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
		orders.add(new OrderStartField(false, false, false, false, false, false));
		orders.add(new OrderText("1234", ebcdic));

		screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
				new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
	}
}
