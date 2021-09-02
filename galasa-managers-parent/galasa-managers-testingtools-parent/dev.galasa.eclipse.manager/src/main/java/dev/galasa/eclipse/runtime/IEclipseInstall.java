/*
* Copyright contributors to the Galasa project 
*/

package dev.galasa.eclipse.runtime;

/**
 * 
 * @author Reece Williams
 *
 */
public interface IEclipseInstall {
	
	/**
	 * Returns the version of eclipse being used
	 * @return EclipseVersion
	 */
	EclipseVersion getEclipseVersion();
	
	/**
	 * Returns the types of eclipse being used
	 * @return EclipseType
	 */
	EclipseType getEclipseType();
	
	/**
	 * Returns the Instance of java being used
	 * @return String
	 */
	String getJavaInstallation();
	
	/**
	 * Returns the location of the eclipse install
	 * @return String
	 */
	String getEclipseInstallation();
}