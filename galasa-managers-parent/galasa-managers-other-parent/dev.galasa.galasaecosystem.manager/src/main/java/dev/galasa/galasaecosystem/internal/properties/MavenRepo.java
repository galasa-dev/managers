/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.galasaecosystem.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;


/**
 * Maven Repository URL
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.maven.repository
 * 
 * @galasa.description The location of the Maven Repository all artifacts will be downloaded from
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values Value URL
 * 
 * @galasa.examples 
 * <code>galasaecosystem.maven.repository=https://nexus.galasa.dev/repository/maven-development</code>
 * 
 */
public class MavenRepo extends CpsProperties {

    public static URL get() throws GalasaEcosystemManagerException {
        try {
            String url = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "maven", "repository") ;
            if (url == null) {
                throw new GalasaEcosystemManagerException("Property galasaecosystem.maven.repository is missing");
            }
            return new URL(url);
        } catch (ConfigurationPropertyStoreException | MalformedURLException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the maven artifact repository", e);
        }
    }
}