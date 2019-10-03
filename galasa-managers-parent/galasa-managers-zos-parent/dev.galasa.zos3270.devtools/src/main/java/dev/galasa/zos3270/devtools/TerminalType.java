package dev.galasa.zos3270.devtools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "3270", name = "type", description = "Type on the 3270 Terminal")
@Service
public class TerminalType implements Action {

    @Argument(index = 0, name = "data", description = "The data to type", required = true)
    private String    data;
    
    @Option(name = "-e", description = "Press enter afterwords", required = false)
    private boolean enter;

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public Object execute() throws Exception {

        final TerminalHolder terminalHolder = TerminalHolder.getHolder();
        if (terminalHolder.terminal == null) {
            this.logger.error("The Terminal is not connected, use 3270:connect");
            return null;
        }

        try {
            terminalHolder.terminal.type(data);
            terminalHolder.terminal.reportScreenWithCursor();
            if (enter) {
                terminalHolder.terminal.enter();
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }

        return null;
    }
}
