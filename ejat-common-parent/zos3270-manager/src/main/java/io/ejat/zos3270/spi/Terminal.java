package io.ejat.zos3270.spi;

import javax.validation.constraints.NotNull;

import io.ejat.zos3270.FieldNotFoundException;
import io.ejat.zos3270.ITerminal;
import io.ejat.zos3270.KeyboardLockedException;
import io.ejat.zos3270.TextNotFoundException;
import io.ejat.zos3270.TimeoutException;
import io.ejat.zos3270.internal.comms.Network;
import io.ejat.zos3270.internal.comms.NetworkThread;
import io.ejat.zos3270.internal.datastream.AttentionIdentification;
import io.ejat.zos3270.internal.terminal.Screen;

public class Terminal implements ITerminal {
	
	private final Screen screen = new Screen(80, 24);
	private final Network network;
	private NetworkThread networkThread;
	
	private int defaultWaitTime = 1200000;

	public Terminal(String host, int port) {
		network = new Network(host, port);
	}
	
	public synchronized void connect() throws NetworkException {
		network.connectClient();
		networkThread = new NetworkThread(screen, network, network.getInputStream());
		networkThread.start();
	}
	
	public void disconnect() {
		network.close();
		try {
			networkThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		networkThread = null;
	}
	

	@Override
	public ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException {
		screen.waitForKeyboard(defaultWaitTime);
		return this;
	}
	
	
	public Screen getScreen() {
		return this.screen;
	}

	@Override
	public ITerminal positionCursorToFieldContaining(@NotNull String text) throws TextNotFoundException, KeyboardLockedException {
		screen.positionCursorToFieldContaining(text);
		return this;
	}
	
	@Override
	public Terminal verifyTextInField(String text) throws TextNotFoundException {
		screen.searchFieldContaining(text);
		return this;
	}

	@Override
	public ITerminal type(String text) throws KeyboardLockedException, FieldNotFoundException {
		screen.type(text);
		return this;
	}

	@Override
	public ITerminal tab() throws KeyboardLockedException, FieldNotFoundException {
		screen.tab();
		return this;
	}

	@Override
	public ITerminal enter() throws KeyboardLockedException, NetworkException {
		network.sendDatastream(screen.aid(AttentionIdentification.ENTER));
		return this;
	}

	@Override
	public ITerminal clear() throws KeyboardLockedException, NetworkException {
		network.sendDatastream(screen.aid(AttentionIdentification.CLEAR));
		return this;
	}

	@Override
	public ITerminal pf3() throws KeyboardLockedException, NetworkException {
		network.sendDatastream(screen.aid(AttentionIdentification.PF3));
		return this;
	}

	@Override
	public ITerminal reportScreen() {
		System.out.println(screen.printScreen());
		return this;
	}


}
