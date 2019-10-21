/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.devtools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "3270", name = "clear", description = "Clear the 3270 Terminal")
@Service
public class TerminalClear implements Action {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public Object execute() throws Exception {

        final TerminalHolder terminalHolder = TerminalHolder.getHolder();
        if (terminalHolder.terminal == null) {
            this.logger.error("The Terminal is not connected, use 3270:connect");
            return null;
        }

        try {
            terminalHolder.terminal.clear();
            terminalHolder.terminal.reportScreenWithCursor();
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }

        return null;
    }
}
