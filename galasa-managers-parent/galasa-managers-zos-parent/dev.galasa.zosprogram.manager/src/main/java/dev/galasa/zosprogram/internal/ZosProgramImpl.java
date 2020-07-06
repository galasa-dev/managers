/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramException;

/**
 * Implementation of {@link IZosProgram}
 *
 */
public class ZosProgramImpl implements IZosProgram {
    
    private static final Log logger = LogFactory.getLog(ZosProgramImpl.class);

    private IZosImage image;
    private String name;
    private Language language;
    private String loadlib;

    public ZosProgramImpl(IZosImage image) throws ZosProgramException {
        this.image = image;
    }

    public ZosProgramImpl(String imageTag, String name, Language language, String loadlib) throws ZosProgramException {
        try {
            this.image = ZosProgramManagerImpl.zosManager.getImageForTag(imageTag);
        } catch (ZosManagerException e) {
            throw new ZosProgramException(e);
        }
        this.name = name;
        this.language = language;
        this.loadlib = loadlib;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Language getLanguage() {
        return this.language;
    }

    @Override
    public String getLoadlib() {
        return this.loadlib;
    }

    @Override
    public IZosImage getImage() {
        return this.image;
    }


    @Override
    public String toString() {
        return "name=" + name + ", language=" + language +  ", loadlib=" + loadlib + ", image=" + image;
    }
}
