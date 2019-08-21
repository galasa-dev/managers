package dev.galasa.common.zosconsole.zosmf.manager.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.common.zosconsole.IZosConsole;
import dev.galasa.common.zosconsole.IZosConsoleResponse;
import dev.galasa.common.zosconsole.ZosConsoleException;
import dev.galasa.common.zos.IZosImage;

public class ZosConsoleImpl implements IZosConsole {
	
	private IZosImage image;

	@Override
	public @NotNull IZosConsoleResponse issueCommand(@NotNull String command, @NotNull IZosImage image) throws ZosConsoleException {
		this.image = image;
		return null;
	}

	@Override
	public String toString() {
		return this.image.getImageID();
	}
}
