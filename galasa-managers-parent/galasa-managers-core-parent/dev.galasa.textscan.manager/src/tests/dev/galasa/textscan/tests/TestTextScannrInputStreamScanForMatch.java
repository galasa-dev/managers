package dev.galasa.textscan.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.spi.TextScannerImpl;
import dev.galasa.textscan.IncorrectOccurancesException;


public class TestTextScannrInputStreamScanForMatch {

	String string = "This junit test tests scanForMatch methods in TextScanner implementation that uses input streams";
	InputStream stream = new ByteArrayInputStream(string.getBytes());
	ITextScanner scanner;
	Pattern searchpattern;
	String searchString;
	int count;
	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
	}

	
	/** ScanForMach method input stream input Pattern 
	 * @throws MissingTextException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	
	@Test
	public void testScanForMachForInputStreamInputWithSearchPatternFound() throws IncorrectOccurancesException, MissingTextException, IOException {
		searchpattern = Pattern.compile("a");
		count=1;
		assertThat("a",is(scanner.scanForMatch(stream,searchpattern,count)));
	}
	@Test(expected = MissingTextException.class)
	public void testScanForMachForInputStreamInputWithSearchPatternNotFound() throws IncorrectOccurancesException, MissingTextException, IOException {
		searchpattern = Pattern.compile("\\d");
		count=1;
		scanner.scanForMatch(stream,searchpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMachForInputStreamInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, MissingTextException, IOException {
		searchpattern = Pattern.compile("[c]");
		count=10;
		scanner.scanForMatch(stream,searchpattern,count);
	}
	@Test(expected = InvalidParameterException.class)
	public void testScanForMachInputStreamWithInvalidOccurancesPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchpattern = Pattern.compile("[c]");
		count =0;
		scanner.scanForMatch(stream,searchpattern,count);
	}
	
	
	/** ScanForMach method Input Stream input String 
	 * @throws MissingTextException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	
	@Test
	public void testScanForMachForInputStreamInputWithSearchStringFound() throws IncorrectOccurancesException, MissingTextException, IOException {
		searchString = "test";
		count=1;
		assertThat("test",is(scanner.scanForMatch(stream,searchString,count)));
	}
	@Test(expected = MissingTextException.class)
	public void testScanForMachForInputStreamInputWithSearchStringNotFound() throws IncorrectOccurancesException, MissingTextException, IOException {
		searchString = "dragon";
		count=1;
		assertThat("test",is(scanner.scanForMatch(stream,searchString,count)));
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMachForInputStreamInputWithIncorrectOccurancesString() throws IncorrectOccurancesException, MissingTextException, IOException {
		searchString = "test";
		count=5;
		assertThat("test",is(scanner.scanForMatch(stream,searchString,count)));
	}
	@Test(expected = InvalidParameterException.class)
	public void testScanForMachInputStreamWithInvalidOccurancesString() throws IncorrectOccurancesException, MissingTextException, IOException {
		searchString = "test";
		count=0;
		assertThat("test",is(scanner.scanForMatch(stream,searchString,count)));
	}
}
