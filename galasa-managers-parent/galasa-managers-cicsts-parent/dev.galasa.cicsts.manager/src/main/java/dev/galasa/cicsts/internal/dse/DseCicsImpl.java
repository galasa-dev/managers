package dev.galasa.cicsts.internal.dse;

import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.cicsts.internal.properties.DseVersion;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

public class DseCicsImpl implements ICicsRegionProvisioned {

    private final String cicsTag;
    private final String applid;
    private final IZosImage zosImage;

    private ProductVersion version;

    public DseCicsImpl(CicstsManagerImpl cicstsManager, String cicsTag, String imageTag, String applid)
            throws CicstsManagerException {
        this.cicsTag = cicsTag;
        this.applid = applid;

        try {
            this.zosImage = cicstsManager.getZosManager().getImageForTag(imageTag);
        } catch (ZosManagerException e) {
            throw new CicstsManagerException("Unable to locate zOS Image tagged " + imageTag, e);
        }

    }

    @Override
    public String getTag() {
        return this.cicsTag;
    }

    @Override
    public String getApplid() {
        return this.applid;
    }

    @Override
    public ProductVersion getVersion() throws CicstsManagerException {
        if (this.version != null) {
            return this.version;
        }

        String versionString = DseVersion.get(this.cicsTag);

        if (versionString == null) {
            throw new CicstsManagerException("The version was missing for DSE tag " + this.cicsTag);
        }

        try {
            this.version = ProductVersion.parse(versionString);
        } catch (Exception e) {
            throw new CicstsManagerException("Invalid version string for DSE tag " + this.cicsTag + ", format should be 0.0.0", e);
        }

        return this.version;
    }

    @Override
    public IZosImage getZosImage() {
        return this.zosImage;
    }

    @Override
    public String toString() {
        return "CICS Region[" + this.applid + "]";
    }

}
