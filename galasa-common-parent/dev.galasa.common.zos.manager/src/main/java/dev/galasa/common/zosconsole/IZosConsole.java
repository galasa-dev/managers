package dev.galasa.common.zosconsole;

import javax.validation.constraints.NotNull;

import dev.galasa.common.zos.IZosImage;

/**
 * Provides the test code access to the zOS Console Manager 
 *
 */
public interface IZosConsole {
	
	/**
	 * Issue a command to the zOS Console
	 * 
	 * @param command The console command
	 * @param image {@link IZosImage} The zOS image to which the command is issued
	 * @return {@link IZosConsoleResponse} A representation of the console response
	 * @throws ZosConsoleException 
	 */
	@NotNull
	public IZosConsoleResponse issueCommand(@NotNull String command, @NotNull IZosImage image) throws ZosConsoleException;

}
