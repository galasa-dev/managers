/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.cicsts.cicsresource;

/**
 * Represents a CICS Bundle resource. It provides methods to set CICS Bundle specific attributes on the resource
 * (via CEDA) and to manage and set attributes in CEMT
 */
public interface ICICSBundle {
	
	/**
	 * Set the CICS BUNDLE resource definition DESCRIPTION attribute value
	 * @param value the resource definition DESCRIPTION attribute value
	 */
	public void setDefinitionDescriptionAttribute(String value);
	
	/**
	 * Set the CICS BUNDLE resource definition STATUS attribute value
	 * @param value the resource definition STATUS attribute value
	 */
	public void setDefinitionStatusAttribute(CicsResourceStatus value);

	/**
	 * Return the CICS BUNDLE resource definition NAME attribute value
	 * @return the resource definition NAME attribute value
	 */
	public String getResourceDefinitionNameAttribute();
	
	/**
	 * Return the CICS BUNDLE resource definition GROUP attribute value
	 * @return the resource definition GROUP attribute value
	 */
	public String getResourceDefinitionGroupAttribute();
	
	/**
	 * Return the CICS BUNDLE resource definition DESCRIPTION attribute value
	 * @return the resource definition DESCRIPTION attribute value
	 */
	public String getResourceDefinitionDescriptionAttribute();

	/**
	 * Return the CICS BUNDLE resource STATUS attribute value
	 * @return the resource STATUS attribute value
	 */
	public CicsResourceStatus getResourceDefinitionStatusAttribute();

	/**
	 * Return the CICS BUNDLE resource BUNDLEDIR attribute value
	 * @return the resource JVMPROFILE attribute value
	 */	
	public String getResourceDefinitionBundledirAttribute();

	/**
	 * Build the complete JVM server including the profile zOS UNIX file and the CICS resource definition. This method will install the CICS 
	 * resource and wait for it to become enabled
	 * @throws CicsBundleResourceException
	 */
	public void build() throws CicsBundleResourceException;

	/**
	 * Build the CICS BUNDLE resource definition only 
	 * @throws CicsBundleResourceException
	 */
	public void buildResourceDefinition() throws CicsBundleResourceException;

	/**
	 * Deploy the CICS BUNDLE to the zOS UNIX file system 
	 * @throws CicsBundleResourceException
	 */
	public void deploy() throws CicsBundleResourceException;	
	
	/**
	 * Build and install the CICS BUNDLE resource definition 
	 * @throws CicsBundleResourceException
	 */
	public void buildInstallResourceDefinition() throws CicsBundleResourceException;	
	
	/**
	 * Install the CICS BUNDLE resource definition
	 * @throws CicsBundleResourceException
	 */
	public void installResourceDefinition() throws CicsBundleResourceException;	
	
	/**
	 * Check if the CICS BUNDLE resource definition exist via CEDA DISPLAY  
	 * @throws CicsBundleResourceException
	 */
	public boolean resourceDefined() throws CicsBundleResourceException;	
	
	/**
	 * Check if the CICS BUNDLE resource has been installed via CEMT INQUIRE  
	 * @throws CicsBundleResourceException
	 */
	public boolean resourceInstalled() throws CicsBundleResourceException;

	/**
	 * Enable the CICS BUNDLE resource
	 * @throws CicsBundleResourceException
	 */
	public void enable() throws CicsBundleResourceException;

	/**
	 * Returns whether the CICS BUNDLE resource is currently enabled
	 * @return true if enabled, false if not enabled
	 * @throws CicsBundleResourceException 
	 */
	public boolean isEnabled() throws CicsBundleResourceException;

	/**
	 * Disable the CICS BUNDLE resource
	 * @return true if disabled, false if not disabled
	 * @throws CicsBundleResourceException
	 */
	public boolean disable() throws CicsBundleResourceException; 
	
	/**
	 * Delete the CICS BUNDLE resource including it's zOS UNIX files and directories. If the resource is installed, it will be disabled and discarded
	 * @throws CicsBundleResourceException
	 */
	public void delete() throws CicsBundleResourceException;

	/**
	 * Delete the CICS BUNDLE resource including it's zOS UNIX files and directories. If the resource is installed, it will be disabled and discarded. 
	 * Errors during the process will cause an exception to be thrown depending on the value of ignoreErrors 
	 * @param ignoreErrors 
	 * @throws CicsBundleResourceException
	 */
	public void delete(boolean ignoreErrors) throws CicsBundleResourceException;

	/**
	 * Discard the CICS BUNDLE resource. If the resource is enabled, it will be disabled and discarded
	 * @throws CicsBundleResourceException
	 */
	public void discard() throws CicsBundleResourceException;

	/**
	 * Disable and discard the CICS BUNDLE resource and delete the resource definition.
	 * Errors during the process will cause an exception to be thrown depending on the value of ignoreErrors 
	 * @param ignoreErrors 
	 * @throws CicsBundleResourceException
	 */
	public void disableDiscardDelete(boolean ignoreErrors) throws CicsBundleResourceException;

	/**
	 * Returns the CICS BUNDLE name as defined in the CICS Resource Definition
	 * @return the CICS BUNDLE name
	 */
	public String getName();
}
