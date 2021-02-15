/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

import java.util.Map;

import dev.galasa.cicsts.cicsresource.IJvmserver.JvmserverType;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosliberty.IZosLibertyServer;

/**
 * Represents a CICS Resource. Provides methods to manage the resource, e.g. set attributes, install and delete via CEDA,
 * enable, disable and discard via CEMT
 */
public interface ICicsResource {
	
	/**
	 * Create a CICS JVMSERVER resource object using the CICS/Galasa default properties
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofileName the name of the JVM profile
	 * @param jvmserverType the JVM server type
	 * @return the Galasa JVM server object
	 */
	public IJvmserver newJvmserver(String name, String group, String jvmprofileName, JvmserverType jvmserverType) throws CicsResourceException;
	
	/**
	 * Create a CICS JVMSERVER resource object using the supplied JVM profile
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofile the JVM server profile
	 * @param jvmserverType the JVM server type {@link JvmserverType}
	 * @return the Galasa JVM server object
	 */
	public IJvmserver newJvmserver(String name, String group, IJvmprofile jvmprofile, JvmserverType jvmserverType) throws CicsResourceException;
	
	/**
	 * Create a CICS Liberty JVMSERVER resource object using the supplied JVM profile and Liberty server
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofile the JVM server profile
	 * @return the Galasa JVM server object
	 */
	public IJvmserver newLibertyJvmserver(String name, String group, IJvmprofile jvmprofile, IZosLibertyServer libertyServer) throws CicsResourceException;

	/**
	 * Create an empty JVM profile object for use by a {@link IJvmserver}
	 * @param jvmprofileName the name of the JVM profile
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName);
	
	/**
	 * Create an JVM profile object for use by a {@link IJvmserver} using the supplied {@link Map} of options.<p>
	 * See {@link IJvmprofile#setProfileValue(String, String)} for format of options
	 * @param jvmprofileName the name of the JVM profile
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName, Map<String, String> content);

	/**
	 * Create a JVM profile object for use by a {@link IJvmserver}
	 * @param jvmprofileName the name of the JVM profile
	 * @param jvmserverType the JVM server type {@link JvmserverType}
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName, JvmserverType jvmserverType);

	/**
	 * Create a JVM profile object for use by a {@link IJvmserver} using an existing {@link IZosUNIXFile}
	 * @param jvmprofileName the existing JVM profile file
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(IZosUNIXFile jvmprofile);
}
