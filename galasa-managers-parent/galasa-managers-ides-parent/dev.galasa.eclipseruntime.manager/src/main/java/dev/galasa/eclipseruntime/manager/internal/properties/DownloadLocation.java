/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.eclipseruntime.manager.internal.properties;

import dev.galasa.CpuArchitecture;
import dev.galasa.OperatingSystem;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.JavaType;
import dev.galasa.java.JavaVersion;
import dev.galasa.eclipseruntime.EclipseManagerException;
import dev.galasa.eclipseruntime.EclipseType;
import dev.galasa.eclipseruntime.EclipseVersion;

/**
 * Eclipse Archive download location
**/

public class DownloadLocation extends CpsProperties {

    public static String get(EclipseType type,
    		EclipseVersion version,
    		OperatingSystem os,
    		CpuArchitecture cpuType) throws EclipseManagerException {
        
        try {
            return getStringNulled(EclipsePropertiesSingleton.cps(), type.name(), "location",  os.name(), cpuType.name(), version.getFriendlyString()) ;
        } catch (ConfigurationPropertyStoreException e) {
            throw new EclipseManagerException("Problem retrieving the eclipse archive " + e);
        }
    }
}