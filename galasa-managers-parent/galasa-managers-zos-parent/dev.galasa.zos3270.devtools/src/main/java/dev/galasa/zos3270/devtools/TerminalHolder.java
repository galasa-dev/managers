/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.devtools;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.IScreenUpdateListener;
import dev.galasa.zos3270.spi.Terminal;

public class TerminalHolder implements IScreenUpdateListener {

    private static TerminalHolder TerminalHolder;

    Terminal                      terminal;

    private TerminalHolder() {
    };

    public static TerminalHolder getHolder() {
        synchronized (TerminalHolder.class) {
            if (TerminalHolder == null) {
                TerminalHolder = new TerminalHolder();
            }
            return TerminalHolder;
        }
    }

	@Override
	public void screenUpdated(@NotNull Direction direction, AttentionIdentification aid) {
        terminal.reportScreenWithCursor();
	}
}
