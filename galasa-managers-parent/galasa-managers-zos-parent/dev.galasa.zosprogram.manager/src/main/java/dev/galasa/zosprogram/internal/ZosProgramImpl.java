/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramException;

/**
 * Implementation of {@link IZosProgram}
 *
 */
public class ZosProgramImpl implements IZosProgram {
    
    private static final Log logger = LogFactory.getLog(ZosProgramImpl.class);
    
    private Field field;
    private IZosImage image;
    private String name;
    private String location;
    private Language language;
    private boolean isCics;
    private IZosDataset loadlib;
    private String programSource;

    public ZosProgramImpl(Field field, String imageTag, String name, String location, Language language, boolean isCics, String loadlib) throws ZosProgramException {
        this.field = field;
        try {
            initalise(ZosProgramManagerImpl.zosManager.getImageForTag(imageTag), name, location, language, isCics, loadlib);
        } catch (ZosManagerException e) {
            throw new ZosProgramException(e);
        }
    }

    public ZosProgramImpl(IZosImage image, String name, String location, Language language, boolean isCics, String loadlib) throws ZosProgramException {
        initalise(image, name, location, language, isCics, loadlib);
    }
    
    protected void initalise(IZosImage image, String name, String location, Language language, boolean isCics, String loadlib) throws ZosProgramException {
        this.image = image;
        this.name = name;
        this.location = location;
        this.language = language;
        this.isCics = isCics;
                
        setLoadlib(loadlib);
    }

    @Override
    public Field getField() {
        return this.field;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getLocation() {
        return this.location;
    }

    @Override
    public Language getLanguage() {
        return this.language;
    }

    @Override
    public boolean isCics() {
        return this.isCics;
    }

    @Override
    public IZosDataset getLoadlib() {
        return this.loadlib;
    }

    @Override
    public IZosImage getImage() {
        return this.image;
    }

    @Override
    public String getProgramSource() throws ZosProgramException {
        if (this.programSource == null) {
            loadProgramSource();
        }
        return this.programSource;
    }

    @Override
    public String toString() {
        return "name=" + name + ", location=" + location + ", language=" + language +  ", loadlib=" + loadlib + ", image=" + image;
    }
    
    protected void setLoadlib(Object loadlib) throws ZosProgramException {
        if (loadlib != null) {
            if (loadlib instanceof IZosDataset) {
                this.loadlib = (IZosDataset) loadlib;
            } else {
                try {
                    this.loadlib = ZosProgramManagerImpl.zosFileManager.getZosFileHandler().newDataset((String) loadlib, image);
                } catch (ZosFileManagerException e) {
                    throw new ZosProgramException("Unable to instantiate loadlib data set object", e); 
                }
            }
        }
    }

    protected void loadProgramSource() throws ZosProgramException {
        IBundleResources testBundleResources = ZosProgramManagerImpl.getTestBundleResources();
        String sourcePath;
        if (getLocation() == null) {
            sourcePath = getName() + "." + getLanguage().getFileExtension();
        } else {
            if (getLocation().endsWith(getLanguage().getFileExtension())) {
                sourcePath = getLocation();
            } else {
                sourcePath = getLocation() + "/" + getName() + getLanguage().getFileExtension();
            }
        }
        try {
            logger.info("Loading source for " + getLanguage() + " program " + getName() + " from " + sourcePath);
            programSource = testBundleResources.retrieveFileAsString(sourcePath);
        } catch (TestBundleResourceException | IOException e) {
            throw new ZosProgramException("Problem loading program source", e);
        }
    }
}
