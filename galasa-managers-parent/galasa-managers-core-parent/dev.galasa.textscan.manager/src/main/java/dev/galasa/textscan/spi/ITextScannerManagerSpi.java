/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.textscan.spi;

import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.TextScanManagerException;

public interface ITextScannerManagerSpi {
	
	public ITextScanner getTextScanner() throws TextScanManagerException;

	public ILogScanner getLogScanner() throws TextScanManagerException;
}
