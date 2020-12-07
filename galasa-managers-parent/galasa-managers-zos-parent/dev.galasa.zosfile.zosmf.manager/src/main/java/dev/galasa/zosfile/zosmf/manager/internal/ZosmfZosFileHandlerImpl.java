/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosVSAMDataset;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosmf.spi.IZosmfManagerSpi;

/**
 * Implementation of {@link IZosFileHandler} using zOS/MF
 *
 */
public class ZosmfZosFileHandlerImpl implements IZosFileHandler {

    private List<ZosmfZosDatasetImpl> zosDatasets = new ArrayList<>();
    private List<ZosmfZosDatasetImpl> zosDatasetsForCleanup = new ArrayList<>();
    private List<ZosmfZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();
    private List<ZosmfZosVSAMDatasetImpl> zosVsamDatasetsForCleanup = new ArrayList<>();
    private List<ZosmfZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();
    private List<ZosmfZosUNIXFileImpl> zosUnixFilesForCleanup = new ArrayList<>();
    private String fieldName;
    
	private ZosmfZosFileManagerImpl zosFileManager;
	public ZosmfZosFileManagerImpl getZosFileManager() {
		return zosFileManager;
	}
	public IZosManagerSpi getZosManager() {
		return zosFileManager.getZosManager();
	}

	public IZosmfManagerSpi getZosmfManager() {
		return zosFileManager.getZosmfManager();
	}
    
    private static final Log logger = LogFactory.getLog(ZosmfZosFileHandlerImpl.class);

    public ZosmfZosFileHandlerImpl(ZosmfZosFileManagerImpl zosFileManager) {
        this(zosFileManager, "INTERNAL");
    }

    public ZosmfZosFileHandlerImpl(ZosmfZosFileManagerImpl zosFileManager, String fieldName) {
    	this.zosFileManager = zosFileManager;
        this.fieldName = fieldName;
    }

    @Override
    public IZosDataset newDataset(String dsname, IZosImage image) throws ZosDatasetException {
        ZosmfZosDatasetImpl zosDataset = new ZosmfZosDatasetImpl(this, image, dsname);
        zosDatasets.add(zosDataset);
        return zosDataset;
    }

    @Override
    public IZosUNIXFile newUNIXFile(String fullFilePath, IZosImage image) throws ZosUNIXFileException {
        ZosmfZosUNIXFileImpl zosUnixFile = new ZosmfZosUNIXFileImpl(this, image, fullFilePath);
        zosUnixFiles.add(zosUnixFile);
        return zosUnixFile;
    }

    @Override
    public IZosVSAMDataset newVSAMDataset(String dsname, IZosImage image) throws ZosVSAMDatasetException {
        ZosmfZosVSAMDatasetImpl zosVsamDataset = new ZosmfZosVSAMDatasetImpl(this, image, dsname);
        this.zosVsamDatasets.add(zosVsamDataset);
        return zosVsamDataset;
    }
    
    public void cleanup(boolean testComplete) throws ZosFileManagerException {
        cleanupDatasets(testComplete);
        cleanupVsamDatasets(testComplete);
        cleanupUnixFiles(testComplete);
    }
    
    public void cleanupDatasets(boolean testComplete) throws ZosFileManagerException {
        Iterator<ZosmfZosDatasetImpl> datasetIterator = this.zosDatasets.iterator();
        while (datasetIterator.hasNext()) {
            ZosmfZosDatasetImpl zosDataset = datasetIterator.next();
            try {
	            if (zosDataset.created() && zosDataset.exists()) {
	                if (!zosDataset.isTemporary() && zosDataset.shouldArchive()) {
	                    zosDataset.saveToResultsArchive("TODO");
	                }
	                if (zosDataset.retainToTestEnd()) {
	                    this.zosDatasetsForCleanup.add(zosDataset);
	                } else {
	                    zosDataset.delete();
	                }
	            }
			} catch (ZosDatasetException e) {
				logger.error("Problem in cleanup phase", e);
			}
            datasetIterator.remove();
        }
        
        if (testComplete) {
            cleanupDatasetsTestComplete();
        }
    }
    
