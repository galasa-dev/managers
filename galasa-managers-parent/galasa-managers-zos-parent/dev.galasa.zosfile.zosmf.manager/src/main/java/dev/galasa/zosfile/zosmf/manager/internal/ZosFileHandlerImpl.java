package dev.galasa.zosfile.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosVSAMDataset;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;

/**
 * Implementation of {@link IZosFileHandler} using zOS/MF
 *
 */
public class ZosFileHandlerImpl implements IZosFileHandler {

	private List<ZosDatasetImpl> zosDatasets = new ArrayList<>();
	private List<ZosDatasetImpl> zosDatasetsForCleanup = new ArrayList<>();
	private List<ZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();
	private List<ZosUNIXFileImpl> zosUnixFilesForCleanup = new ArrayList<>();
	private String fieldName;

	public ZosFileHandlerImpl(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public IZosDataset newDataset(String dsName, IZosImage image) throws ZosDatasetException {
		ZosDatasetImpl zosDataset = new ZosDatasetImpl(image, dsName);
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
	public void deleteDatasetsByPrefix(String prefix, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void apfAuthorise(IZosDataset dataset, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAPFAuthorised(IZosDataset dataset, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IZosVSAMDataset newVSAMDataset(String dsName, IZosImage image) {
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newKSDS(String dsName, IZosImage image) {
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newESDS(String dsName, IZosImage image) {
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newRRDS(String dsName, IZosImage image) {
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public void define(IZosVSAMDataset vsam, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void store(IZosVSAMDataset vsam, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(IZosVSAMDataset vsam, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean exists(IZosVSAMDataset vsam, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IZosUNIXFile storeResourcesFile(String resourcePath, String unixPath, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosUNIXFileException {
		return new ZosUNIXFileImpl(image, unixPath);
	}

	@Override
	public IZosDataset storeResourcesDataset(String resourcePath, String dsName, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosDatasetException {
		return new ZosDatasetImpl(image, dsName);
	}

	@Override
	public IZosUNIXFile storeResourcesDirectory(String resourcePath, String unixPath, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosUNIXFileException {
		return new ZosUNIXFileImpl(image, unixPath);
	}

	@Override
	public IZosDataset storeResourcesPDS(String resourcePath, String dsName, DSType pdsType, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosDatasetException {
		return new ZosDatasetImpl(image, dsName);
	}

	@Override
	public void storeFileToTestOutput(IZosUNIXFile file, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void storeDatasetToTestOutput(IZosDataset dataset, IZosImage image) throws ZosDatasetException {
		throw new UnsupportedOperationException();
	}

	public void cleanupEndOfMethod() throws ZosFileManagerException {
		cleanupDatasets(false);
		cleanupFiles(false);
	}
	
	public void cleanupEndOfTest() throws ZosFileManagerException {
		cleanupDatasets(true);
		cleanupFiles(true);
	}
	
	/**
	 * Clean up any existing data sets
	 * @throws ZosFileManagerException
	 */
	public void cleanupDatasets(boolean endOfTest) throws ZosFileManagerException {
		if (!endOfTest) {		
			Iterator<ZosDatasetImpl> datasetIterator = this.zosDatasets.iterator();
			while (datasetIterator.hasNext()) {
				ZosDatasetImpl zosDataset = datasetIterator.next();
				if (zosDataset.created() && zosDataset.exists()) {
					zosDataset.saveToResultsArchive();
					if (zosDataset.retainToTestEnd()) {
						this.zosDatasetsForCleanup.add(zosDataset);
					} else {
						zosDataset.delete();
					}
				}
				datasetIterator.remove();
			}
		}
	}
		
	/**
	 * Clean up any existing UNIX files
	 * @throws ZosFileManagerException
	 */
	public void cleanupFiles(boolean endOfTest) throws ZosFileManagerException {
		if (!endOfTest) {
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
		} else {
			Iterator<ZosUNIXFileImpl> unixFileIterator = this.zosUnixFilesForCleanup.iterator();
			while (unixFileIterator.hasNext()) {
				ZosUNIXFileImpl zosUnixFile = unixFileIterator.next();
				zosUnixFile.cleanCreatedPath();
			}
		}
	}

	@Override
	public String toString() {
		return this.fieldName;
	}
}
