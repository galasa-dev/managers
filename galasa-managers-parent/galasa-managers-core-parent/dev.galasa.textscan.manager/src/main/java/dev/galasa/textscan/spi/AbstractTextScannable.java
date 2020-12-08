/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan.spi;

import java.io.InputStream;

import dev.galasa.ManagerException;
import dev.galasa.textscan.ITextScannable;

/**
 * Abstract Text Scannable
 * 
 * @author Michael Baylis
 *
 */
public class AbstractTextScannable implements ITextScannable {

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
        return "unknown";
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
