/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import dev.galasa.cicsts.MasType;
import dev.galasa.zos.IZosImage;

public abstract class BaseCicsImpl implements ICicsRegionProvisioned {

    private final String cicsTag;
    private final String applid;
    private final IZosImage zosImage;
    private final MasType   masType;

    private int lastTerminalId;
    

    public BaseCicsImpl(ICicstsManagerSpi cicstsManager, String cicsTag, IZosImage zosImage, String applid, MasType masType) {
        this.cicsTag = cicsTag;
        this.applid = applid;
        this.zosImage = zosImage;
        this.masType  = masType;
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
    public IZosImage getZosImage() {
        return this.zosImage;
    }

    @Override
    public String toString() {
        return "CICS Region[" + this.applid + "]";
    }

    @Override
    public String getNextTerminalId() {
        lastTerminalId++;
        return this.applid + "_" + Integer.toString(lastTerminalId);
    }
    
    @Override
    public MasType getMasType() {
        return this.masType;
    }

}
