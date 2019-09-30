package dev.galasa.common.zosconsole;

/**
 * Represents a zOS Console Command
 *
 */
public interface IZosConsoleCommand {

	/**
	 * Return the immediate response from a console command
	 * @return the response String
	 * @throws ZosConsoleException
	 */
	public String getResponse() throws ZosConsoleException;

	/**
	 * Return the delayed response from the current console command
	 * @return the response string
	 * @throws ZosConsoleException
	 */
	public String requestResponse() throws ZosConsoleException;
	
	/**
	 * Return the command
	 * @return the command String
	 */
	public String getCommand();

}
