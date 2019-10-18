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

/**
 * Implementation of {@link IZosFileHandler} using zOS/MF
 *
 */
public class ZosFileHandlerImpl implements IZosFileHandler {

	private List<ZosDatasetImpl> zosDatasets = new ArrayList<>();
	private String fieldName;

	public ZosFileHandlerImpl(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public IZosDataset newDataset(String dsName, IZosImage image) throws ZosDatasetException {
		ZosDatasetImpl zosDataset = null;
		try {
			zosDataset = new ZosDatasetImpl(image, dsName);
		} catch (ZosFileManagerException e) {
			throw new ZosDatasetException(e);
		}
		zosDatasets.add(zosDataset);
		return zosDataset;
	}

	@Override
	public IZosUNIXFile newUNIXFile(String fullFilePath, IZosImage image) {
		// TODO Not yet implemented
		return new ZosUNIXFileImpl(image, fullFilePath);
	}

	@Override
	public void deleteDatasetsByPrefix(String prefix, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		
	}

	@Override
	public void apfAuthorise(IZosDataset dataset, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		
	}

	@Override
	public boolean isAPFAuthorised(IZosDataset dataset, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		return false;
	}

	@Override
	public IZosVSAMDataset newVSAMDataset(String dsName, IZosImage image) {
		// TODO Not yet implemented
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newKSDS(String dsName, IZosImage image) {
		// TODO Not yet implemented
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newESDS(String dsName, IZosImage image) {
		// TODO Not yet implemented
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newRRDS(String dsName, IZosImage image) {
		// TODO Not yet implemented
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public void define(IZosVSAMDataset vsam, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		
	}

	@Override
	public void store(IZosVSAMDataset vsam, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		
	}

	@Override
	public void delete(IZosVSAMDataset vsam, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		
	}

	@Override
	public boolean exists(IZosVSAMDataset vsam, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		return false;
	}

	@Override
	public IZosUNIXFile storeResourcesFile(String resourcePath, String unixPath, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosDatasetException {
		// TODO Not yet implemented
		return new ZosUNIXFileImpl(image, unixPath);
	}

	@Override
	public IZosDataset storeResourcesDataset(String resourcePath, String dsName, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosDatasetException {
		// TODO Not yet implemented
		return new ZosDatasetImpl(image, dsName);
	}

	@Override
	public IZosUNIXFile storeResourcesDirectory(String resourcePath, String unixPath, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosDatasetException {
		// TODO Not yet implemented
		return new ZosUNIXFileImpl(image, unixPath);
	}

	@Override
	public IZosDataset storeResourcesPDS(String resourcePath, String dsName, DSType pdsType, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosDatasetException {
		// TODO Not yet implemented
		return new ZosDatasetImpl(image, dsName);
	}

	@Override
	public void storeFileToTestOutput(IZosUNIXFile file, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		
	}

	@Override
	public void storeDatasetToTestOutput(IZosDataset dataset, IZosImage image) throws ZosDatasetException {
		// TODO Not yet implemented
		
	}

	/**
	 * Clean up any existing files
	 * @throws ZosFileManagerException
	 */
	public void cleanup() throws ZosFileManagerException {
		
		Iterator<ZosDatasetImpl> iterator = this.zosDatasets.iterator();
		while (iterator.hasNext()) {
			ZosDatasetImpl zosDataset = iterator.next();
			if (zosDataset.created()) {
				zosDataset.delete();				
			}
		}
		this.zosDatasets.clear();
	}
	
	@Override
	public String toString() {
		return this.fieldName;
	}
}
