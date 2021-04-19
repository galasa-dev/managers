/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.textscan.internal;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.textscan.TextScanManagerException;


public class TestTextScannerImplInputStreamScanForMatch {

	private static final String TEXT_STRING = "This junit test tests scanForMatch methods in TextScanner implementation that uses input streams";
	InputStream textInputStream = new ByteArrayInputStream(TEXT_STRING.getBytes());
	ITextScanner scanner;
	
	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
	}

	
	// scanForMatch() with InputStream and search Pattern	
	@Test
	public void testScanForMachForInputStreamInputWithSearchPatternFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("a");
		int count = 1;
		assertEquals("a", scanner.scanForMatch(textInputStream, searchPattern, count));
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForMachForInputStreamInputWithSearchPatternNotFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("\\d");
		int count = 1;
		scanner.scanForMatch(textInputStream, searchPattern, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForMachForInputStreamInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		int count = 10;
		scanner.scanForMatch(textInputStream, searchPattern, count);
	}
	
	@Test(expected = TextScanException.class)
	public void testScanForMachInputStreamWithInvalidOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		int count = 0;
		scanner.scanForMatch(textInputStream, searchPattern, count);
	}
	
	// scanForMatch() InputStream and search String 	
	@Test
	public void testScanForMachForInputStreamInputWithSearchStringFound() throws TextScanManagerException {
		String searchString = "test";
		int count = 1;
		assertEquals("test", scanner.scanForMatch(textInputStream, searchString, count));
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForMachForInputStreamInputWithSearchStringNotFound() throws TextScanManagerException {
		String searchString = "dragon";
		int count = 1;
		scanner.scanForMatch(textInputStream, searchString, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForMachForInputStreamInputWithIncorrectOccurancesString() throws TextScanManagerException {
		String searchString = "test";
		int count = 5;
		scanner.scanForMatch(textInputStream, searchString, count);
	}
	
	@Test(expected = TextScanException.class)
	public void testScanForMachInputStreamWithInvalidOccurancesString() throws TextScanManagerException {
		String searchString = "test";
		int count = 0;
		scanner.scanForMatch(textInputStream, searchString, count);
	}
}
