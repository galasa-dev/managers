/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.internal.dse;

import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.MasType;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.cicsts.internal.properties.DseJvmProfileDir;
import dev.galasa.cicsts.internal.properties.DseUssHome;
import dev.galasa.cicsts.internal.properties.DseVersion;
import dev.galasa.cicsts.spi.BaseCicsImpl;
import dev.galasa.zos.IZosImage;

public class DseCicsImpl extends BaseCicsImpl {

    private ProductVersion version;
	private String usshome;
	private String jvmProfileDir;

    public DseCicsImpl(CicstsManagerImpl cicstsManager, String cicsTag, IZosImage image, String applid)
            throws CicstsManagerException {
        super(cicstsManager, cicsTag, image, applid, MasType.NONE);
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

    @Override
    public String getUssHome() throws CicstsManagerException {
    	if (this.usshome == null) {
    		this.usshome = DseUssHome.get(this.getTag());
    		if (this.usshome == null) {
    			throw new CicstsManagerException("A value for USSHOME was missing for DSE tag " + this.getTag());
    		}
    	}
        return this.usshome;
    }

    @Override
    public String getJvmProfileDir() throws CicstsManagerException {
    	if (this.jvmProfileDir == null) {
    		this.jvmProfileDir = DseJvmProfileDir.get(this.getTag());
    		if (this.jvmProfileDir == null) {
    			throw new CicstsManagerException("A value for JVMPROFILEDIR was missing for DSE tag " + this.getTag());
    		}
    	}
        return this.jvmProfileDir;
    }

    @Override
    public boolean isProvisionStart() {
        return true;  // DSE regions are assumed to be started before the test runs
    }

    @Override
    public void startup() throws CicstsManagerException {
        throw new CicstsManagerException("Unable to startup DSE CICS TS regions");
        
    }

    @Override
    public void shutdown() throws CicstsManagerException {
        throw new CicstsManagerException("Unable to shutdown DSE CICS TS regions");
    }

    @Override
    public void submitRuntimeJcl() throws CicstsManagerException {
        throw new CicstsManagerException("Unable to submit DSE CICS TS regions");
    }

    @Override
    public boolean hasRegionStarted() throws CicstsManagerException {
        throw new CicstsManagerException("Unable to check DSE CICS TS regions has started");
    }

}
