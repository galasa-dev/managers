/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.windows.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.windows.WindowsManagerException;

/**
 * Windows Hostname IPV4
 * <p>
 * The IPV4 hostname of the Windows DSE server
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * windows.dse.tag.[tag].hostname4=cics.ibm.com
 * </p>
 * <p>
 * There is no default
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public class Hostname4 extends CpsProperties {

    public static String get(String tag) throws WindowsManagerException, ConfigurationPropertyStoreException {
        return getStringNulled(WindowsPropertiesSingleton.cps(), "dse.tag." + tag, "hostname4");
    }

}
