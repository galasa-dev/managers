/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

/**
 * Provides common methods to manage a CICS resource, e.g. set attributes, install and delete via CEDA, enable, 
 * disable and discard via CEMT
 */
public interface ICicsResourceBase {
	
	/**
	 * Return the CICS resource definition NAME attribute value
	 * @param value the resource definition NAME attribute value
	 */
	public void setDefinitionNameAttribute(String value);
	
	/**
	 * Set the CICS resource definition GROUP attribute value
	 * @param value the resource definition GROUP attribute value
	 */
	public void setDefinitionGroupAttribute(String value);
	
	/**
	 * Set the CICS resource definition DESCRIPTION attribute value
	 * @param value the resource definition DESCRIPTION attribute value
	 */
	public void setDefinitionDescriptionAttribute(String value);
	
	/**
	 * Set the CICS resource definition STATUS attribute value
	 * @param value the resource definition STATUS attribute value
	 */
	public void setDefinitionStatusAttribute(CicsResourceStatus value);
	
	/**
	 * Set a CICS resource definition attribute value
	 * @param attribute the resource definition attribute name
	 * @param the value of the specified resource definition attribute
	 */
	public void setDefinitionAttribute(String attribute, String value);
	
	/**
	 * Return the CICS resource definition NAME attribute value
	 * @return the resource definition NAME attribute value
	 */
	public String getDefinitionNameAttribute();
	
	/**
	 * Return the CICS resource definition GROUP attribute value
	 * @return the resource definition GROUP attribute value
	 */
	public String getDefinitionGroupAttribute();
	
	/**
	 * Return the CICS resource definition DESCRIPTION attribute value
	 * @return the resource definition DESCRIPTION attribute value
	 */
	public String getDefinitionDescriptionAttribute();
	
	/**
	 * Return the CICS resource definition STATUS attribute value
	 * @return the resource definition STATUS attribute value
	 */
	public CicsResourceStatus getDefinitionStatusAttribute();

	/**
	 * Return a CICS resource definition attribute value
	 * @param attribute the resource definition attribute name
	 * @return the value of the resource definition attribute
	 */
	public String getDefinitionAttribute(String attribute);
	
	/**
	 * Build the CICS resource definition only 
	 * @throws CicsCicsResourceException
	 */
	public void buildResourceDefinition() throws CicsResourceException;	
	
	/**
	 * Build and install the CICS resource definition 
	 * @throws CicsCicsResourceException
	 */
	public void buildInstallResourceDefinition() throws CicsResourceException;	
	
	/**
	 * Install the CICS resource definition
	 * @throws CicsCicsResourceException
	 */
	public void installResourceDefinition() throws CicsResourceException;	
	
	/**
	 * Delete the CICS resource. If the resource is installed, it will be disabled and discarded
	 * @throws CicsResourceException
	 */
	public void delete() throws CicsResourceException;

	/**
	 * Enable the CICS resource
	 * @throws CicsResourceException
	 */
	public void enable() throws CicsResourceException;

	/**
	 * Wait for the CICS resource to be enabled. Does NOT issue the enable command
	 * @throws CicsResourceException
	 */
	public void waitForEnable() throws CicsResourceException;

	/**
	 * Wait for the CICS resource to be enabled with specified timeout. Does NOT issue the disable 
	 * @param millisecondTimeout 
	 * @throws CicsResourceException
	 */
	public void waitForEnable(int millisecondTimeout) throws CicsResourceException;

	/**
	 * Disable the CICS resource
	 * @throws CicsCicsResourceException
	 */
	public void disable() throws CicsResourceException;
	
	/**
	 * Wait for the CICS resource to be disabled. Does NOT issue the disable command
	 * @throws CicsResourceException
	 */
	public void waitForDisable() throws CicsResourceException;	
	
	/**
	 * Wait for the CICS resource to be disabled with specified timeout. Does NOT issue the disable command
	 * @throws CicsResourceException
	 */
	public void waitForDisable(int millisecondTimeout) throws CicsResourceException;
	
	/**
	 * Returns whether the CICS resource is currently enabled
	 * @return true if enabled, false if not enabled
	 * @throws CicsResourceException 
	 */
	public boolean isEnabled() throws CicsResourceException;

	/**
	 * Discard the CICS resource. If the resource is enabled, it will be disabled and discarded
	 * @throws CicsResourceException
	 */
	public void discard() throws CicsResourceException;

	/**
	 * Disable and discard the CICS resource and delete the resource definition.
	 * Errors during the process will cause an exception to be thrown depending on the value of ignoreErrors 
	 * @param ignoreErrors 
	 * @throws CicsResourceException
	 */
	public void disableDiscardDelete(boolean ignoreErrors) throws CicsResourceException;
}
