/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.cicsresource;

import java.util.HashMap;

import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.IJvmserver.JvmserverType;
import dev.galasa.zosliberty.IZosLibertyServer;

/**
 * Represents a CICS Resource. Provides methods to manage the resource, e.g. set attributes, install and delete via CEDA,
 * enable, disable and discard via CEMT
 */
public interface ICicsResource {
	
	/**
	 * Create a CICS JVMSERVER resource object using the CICS/Galasa default properties
	 * @param cicsTerminal a ICicsTerminal object for CEDA and CEMT transactions  
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofileName the name of the JVM profile
	 * @param jvmserverType the JVM server type
	 * @return the Galasa JVM server object
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserver newJvmserver(ICicsTerminal cicsTerminal, String name, String group, String jvmprofileName, JvmserverType jvmserverType) throws CicsJvmserverResourceException;
	
	/**
	 * Create a CICS JVMSERVER resource object using the supplied JVM profile
	 * @param cicsTerminal a ICicsTerminal object for CEDA and CEMT transactions
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofile the JVM server profile
	 * @return the Galasa JVM server object
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserver newJvmserver(ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile) throws CicsJvmserverResourceException;
	
	/**
	 * Create a CICS Liberty JVMSERVER resource object using the supplied JVM profile and Liberty server
	 * @param cicsTerminal a ICicsTerminal object for CEDA and CEMT transactions
	 * @param name the JVM server name
	 * @param group the JVM server RDO group name
	 * @param jvmprofile the JVM server profile
	 * @return the Galasa JVM server object
	 * @throws CicsJvmserverResourceException
	 */
	public IJvmserver newLibertyJvmserver(ICicsTerminal cicsTerminal, String name, String group, IJvmprofile jvmprofile, IZosLibertyServer libertyServer) throws CicsJvmserverResourceException;

	/**
	 * Create an empty JVM profile object for use by a {@link IJvmserver}
	 * @param jvmprofileName the name of the JVM profile
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName);
	
	/**
	 * Create a JVM profile object for use by a {@link IJvmserver} using the CICS/Galasa default properties
	 * @param jvmprofileName the name of the JVM profile
	 * @param jvmserverType the JVM server type {@link JvmserverType}
	 * @return the JVM profile
	 * @throws CicsJvmprofileResourceException
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName, JvmserverType jvmserverType) throws CicsJvmprofileResourceException;

	/**
	 * Create an JVM profile object for use by a {@link IJvmserver} using the supplied String content, e.g. previously read  or Galasa artifact
	 * or from zOS UNIX file system.<p>
	 * See {@link IJvmprofile#setProfileValue(String, String)} for format of options
	 * @param jvmprofileName the name of the JVM profile
	 * @return the JVM profile content
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName, String content);

	/**
	 * Create an JVM profile object for use by a {@link IJvmserver} using the supplied {@link HashMap} of options.<p>
	 * See {@link IJvmprofile#setProfileValue(String, String)} for format of options
	 * @param jvmprofileName the name of the JVM profile
	 * @return the JVM profile
	 */
	public IJvmprofile newJvmprofile(String jvmprofileName, HashMap<String, String> content);
}
