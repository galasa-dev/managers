/*
* Copyright contributors to the Galasa project 
*/

package dev.galasa.eclipse;

/**
 * 
 * @author reecewilliams
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
	 * Returns the version of java being used
	 * @return String
	 */
	String getJavaVersion();

	
	/**
	 * Returns the path where the workspace resides.
	 * @return String
	 */
	String getWorkspacePath();
	
}