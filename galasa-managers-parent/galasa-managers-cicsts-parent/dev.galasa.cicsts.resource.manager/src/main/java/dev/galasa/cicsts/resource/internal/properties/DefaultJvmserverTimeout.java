/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.resource.internal.properties;

import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;

/**
 * The Default Timeout value in milliseconds for JVM servers on a zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cicsresource.jvmserver.[image].default.timeout
 * 
 * @galasa.description Provides a value for the default timeout for JVM servers on a zOS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default 120000 milliseconds
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>cicsresource.jvmserver.[image].default.timeout=120000</code><br>
 *
 */
public class DefaultJvmserverTimeout extends CpsProperties {

    public static int get(IZosImage image) throws CicsResourceManagerException {
    	return getIntWithDefault(CicstsResourcePropertiesSingleton.cps(), 120000, "jvmserver", "default.timeout", image.getImageID());
    }
}
