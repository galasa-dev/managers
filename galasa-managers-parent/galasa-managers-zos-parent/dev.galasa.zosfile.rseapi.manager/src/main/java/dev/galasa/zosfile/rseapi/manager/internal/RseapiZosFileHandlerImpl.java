/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosVSAMDataset;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.ZosVSAMDatasetException;

/**
 * Implementation of {@link IZosFileHandler} using zOS/MF
 *
 */
public class RseapiZosFileHandlerImpl implements IZosFileHandler {

    private List<RseapiZosDatasetImpl> zosDatasets = new ArrayList<>();
    private List<RseapiZosDatasetImpl> zosDatasetsForCleanup = new ArrayList<>();
    private List<RseapiZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();
    private List<RseapiZosVSAMDatasetImpl> zosVsamDatasetsForCleanup = new ArrayList<>();
    private List<RseapiZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();
    private List<RseapiZosUNIXFileImpl> zosUnixFilesForCleanup = new ArrayList<>();
    private String fieldName;

    public RseapiZosFileHandlerImpl() {
        this("INTERNAL");
    }

    public RseapiZosFileHandlerImpl(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public IZosDataset newDataset(String dsname, IZosImage image) throws ZosDatasetException {
        RseapiZosDatasetImpl zosDataset = new RseapiZosDatasetImpl(image, dsname);
        zosDatasets.add(zosDataset);
        return zosDataset;
    }

    @Override
    public IZosUNIXFile newUNIXFile(String fullFilePath, IZosImage image) throws ZosUNIXFileException {
        RseapiZosUNIXFileImpl zosUnixFile = new RseapiZosUNIXFileImpl(image, fullFilePath);
        zosUnixFiles.add(zosUnixFile);
        return zosUnixFile;
    }

    @Override
    public IZosVSAMDataset newVSAMDataset(String dsname, IZosImage image) throws ZosVSAMDatasetException {
        RseapiZosVSAMDatasetImpl zosVsamDataset = new RseapiZosVSAMDatasetImpl(image, dsname);
        this.zosVsamDatasets.add(zosVsamDataset);
        return zosVsamDataset;
    }
    
    public void cleanup(boolean testComplete) throws ZosFileManagerException {
        cleanupDatasets(testComplete);
        cleanupVsamDatasets(testComplete);
        cleanupUnixFiles(testComplete);
    }
    
    public void cleanupDatasets(boolean testComplete) throws ZosFileManagerException {
        Iterator<RseapiZosDatasetImpl> datasetIterator = this.zosDatasets.iterator();
        while (datasetIterator.hasNext()) {
            RseapiZosDatasetImpl zosDataset = datasetIterator.next();
            if (zosDataset.created() && zosDataset.exists()) {
                if (!zosDataset.isTemporary()) {
                    zosDataset.saveToResultsArchive();
                }
                if (zosDataset.retainToTestEnd()) {
                    this.zosDatasetsForCleanup.add(zosDataset);
                } else {
                    zosDataset.delete();
                }
            }
            datasetIterator.remove();
        }
        
        if (testComplete) {
            cleanupDatasetsTestComplete();
        }
    }
    
    protected void cleanupDatasetsTestComplete() throws ZosDatasetException {
        Iterator<RseapiZosDatasetImpl> datasetForCleanupIterator = this.zosDatasetsForCleanup.iterator();
        while (datasetForCleanupIterator.hasNext()) {
            RseapiZosDatasetImpl zosDataset = datasetForCleanupIterator.next();
            if (zosDataset.created() && zosDataset.exists()) {
                if (!zosDataset.isTemporary()) {
                    zosDataset.saveToResultsArchive();
                }
                zosDataset.delete();
            }
        }
    }

    public void cleanupVsamDatasets(boolean testComplete) throws ZosFileManagerException {
        Iterator<RseapiZosVSAMDatasetImpl> vsamDatasetIterator = this.zosVsamDatasets.iterator();
        while (vsamDatasetIterator.hasNext()) {
            RseapiZosVSAMDatasetImpl zosVsamDataset = vsamDatasetIterator.next();
            if (zosVsamDataset.created() && zosVsamDataset.exists()) {
                zosVsamDataset.saveToResultsArchive();
                if (zosVsamDataset.retainToTestEnd()) {
                    this.zosVsamDatasetsForCleanup.add(zosVsamDataset);
                } else {
                    zosVsamDataset.delete();
                }
            }
            vsamDatasetIterator.remove();
        }
        
        if (testComplete) {
            cleanupVsamDatasetsTestComplete();
        }
    }
        
    protected void cleanupVsamDatasetsTestComplete() throws ZosVSAMDatasetException {
        Iterator<RseapiZosVSAMDatasetImpl> vsamDatasetForCleanupIterator = this.zosVsamDatasetsForCleanup.iterator();
        while (vsamDatasetForCleanupIterator.hasNext()) {
            RseapiZosVSAMDatasetImpl zosVsamDataset = vsamDatasetForCleanupIterator.next();
            if (zosVsamDataset.created() && zosVsamDataset.exists()) {
                zosVsamDataset.saveToResultsArchive();
                zosVsamDataset.delete();
            }
        }
    }

    public void cleanupUnixFiles(boolean testComplete) throws ZosFileManagerException {
        Iterator<RseapiZosUNIXFileImpl> unixFileIterator = this.zosUnixFiles.iterator();
        while (unixFileIterator.hasNext()) {
            RseapiZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
            if (zosUnixFile.created() && !zosUnixFile.deleted() && zosUnixFile.exists()) {
                zosUnixFile.saveToResultsArchive();
                if (!zosUnixFile.retainToTestEnd()) {
                    zosUnixFile.delete();
                }
            }
            this.zosUnixFilesForCleanup.add(zosUnixFile);
            unixFileIterator.remove();
        }
        
        if (testComplete) {
            cleanupUnixFilesTestComplete();
        }
    }

    protected void cleanupUnixFilesTestComplete() {
        Iterator<RseapiZosUNIXFileImpl> zosUnixFilesForCleanupIterator = this.zosUnixFilesForCleanup.iterator();
        while (zosUnixFilesForCleanupIterator.hasNext()) {
            RseapiZosUNIXFileImpl zosUnixFile = zosUnixFilesForCleanupIterator.next();
            zosUnixFile.cleanCreatedPath();
        }
    }

    @Override
    public String toString() {
        return this.fieldName;
    }
}
