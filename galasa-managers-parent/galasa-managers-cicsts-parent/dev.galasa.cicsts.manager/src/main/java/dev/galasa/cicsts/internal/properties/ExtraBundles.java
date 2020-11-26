/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.internal.properties;

import java.util.Iterator;
import java.util.List;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Extra bundles required to implement the CICS TS Manager
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cicsts.extra.bundles
 * 
 * @galasa.description The symbolic names of any bundles that need to be loaded
 *                     with the CICS TS Manager
 * 
 * @galasa.required No
 * 
 * @galasa.default dev.galasa.cicsts.ceci.manager,dev.galasa.cicsts.ceda.manager,dev.galasa.cicsts.cemt.manager
 * 
 * @galasa.valid_values bundle symbolic names comma separated
 * 
 * @galasa.examples <code>cicsts.extra.bundles=org.example.cicsts.provisioning</code><br>
 *
 */
public class ExtraBundles extends CpsProperties {

    public static List<String> get() throws CicstsManagerException {
        try {
            List<String> list = getStringList(CicstsPropertiesSingleton.cps(), "extra", "bundles");

            if (list.isEmpty()) {
                list.add("dev.galasa.cicsts.ceci.manager");
                list.add("dev.galasa.cicsts.ceda.manager");
                list.add("dev.galasa.cicsts.cemt.manager");
            } else {
                Iterator<String> i = list.iterator();
                while(i.hasNext()) {
                    String bundle = i.next();
                    if (bundle.equalsIgnoreCase("none")) {
                        i.remove();
                    }
                }
            }
            
            return list;
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Problem asking CPS for the CICS TS extra bundles", e); 
        }
    }
}
