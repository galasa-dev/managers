/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.IExecInterfaceBlock;
import dev.galasa.zos3270.ITerminal;

/**
 * CICS/TS Command-level Interpreter (CECI) Interface.
 *
 */
public interface ICECI {
    
    /**
     * Issue a CECI command. The command will be stored and executed from a CECI variable. 
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should first have issued <code>CEOT TRANIDONLY</code>.
     * @param command a {@link String} containing the CECI command
     * @return an {@link IExecInterfaceBlock} object containing the command's EIB response.
     * @throws CECIException 
     */
    public IExecInterfaceBlock issueCommand(@NotNull ITerminal ceciTerminal, @NotNull String command) throws CECIException;

    /**
     * Set a CECI text variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should first have issued <code>CEOT TRANIDONLY</code>.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @param value text string with a maximum length of 32767 characters.
     * @throws CECIException
     */
    public void setVariable(@NotNull ITerminal ceciTerminal, @NotNull String name, @NotNull String value) throws CECIException;

    /**
     * Set a CECI binary variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should first have issued <code>CEOT TRANIDONLY</code>.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @param value binary char array with a maximum length of 32767 characters.
     * @throws CECIException
     */
    public void setVariableBinary(@NotNull ITerminal ceciTerminal, @NotNull String name, @NotNull char[] value) throws CECIException;
    
    /**
     * Set a hexadecimal CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @param value char array value with a maximum length of 32767 characters.
     * @throws CECIException
     */
    public void setVariableHex(@NotNull ITerminal ceciTerminal, @NotNull String name, @NotNull char[] value) throws CECIException;
    
    /**
     * Set a double word CECI variable. 
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @param value a {@link Double} representing a double word (8 bytes) with a decimal value from -9223372036854775808 to +9223372036854775807
     * @throws CECIException
     */
    public void setVariableDoubleWord(@NotNull ITerminal ceciTerminal, @NotNull String name, @NotNull Double value) throws CECIException;
    
