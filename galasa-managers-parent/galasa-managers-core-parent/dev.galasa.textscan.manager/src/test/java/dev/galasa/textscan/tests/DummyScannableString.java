package dev.galasa.textscan.tests;

import java.io.InputStream;

import dev.galasa.ManagerException;
import dev.galasa.textscan.ITextScannable;

public class DummyScannableString implements ITextScannable {

	@Override
    public boolean isScannableInputStream() {
        return false;
    }

    @Override
    public boolean isScannableString() {
        return true;
    }

    @Override
    public InputStream getScannableInputStream() throws ManagerException {
        return null;
    }

    @Override
    public String getScannableName() {
        return "StringDummy";
    }

    @Override
    public ITextScannable updateScannable() throws ManagerException {
        return this;
    }

    @Override
    public String getScannableString() throws ManagerException {
        return "This class is a dummy object with String for junit "
        		+ "tests for scanner implementations "
        		+ "methods that uses scannable";
    }
}
