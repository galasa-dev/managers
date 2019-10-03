package dev.galasa.zos3270.devtools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "3270", name = "disconnect", description = "Disconnect a 3270 Terminal")
@Service
public class TerminalDisconnect implements Action {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public Object execute() throws Exception {

        final TerminalHolder terminalHolder = TerminalHolder.getHolder();
        if (terminalHolder.terminal == null) {
            this.logger.error("The Terminal is is not connected");
            return null;
        }

        terminalHolder.terminal.getScreen().unregisterScreenUpdateListener(terminalHolder);
        terminalHolder.terminal.disconnect();
        terminalHolder.terminal = null;

        return null;
    }
}
