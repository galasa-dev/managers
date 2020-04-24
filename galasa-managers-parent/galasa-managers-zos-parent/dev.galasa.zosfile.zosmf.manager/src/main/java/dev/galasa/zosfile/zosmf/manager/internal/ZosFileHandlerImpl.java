/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

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
public class ZosFileHandlerImpl implements IZosFileHandler {

    private List<ZosDatasetImpl> zosDatasets = new ArrayList<>();
    private List<ZosDatasetImpl> zosDatasetsForCleanup = new ArrayList<>();
    private List<ZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();
    private List<ZosVSAMDatasetImpl> zosVsamDatasetsForCleanup = new ArrayList<>();
    private List<ZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();
    private List<ZosUNIXFileImpl> zosUnixFilesForCleanup = new ArrayList<>();
    private String fieldName;

    public ZosFileHandlerImpl() {
        this("INTERNAL");
    }

    public ZosFileHandlerImpl(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public IZosDataset newDataset(String dsname, IZosImage image) throws ZosDatasetException {
        ZosDatasetImpl zosDataset = new ZosDatasetImpl(image, dsname);
        zosDatasets.add(zosDataset);
        return zosDataset;
    }

    @Override
    public IZosUNIXFile newUNIXFile(String fullFilePath, IZosImage image) throws ZosUNIXFileException {
        ZosUNIXFileImpl zosUnixFile = new ZosUNIXFileImpl(image, fullFilePath);
        zosUnixFiles.add(zosUnixFile);
        return zosUnixFile;
    }

    @Override
    public IZosVSAMDataset newVSAMDataset(String dsname, IZosImage image) throws ZosVSAMDatasetException {
        ZosVSAMDatasetImpl zosVsamDataset = new ZosVSAMDatasetImpl(image, dsname);
        this.zosVsamDatasets.add(zosVsamDataset);
        return zosVsamDataset;
    }
    
    public void cleanupEndOfTestMethod() throws ZosFileManagerException {
        cleanupDatasetsEndOfTestMethod();
        cleanupVsamDatasetsEndOfTestMethod();
        cleanupFilesEndOfTestMethod();
    }
    
    public void cleanupEndOfClass() throws ZosFileManagerException {
        cleanupDatasetsEndOfTestClass();
        cleanupVsamDatasetsEndOfTestClass();
        cleanupFilesEndOfTestClass();
    }
    
    public void cleanupDatasetsEndOfTestMethod() throws ZosFileManagerException {
        Iterator<ZosDatasetImpl> datasetIterator = this.zosDatasets.iterator();
        while (datasetIterator.hasNext()) {
            ZosDatasetImpl zosDataset = datasetIterator.next();
            if (zosDataset.created() && zosDataset.exists()) {
                if (zosDataset.isTemporary()) {
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
    }

    public void cleanupDatasetsEndOfTestClass() throws ZosFileManagerException {
        Iterator<ZosDatasetImpl> datasetForCleanupIterator = this.zosDatasetsForCleanup.iterator();
        while (datasetForCleanupIterator.hasNext()) {
            ZosDatasetImpl zosDataset = datasetForCleanupIterator.next();
            if (zosDataset.created() && zosDataset.exists()) {
                zosDataset.saveToResultsArchive();
                zosDataset.delete();
            }
        }
    }
    
    public void cleanupVsamDatasetsEndOfTestMethod() throws ZosFileManagerException {
        Iterator<ZosVSAMDatasetImpl> vsamDatasetIterator = this.zosVsamDatasets.iterator();
        while (vsamDatasetIterator.hasNext()) {
            ZosVSAMDatasetImpl zosVsamDataset = vsamDatasetIterator.next();
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
    }
    
    public void cleanupVsamDatasetsEndOfTestClass() throws ZosFileManagerException {
        Iterator<ZosVSAMDatasetImpl> vsamDatasetForCleanupIterator = this.zosVsamDatasetsForCleanup.iterator();
        while (vsamDatasetForCleanupIterator.hasNext()) {
            ZosVSAMDatasetImpl zosVsamDataset = vsamDatasetForCleanupIterator.next();
            if (zosVsamDataset.created() && zosVsamDataset.exists()) {
                zosVsamDataset.saveToResultsArchive();
                zosVsamDataset.delete();
            }
        }
    }
        
    public void cleanupFilesEndOfTestMethod() throws ZosFileManagerException {
        Iterator<ZosUNIXFileImpl> unixFileIterator = this.zosUnixFiles.iterator();
        while (unixFileIterator.hasNext()) {
            ZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
            if (zosUnixFile.created() && !zosUnixFile.deleted() && zosUnixFile.exists()) {
                zosUnixFile.saveToResultsArchive();
                if (!zosUnixFile.retainToTestEnd()) {
                    zosUnixFile.delete();
                }
            }
            this.zosUnixFilesForCleanup.add(zosUnixFile);
            unixFileIterator.remove();
        }
    }
    
    public void cleanupFilesEndOfTestClass() {
        Iterator<ZosUNIXFileImpl> unixFileIterator = this.zosUnixFilesForCleanup.iterator();
        while (unixFileIterator.hasNext()) {
            ZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
            zosUnixFile.cleanCreatedPath();
        }
    }

    @Override
    public String toString() {
        return this.fieldName;
    }
}
