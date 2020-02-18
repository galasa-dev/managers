/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.galasaecosystem.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;


/**
 * Maven Artifact Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.maven.version
 * 
 * @galasa.description The versions of the Maven artifacts to be used with the Ecosystem
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid maven version literial
 * 
 * @galasa.examples 
 * <code>galasaecosystem.maven.version=0.4.0</code>
 * 
 */
public class MavenVersion extends CpsProperties {

    public static String get() throws GalasaEcosystemManagerException {
        try {
            String version = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "maven", "version") ;
            if (version == null) {
                throw new GalasaEcosystemManagerException("Property galasaecosystem.maven.version is missing");
            }
            return version;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the maven artifact version", e);
        }
    }
}