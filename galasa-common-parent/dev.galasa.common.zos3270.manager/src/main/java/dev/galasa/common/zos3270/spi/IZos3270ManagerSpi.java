package dev.galasa.common.zos3270.spi;

import dev.galasa.common.zos3270.IZos3270Manager;
import dev.galasa.common.zos3270.Zos3270ManagerException;

public interface IZos3270ManagerSpi extends IZos3270Manager {

    Zos3270TerminalImpl getTerminal() throws Zos3270ManagerException;

}
