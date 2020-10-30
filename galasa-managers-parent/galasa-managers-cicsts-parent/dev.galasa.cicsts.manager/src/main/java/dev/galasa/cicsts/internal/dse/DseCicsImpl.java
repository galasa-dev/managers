/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.internal.dse;

import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.cicsts.internal.properties.DseVersion;
import dev.galasa.cicsts.spi.BaseCicsImpl;
import dev.galasa.zos.IZosImage;

public class DseCicsImpl extends BaseCicsImpl {

    private ProductVersion version;

    public DseCicsImpl(CicstsManagerImpl cicstsManager, String cicsTag, IZosImage image, String applid)
            throws CicstsManagerException {
        super(cicstsManager, cicsTag, image, applid);
    }

    @Override
    public ProductVersion getVersion() throws CicstsManagerException {
        if (this.version != null) {
            return this.version;
        }

        String versionString = DseVersion.get(this.getTag());

        if (versionString == null) {
            throw new CicstsManagerException("The version was missing for DSE tag " + this.getTag());
        }

        try {
            this.version = ProductVersion.parse(versionString);
        } catch (Exception e) {
            throw new CicstsManagerException("Invalid version string for DSE tag " + this.getTag() + ", format should be 0.0.0", e);
        }

        return this.version;
    }



}
