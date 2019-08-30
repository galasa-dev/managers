package dev.galasa.common.zosconsole.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.common.zosconsole.IZosConsole;
import dev.galasa.common.zosconsole.IZosConsoleCommand;
import dev.galasa.common.zosconsole.ZosConsoleException;
import dev.galasa.common.zosconsole.ZosConsoleManagerException;
import dev.galasa.common.zos.IZosImage;

public class ZosConsoleImpl implements IZosConsole {

	private List<ZosConsoleCommandImpl> zosConsoleCommands = new ArrayList<>();
	private IZosImage image;
	
	public ZosConsoleImpl(IZosImage image) {
		this.image = image;
	}

	@Override
	public @NotNull IZosConsoleCommand issueCommand(@NotNull String command) throws ZosConsoleException {
		return issueCommand(command, null);
	}

	@Override
	public @NotNull IZosConsoleCommand issueCommand(@NotNull String command, String consoleName) throws ZosConsoleException {
		ZosConsoleCommandImpl zosConsoleCommand;
		
		try {
			zosConsoleCommand = new ZosConsoleCommandImpl(command, consoleName, this.image);
			this.zosConsoleCommands.add(zosConsoleCommand);
		} catch (ZosConsoleManagerException e) {
			throw new ZosConsoleException("Unable to issue console command", e);
		}
		
		return zosConsoleCommand.issueCommand();
	}
}
