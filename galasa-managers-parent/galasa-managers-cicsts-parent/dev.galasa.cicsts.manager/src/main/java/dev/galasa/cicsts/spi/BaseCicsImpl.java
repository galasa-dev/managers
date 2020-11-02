/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import dev.galasa.zos.IZosImage;

public abstract class BaseCicsImpl implements ICicsRegionProvisioned {

    private final String cicsTag;
    private final String applid;
    private final IZosImage zosImage;

    private int lastTerminalId;

    public BaseCicsImpl(ICicstsManagerSpi cicstsManager, String cicsTag, IZosImage zosImage, String applid) {
        this.cicsTag = cicsTag;
        this.applid = applid;
        this.zosImage = zosImage;
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

}
