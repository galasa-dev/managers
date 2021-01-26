package dev.galasa.textscan.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.TextScannerImpl;
import dev.galasa.textscan.IncorrectOccurancesException;


public class TestTextScannerInputStreamScan {

	String string = "This junit test tests scan methods in TextScanner implementation that uses input streams";
	InputStream stream = new ByteArrayInputStream(string.getBytes());
	ITextScanner scanner;
	Pattern searchpattern;
	Pattern failpattern;
	String searchString;
	String failString;
	int count;
	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
	}

	/** scan method input stream input Pattern 
	 * @throws IOException 
	 * @throws TextScanManagerException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("[c]");
		failpattern = Pattern.compile("[a-zA-Z]");
		count =1;
		scanner.scan(stream,searchpattern,failpattern,count);
	}
	@Test
	public void testScanForInputStreamInputWithSearchPatternFound() throws TextScanManagerException {
		searchpattern = Pattern.compile("[a]");
		failpattern = null;
		count =1;
		assertTrue(scanner.scan(stream,searchpattern,failpattern,count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForInputStreamInputWithSearchPatternNotFound() throws TextScanManagerException {
		searchpattern = Pattern.compile("\\d");
		failpattern = null;
		count =1;
		scanner.scan(stream,searchpattern,failpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("[c]");
		failpattern = null;
		count =10;
		scanner.scan(stream,searchpattern,failpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanInputStreamWithInvalidOccurancesPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("[c]");
		failpattern = null;
		count =0;
		scanner.scan(stream,searchpattern,failpattern,count);
	}

	/** scan method Input Stream input String 
	 * @throws IOException 
	 * @throws TextScanManagerException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailString() throws TextScanManagerException {
		searchString = "test";
		failString = "scan";
		count =1;
		scanner.scan(stream, searchString, failString, count);
	}
	@Test
	public void testScanForInputStreamInputWithSearchStringFound() throws TextScanManagerException {
		searchString = "test";
		failString = null;
		count =1;
		assertTrue(scanner.scan(stream, searchString, failString, count)instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class)
	public void testScanForInputStreamInputWithSearchStringNotFound() throws TextScanManagerException {
		searchString = "dragon";
		failString = null;
		count =1;
		scanner.scan(stream, searchString, failString, count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesrString() throws TextScanManagerException {
		searchString = "test";
		failString = null;
		count =10;
		scanner.scan(stream, searchString, failString, count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanInputStreamwithInvalidOccurancesString() throws TextScanManagerException {
		searchString = "test";
		failString = null;
		count =0;
		scanner.scan(stream, searchString, failString, count);
	}


}
