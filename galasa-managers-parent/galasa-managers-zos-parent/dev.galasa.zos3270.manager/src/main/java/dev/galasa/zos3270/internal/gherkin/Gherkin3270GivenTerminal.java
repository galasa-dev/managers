/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.gherkin;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IStatementOwner;
import dev.galasa.framework.spi.language.gherkin.ExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.Zos3270ManagerImpl;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class Gherkin3270GivenTerminal  implements IStatementOwner {
    
    private final static Log logger = LogFactory.getLog(Gherkin3270GivenTerminal.class);
    private final Gherkin3270Coordinator gerkinCoordinator;
    private final Zos3270ManagerImpl manager;
    
    public Gherkin3270GivenTerminal(Gherkin3270Coordinator gerkinCoordinator, Zos3270ManagerImpl manager) {
        this.gerkinCoordinator = gerkinCoordinator;
        this.manager = manager;
    }
    
    @ExecutionMethod(keyword = GherkinKeyword.GIVEN, regex = "a terminal( with id of (\\w+))?( tagged (\\w+))?")
    public void allocateTerminal(IGherkinExecutable executable, Map<String,Object> testVariables) throws Zos3270ManagerException {
        // Ensure we have a connected terminal
        List<String> groups = executable.getRegexGroups();
        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(1));

        Zos3270TerminalImpl terminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (terminal == null) {
            throw new Zos3270ManagerException("Terminal '" + terminalId + "' was not provisioned!");
        }
        if (!terminal.isConnected()) {
            throw new Zos3270ManagerException("Terminal '" + terminalId + "' is not connected to the host system");
        }
    }

    public void provision(IGherkinExecutable executable) throws Zos3270ManagerException {
        List<String> groups = executable.getRegexGroups();
        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(1));
        String imageTag = Gherkin3270Coordinator.defaultImageTag(groups.get(3));
        
        Zos3270TerminalImpl newTerminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (newTerminal == null) {
            TerminalSize terminalSize = new TerminalSize(80, 24);
            TerminalSize alternateSize = new TerminalSize(0, 0);

            newTerminal = this.manager.generateTerminal(imageTag, true, terminalSize, alternateSize);
            this.gerkinCoordinator.registerTerminal(terminalId, newTerminal, imageTag);
            logger.info("zOS 3270 Terminal id '" + terminalId + "' as been provisioned for image tag '" + imageTag + "'");
        }       
    }
}