    protected void cleanupDatasetsTestComplete() throws ZosDatasetException {
        Iterator<ZosmfZosDatasetImpl> datasetForCleanupIterator = this.zosDatasetsForCleanup.iterator();
        while (datasetForCleanupIterator.hasNext()) {
            ZosmfZosDatasetImpl zosDataset = datasetForCleanupIterator.next();
            try {
	            if (zosDataset.created() && zosDataset.exists() && zosDataset.shouldArchive()) {
	                if (!zosDataset.isTemporary()) {
	                    zosDataset.saveToResultsArchive("TODO");//TODO
	                }
	                zosDataset.delete();
	            }
			} catch (ZosDatasetException e) {
				logger.error("Problem in cleanup phase", e);
			}
        }
    }

    public void cleanupVsamDatasets(boolean testComplete) throws ZosFileManagerException {
        Iterator<ZosmfZosVSAMDatasetImpl> vsamDatasetIterator = this.zosVsamDatasets.iterator();
        while (vsamDatasetIterator.hasNext()) {
            ZosmfZosVSAMDatasetImpl zosVsamDataset = vsamDatasetIterator.next();
            try {
	            if (zosVsamDataset.created() && zosVsamDataset.exists() && zosVsamDataset.shouldArchive()) {
	                zosVsamDataset.saveToResultsArchive("TODO");//TODO
	                if (zosVsamDataset.retainToTestEnd()) {
	                    this.zosVsamDatasetsForCleanup.add(zosVsamDataset);
	                } else {
	                    zosVsamDataset.delete();
	                }
	            }
			} catch (ZosVSAMDatasetException e) {
				logger.error("Problem in cleanup phase", e);
			}
            vsamDatasetIterator.remove();
        }
        
        if (testComplete) {
            cleanupVsamDatasetsTestComplete();
        }
    }
        
    protected void cleanupVsamDatasetsTestComplete() throws ZosVSAMDatasetException {
        Iterator<ZosmfZosVSAMDatasetImpl> vsamDatasetForCleanupIterator = this.zosVsamDatasetsForCleanup.iterator();
        while (vsamDatasetForCleanupIterator.hasNext()) {
        	ZosmfZosVSAMDatasetImpl zosVsamDataset = vsamDatasetForCleanupIterator.next();
            try {
	            if (zosVsamDataset.created() && zosVsamDataset.exists() && zosVsamDataset.shouldArchive()) {
	                zosVsamDataset.saveToResultsArchive("TODO");//TODO
	                zosVsamDataset.delete();
	            }
			} catch (ZosVSAMDatasetException e) {
				logger.error("Problem in cleanup phase", e);
			}
        }
    }

    public void cleanupUnixFiles(boolean testComplete) throws ZosFileManagerException {
        Iterator<ZosmfZosUNIXFileImpl> unixFileIterator = this.zosUnixFiles.iterator();
        while (unixFileIterator.hasNext()) {
            ZosmfZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
            try {
				if (zosUnixFile.created() && !zosUnixFile.deleted() && zosUnixFile.exists() && zosUnixFile.shouldArchive()) {
	                zosUnixFile.saveToResultsArchive("TODO");//TODO
	                if (!zosUnixFile.retainToTestEnd()) {
	                	if (zosUnixFile.isDirectory()) {
	                		zosUnixFile.directoryDeleteNonEmpty();
	                	} else {
	                		zosUnixFile.delete();
	                	}
	                }
	            }
			} catch (ZosUNIXFileException e) {
				logger.error("Problem in cleanup phase", e);
			}
            this.zosUnixFilesForCleanup.add(zosUnixFile);
            unixFileIterator.remove();
        }
        
        if (testComplete) {
            cleanupUnixFilesTestComplete();
        }
    }

    protected void cleanupUnixFilesTestComplete() {
        Iterator<ZosmfZosUNIXFileImpl> zosUnixFilesForCleanupIterator = this.zosUnixFilesForCleanup.iterator();
        while (zosUnixFilesForCleanupIterator.hasNext()) {
            ZosmfZosUNIXFileImpl zosUnixFile = zosUnixFilesForCleanupIterator.next();
            zosUnixFile.cleanCreatedPath();
        }
    }

    @Override
    public String toString() {
        return this.fieldName;
    }
}
