package dev.galasa.zosfile.zosmf.manager.internal;

import java.util.Map;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosVSAMDataset;
import dev.galasa.zosfile.IZosFile;
import dev.galasa.zosfile.ZosFileException;

/**
 * Implementation of {@link IZosFile} using zOS/MF
 *
 */
public class ZosFileImpl implements IZosFile {

	private IZosImage image;

	public ZosFileImpl(IZosImage image) {
		this.image = image;
	}

	@Override
	public IZosUNIXFile newUNIXFile(String fullFilePath) {
		// TODO Not yet implemented
		return new ZosUNIXFileImpl(image, fullFilePath);
	}

	@Override
	public void store(IZosUNIXFile unixFile, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void retrieve(IZosUNIXFile unixFile, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void delete(IZosUNIXFile unixFile, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void deleteDirectory(String directory, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void createDirectory(String directory, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public boolean exists(IZosUNIXFile unixFile, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		return false;
	}

	@Override
	public IZosDataset newDataset(String dsName) {
		// TODO Not yet implemented
		return new ZosDatasetImpl(image, dsName);
	}

	@Override
	public void store(IZosDataset dataset, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void retrieve(IZosDataset dataset, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void delete(IZosDataset dataset, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void deleteDatasetsByPrefix(String prefix, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public boolean exists(IZosDataset dataset, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		return false;
	}

	@Override
	public void apfAuthorise(IZosDataset dataset, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public boolean isAPFAuthorised(IZosDataset dataset, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		return false;
	}

	@Override
	public IZosVSAMDataset newVSAMDataset(String dsName) {
		// TODO Not yet implemented
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newKSDS(String dsName) {
		// TODO Not yet implemented
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newESDS(String dsName) {
		// TODO Not yet implemented
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public IZosVSAMDataset newRRDS(String dsName) {
		// TODO Not yet implemented
		return new ZosVSAMDataset(image, dsName);
	}

	@Override
	public void define(IZosVSAMDataset vsam, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void store(IZosVSAMDataset vsam, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void delete(IZosVSAMDataset vsam, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public boolean exists(IZosVSAMDataset vsam, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		return false;
	}

	@Override
	public IZosUNIXFile storeResourcesFile(String resourcePath, String unixPath, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosFileException {
		// TODO Not yet implemented
		return new ZosUNIXFileImpl(image, unixPath);
	}

	@Override
	public IZosDataset storeResourcesDataset(String resourcePath, String dsName, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosFileException {
		// TODO Not yet implemented
		return new ZosDatasetImpl(image, dsName);
	}

	@Override
	public IZosUNIXFile storeResourcesDirectory(String resourcePath, String unixPath, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosFileException {
		// TODO Not yet implemented
		return new ZosUNIXFileImpl(image, unixPath);
	}

	@Override
	public IZosDataset storeResourcesPDS(String resourcePath, String dsName, DSType pdsType, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass) throws ZosFileException {
		// TODO Not yet implemented
		return new ZosDatasetImpl(image, dsName);
	}

	@Override
	public void storeFileToTestOutput(IZosUNIXFile file, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	@Override
	public void storeDatasetToTestOutput(IZosDataset dataset, IZosImage image) throws ZosFileException {
		// TODO Not yet implemented
		
	}

	public void cleanup() {
		// TODO Not yet implemented
		
	}
	
}
