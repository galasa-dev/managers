package dev.galasa.textscan.tests;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import dev.galasa.ManagerException;
import dev.galasa.textscan.ITextScannable;

public class DummyScannableInputStream implements ITextScannable{

	@Override
	public boolean isScannableInputStream() {
		return true;
	}

	@Override
	public boolean isScannableString() {
		return false;
	}

	@Override
	public String getScannableName() {
		return "InputStreamDummy";
	}

	@Override
	public ITextScannable updateScannable() throws ManagerException {
		return this;
	}

	@Override
	public InputStream getScannableInputStream() throws ManagerException {
		String string = "This class is a dummy object with InputStream for junit "
        		+ "tests for scanner implementations methods that uses scannable";
		InputStream stream = new ByteArrayInputStream(string.getBytes());
		return stream;
	}

	@Override
	public String getScannableString() throws ManagerException {
		return null;
	}

}
