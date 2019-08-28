package dev.galasa.common.zosconsole.zosmf.manager.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.common.zosconsole.IZosConsole;
import dev.galasa.common.zosconsole.IZosConsoleResponse;
import dev.galasa.common.zosconsole.ZosConsoleException;
import dev.galasa.common.zos.IZosImage;

public class ZosConsoleImpl implements IZosConsole {
	
	private IZosImage image;
	private String command;

	@Override
	public @NotNull IZosConsoleResponse issueCommand(@NotNull String command, @NotNull IZosImage image) throws ZosConsoleException {
		this.command = command;
		this.image = image;
		IZosConsoleResponse zosConsoleResponse = (IZosConsoleResponse) new ZosConsoleResponseImpl();
		return zosConsoleResponse;
	}

	@Override
	public String toString() {
		return "COMMAND=" + this.command + (this.image != null ? " IMAGE=" +  this.image.getImageID() : "");
	}
}
