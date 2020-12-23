package dev.galasa.textscan.tests;

import java.io.InputStream;

import dev.galasa.ManagerException;
import dev.galasa.textscan.ITextScannable;

public class DummyScannableEmpty  implements ITextScannable{
	
	@Override
    public boolean isScannableInputStream() {
        return false;
    }

    @Override
    public boolean isScannableString() {
        return false;
    }

    @Override
    public InputStream getScannableInputStream() throws ManagerException {
        return null;
    }

    @Override
    public String getScannableName() {
        return "";
    }

    @Override
    public ITextScannable updateScannable() throws ManagerException {
        return this;
    }

    @Override
    public String getScannableString() throws ManagerException {
        return "";
    }

}
