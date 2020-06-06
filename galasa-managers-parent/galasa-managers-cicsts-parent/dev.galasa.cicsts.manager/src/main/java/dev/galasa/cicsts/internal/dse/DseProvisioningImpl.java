package dev.galasa.cicsts.internal.dse;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.cicsts.internal.properties.DseApplid;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.cicsts.spi.ICicsRegionProvisioner;

public class DseProvisioningImpl implements ICicsRegionProvisioner {
    private static final Log logger = LogFactory.getLog(DseProvisioningImpl.class);

    private final CicstsManagerImpl cicstsManager;

    private HashMap<String, DseCicsImpl> dseCicsRegions = new HashMap<>();

    private final boolean enabled;

    public DseProvisioningImpl(CicstsManagerImpl cicstsManager) throws CicstsManagerException {
        this.cicstsManager = cicstsManager;

        String provisionType = this.cicstsManager.getProvisionType();
        switch (provisionType) {
            case "DSE":
            case "MIXED":
                this.enabled = true;
                break;
            default:
                this.enabled = false;
        }
    }

    @Override
    public ICicsRegionProvisioned provision(@NotNull String cicsTag, @NotNull String imageTag,
            @NotNull List<Annotation> annotations) throws ManagerException {
        if (!this.enabled) {
            return null;
        }
        
        String applid = DseApplid.get(cicsTag);
        if (applid == null) {
            return null;
        }

        DseCicsImpl cicsRegion = new DseCicsImpl(this.cicstsManager, cicsTag, imageTag, applid);

        return cicsRegion;
    }

    @NotNull
    public List<ICicsRegionProvisioned> getRegions() {
        ArrayList<ICicsRegionProvisioned> regions = new ArrayList<>();
        regions.addAll(dseCicsRegions.values());
        return regions;
    }

    public ICicsRegionProvisioned getTaggedRegion(String tag) {
        DseCicsImpl region = this.dseCicsRegions.get(tag);

        if (region != null) {
            logger.info("Provisioned DSE " + region + " for tag " + tag);
        }

        return region;
    }

}
