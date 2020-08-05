/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;

/**
 * zOS Program LanguageExtended specific custom link syslibs
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.[language].[imageid].link.syslibs
 * 
 * @galasa.description zOS Program LanguageExtended data set prefix
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values
 * 
 * @galasa.examples 
 * <code>zosprogram.cobol.MVSA.link.syslibs=TEAM.LOADLIB</code><br>
 * <code>zosprogram.cobol.link.syslibs=COMPANY.LOADLIB,TEAM.LOADLIB</code>
 *
 */
public class ProgramLanguageLinkSyslibs extends CpsProperties {

    public static List<String> get(String imageId, Language language) throws ZosProgramManagerException {
        try {
            return getStringList(ZosProgramPropertiesSingleton.cps(), language.toString().toLowerCase(), "link.syslibs", imageId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program " + language + " custom link syslibs for zOS image "  + imageId, e);
        }
    }

}
