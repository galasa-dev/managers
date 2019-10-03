package dev.galasa.zos3270.devtools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import dev.galasa.zos3270.spi.Terminal;

@Command(scope = "3270", name = "connect", description = "Connect a 3270 Terminal")
@Service
public class TerminalConnect implements Action {

    @Argument(index = 0, name = "host", description = "The host to connect to", required = true)
    private String    host;

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public Object execute() throws Exception {

        final TerminalHolder terminalHolder = TerminalHolder.getHolder();
        if (terminalHolder.terminal != null) {
            this.logger.error("The Terminal is instantiated, use 3270:disconnect");
            return null;
        }

        try {
            terminalHolder.terminal = new Terminal(host, 23);
            terminalHolder.terminal.getScreen().registerScreenUpdateListener(terminalHolder);
            terminalHolder.terminal.connect();
        } catch(Exception e) {
            e.printStackTrace();
            terminalHolder.terminal = null;
            throw e;
        }

        return null;
    }
}
