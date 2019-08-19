package dev.galasa.common.zos3270.spi;

import java.util.ArrayList;

import dev.galasa.common.zos3270.IZos3270Manager;
import dev.galasa.common.zos3270.Zos3270ManagerException;

public interface IZos3270ManagerSpi extends IZos3270Manager {

    /**
	 * Returns a Terminal for the manager
     * @param Tag for the Terminal
	 * @return Terminal
     * @throws Zos3270ManagerException when tag does not exist
	 */
	ArrayList<Zos3270TerminalImpl> getTerminal(String tag) throws Zos3270ManagerException;

}