    /**
     * Set a full word CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @param value an integer representing a full word (4 bytes) with a decimal value from -2147483648 to +2147483647 ({@link Integer.MIN_VALUE} to {@link Integer.MAX_VALUE}) 
     * @throws CECIException
     */
    public void setVariableFullWord(@NotNull ITerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CECIException;
    
    /**
     * Set a half word CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @param value an integer representing a full word (2 bytes) with a decimal value from -32768 to +32767 
     * @throws CECIException
     */
    public void setVariableHalfWord(@NotNull ITerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CECIException;
    
    /**
     * Set a packed decimal CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @param value an integer representing a full word (2 bytes) with a decimal value from -9999999 to +9999999 (0x9999999D to 0x9999999C) 
     * @throws CECIException
     */
    public void setVariablePacked(@NotNull ITerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CECIException;

    /**
     * Get a CECI text variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @return variable value
     * @throws CECIException
     */
    public String getVariable(@NotNull ITerminal ceciTerminal, @NotNull String name) throws CECIException;

    /**
     * Get a CECI binary variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @return variable value
     * @throws CECIException
     */
    public char[] getVariableBinary(@NotNull ITerminal ceciTerminal, @NotNull String name) throws CECIException;

    /**
     * Get a hexadecimal CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @return variable value
     * @throws CECIException
     */
    public char[] getVariableHex(@NotNull ITerminal ceciTerminal, @NotNull String name) throws CECIException;

    /**
     * Get a double word CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @return variable value
     * @throws CECIException
     */
    public Double getVariableDoubleWord(@NotNull ITerminal ceciTerminal, @NotNull String name) throws CECIException;

    /**
     * Get a full word CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @return variable value
     * @throws CECIException
     */
    public String getVariableFullWord(@NotNull ITerminal ceciTerminal, @NotNull String name) throws CECIException;

    /**
     * Get a half word CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. Must start with {@literal &} with a maximum length of 10.
     * @return variable value
     * @throws CECIException
     */
    public String getVariableHalfWord(@NotNull ITerminal ceciTerminal, @NotNull String name) throws CECIException;
    
    /**
     * Delete all variables in this CECI session.
     * @param terminal
     * @throws CECIException
     */
    public void clearVariableScreen(@NotNull ITerminal terminal) throws CECIException;
    
    /**
     * Get the content of the current EXEC Interface Block (EIB)
     * @param terminal
     * @return the {@link IExecInterfaceBlock} 
     * @throws CECIException
     */
    public IExecInterfaceBlock getEIB(@NotNull ITerminal terminal) throws CECIException;

    /**
     * EXEC CICS LINK to a PROGRAM.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should first have issued <code>CEOT TRANIDONLY</code>.
     * @param programName the name of the PROGRAM
     * @param commarea a string representing the COMMAREA. If null, COMMAREA will be omitted from the command. Can be CECI variable name populated with
     *  (<b>&</b>name set via {@link #setVariable(ITerminal, String, String)}) or the actual data. The value of DATALENGTH in the command will be 
     * set to the data length().
     * @param sysid the system name where the CICS region where the link request is to be routed. If null, SYSID will be omitted from the command.
     * @param transid the name of the mirror transaction on the remote region. If null, TRANSID will be omitted from the command.
     * @param synconreturn the remote system should take a sync point at program end. If false, SYNCONRETURN will be omitted from the command.
     * @throws CECIException
     */
    public void linkProgram(@NotNull ITerminal ceciTerminal, @NotNull String programName, String commarea, String sysid, String transid, boolean synconreturn) throws CECIException;

    /**
     * EXEC CICS LINK to a PROGRAM with a CHANNEL. Use {@link #putContainer(ITerminal, String, String, String)} to create the container(s) on the CHANNEL 
     * and {@link #getContainer(ITerminal, String, String, String)} to retrieve the content after the LINK.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should first have issued <code>CEOT TRANIDONLY</code>.  
     * @param programName the name of the PROGRAM
     * @param channelName the name of the CHANNEL.
     * @throws CECIExceptionan an {@link IExecInterfaceBlock} object containing the command's EIB response.
     */
    public IExecInterfaceBlock linkProgramWithChannel(@NotNull ITerminal ceciTerminal, @NotNull String programName, @NotNull String channelName, String sysid, String transid, boolean synconreturn) throws CECIException;
    
    /**
     * Puts data in a CONTAINER with an associated CHANNEL.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * @param channelName the CHANNELNAME
     * @param containerName the COTAINER name
     * @param conntent a string representing the container contents. Can be CECI variable name populated with (<b>&</b>name set via {@link #setVariable(ITerminal, String, String)}) 
     * or the actual data. The value of FLENGTH in the command will be set to the data length.
     * @param dataType BIT or CHAR. If null, DATATYPE will be ommited from the command.
     * @param fromCcsid provides a value for FROMCCSID. If null, will be omitted from the command.
     * @param fromCodepage provides a value for FROMCODEPAGE. If null, will be omitted from the command.
     * @return an {@link IExecInterfaceBlock} object containing the command's EIB response.
     * @throws CECIException
     */
    public IExecInterfaceBlock putContainer(@NotNull ITerminal ceciTerminal, @NotNull String channelName, @NotNull String containerName, @NotNull String conntent, String dataType, String fromCcsid, String fromCodepage) throws CECIException;
    
    /**
     * Gets the data in a CONTAINER with an associated CHANNEL into a CECI variable.
     * @param ceciTerminal an {@link ITerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should first have issued <code>CEOT TRANIDONLY</code>.
     * @param channelName the CHANNELNAME
     * @param containerName the COTAINER name
     * @param variableName the CECI variable name. Data can be retrieved using {@link #getVariable(ITerminal, String)} or {@link #getVariableHex(ITerminal, String)} 
     * @param intoCcsid provides a value for INTOCCSID. If null, will be omitted from the command.
     * @param intoCodepage provides a value for INTOCODEPAGE. If null, will be omitted from the command.
     * @return an {@link IExecInterfaceBlock} object containing the command's EIB response.
     * @throws CECIException
     */
    public IExecInterfaceBlock getContainer(@NotNull ITerminal ceciTerminal, @NotNull String channelName, @NotNull String containerName, @NotNull String variableName, String intoCcsid, String intoCodepage) throws CECIException;

}

