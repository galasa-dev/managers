/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import java.io.InputStream;

import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.TextScanException;

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
    public InputStream getScannableInputStream() throws TextScanException {
        return null;
    }

    @Override
    public String getScannableName() {
        return "";
    }

    @Override
    public ITextScannable updateScannable() throws TextScanException {
        return this;
    }

    @Override
    public String getScannableString() throws TextScanException {
        return "";
    }

}
