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
 * zOS Program LanguageExtended specific custom compile syslibs
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosprogram.[language].[imageid].compile.syslibs
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
 * <code>zosprogram.cobol.MVSA.compile.syslibs=TEAM.COPYBOOK</code><br>
 * <code>zosprogram.cobol.compile.syslibs=COMPANY.COPYBOOK,TEAM.COPYBOOK</code>
 *
 */
public class ProgramLanguageCompileSyslibs extends CpsProperties {

    public static List<String> get(String imageId, Language language) throws ZosProgramManagerException {
        try {
            return getStringList(ZosProgramPropertiesSingleton.cps(), language.toString().toLowerCase(), "compile.syslibs", imageId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Problem asking the CPS for the zOS program " + language + " custom compile syslibs for zOS image "  + imageId, e);
        }
    }

}
