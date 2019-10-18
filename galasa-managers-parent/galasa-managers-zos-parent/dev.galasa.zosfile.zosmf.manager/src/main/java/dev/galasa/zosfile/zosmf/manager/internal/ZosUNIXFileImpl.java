package dev.galasa.zosfile.zosmf.manager.internal;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosUNIXFileException;

public class ZosUNIXFileImpl implements IZosUNIXFile {

	public ZosUNIXFileImpl(IZosImage image, String fullFilePath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDirectory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getFileName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void store() throws ZosUNIXFileException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void retrieve() throws ZosUNIXFileException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void delete() throws ZosUNIXFileException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void deleteDirectory(String directory) throws ZosUNIXFileException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void createDirectory(String directory) throws ZosUNIXFileException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public boolean exists() throws ZosUNIXFileException {
		throw new UnsupportedOperationException();
	}

}
