package dev.galasa.textscan.tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.ManagerException;
import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.spi.TextScannerImpl;
import dev.galasa.textscan.IncorrectOccurancesException;


public class TestTextScannerScannableScanForMatch {
	
	ITextScanner scanner;
	ITextScannable scannableString;
	ITextScannable scannableStream;
	ITextScannable scannableEmpty;
	Pattern searchpattern;
	String searchString;
	int count;
	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
		scannableString = new DummyScannableString();
		scannableStream = new DummyScannableInputStream();
		scannableEmpty = new DummyScannableEmpty();
	}

	/** ScanForMach method Scannable String input Pattern 
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/

	@Test
	public void testScanForMachForScannableStringInputWithSearchPatternFound() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[c]");
		scanner = new TextScannerImpl();
		count = 1;
		assertThat("c",is(scanner.scanForMatch(scannableString,searchpattern,count)));
	}
	@Test(expected = MissingTextException.class)
	public void testScanForMachForScannableStringInputWithSearchPatternNotFound() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("\\d");
		count = 1;
		scanner.scanForMatch(scannableString,searchpattern,count);
	
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMachForScannableStringInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[c]");
		count = 10;
		scanner.scanForMatch(scannableString,searchpattern,count);
	}
	@Test (expected = InvalidParameterException.class)
	public void testScanScannableInvalidOccurancesPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[a]");
		count = 0;
		scanner.scanForMatch(scannableString,searchpattern,count);
	}

	/** ScanForMach method Scannable InputStream input Pattern 
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/

	@Test 
	public void testScanForMachForScannableInputStreamInputWithSearchPatternFound() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[a]");
		count = 1;
		assertThat("a",is(scanner.scanForMatch(scannableStream,searchpattern,count)));
	
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForMachForScannableInputStreamInputWithSearchPatternNotFound() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("\\d");
		count = 1;
		scanner.scanForMatch(scannableStream,searchpattern,count);
	
		}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMachForScannableInputStreamInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[i]");
		count = 10;
		scanner.scanForMatch(scannableStream,searchpattern,count);
	
	}

	/** ScanForMach method Scannable String input String 
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/

	@Test
	public void testScanForMachForScannableStringInputWithSearchStringFound() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		count = 1;
		assertThat(searchString,is(scanner.scanForMatch(scannableString,searchString,count)));
	
	}
	@Test(expected = MissingTextException.class)
	public void testScanForMachForScannableStringInputWithSearchStringNotFound() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchString = "lemon";
		count = 1;
		scanner.scanForMatch(scannableString,searchString,count);
	
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMachForScannableInputStringWithIncorrectOccurancesString() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		count = 10;
		scanner.scanForMatch(scannableString,searchString,count);
	}
	@Test (expected = InvalidParameterException.class)
	public void testScanScannableInvalidOccurancesString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		count = 0;
		scanner.scanForMatch(scannableString,searchString,count);
	}

	/** ScanForMach method Scannable InputStream input String 
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/

	@Test
	public void testScanForMachForScannableInputStreamInputWithSearchStringFound() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		count = 1;
		assertThat("dummy",is(scanner.scanForMatch(scannableStream.getScannableInputStream(),searchString,count)));
	}
	@Test(expected = MissingTextException.class)
	public void testScanForMachForScannableInputStreamInputWithSearchStringNotFound() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchString = "lemon";
		count = 1;
		scanner.scanForMatch(scannableStream,searchString,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMachForScannableInputStreamInputWithIncorrectOccurancesString() throws IncorrectOccurancesException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		count = 5;
		scanner.scanForMatch(scannableStream,searchString,count);
	}
	/** Cheking code if scannable is neither String nor ImputStream
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test
	public void testScanForMatchScannableEmptyString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		count = 1;
		assertThat(null,is(scanner.scanForMatch(scannableEmpty,searchString,count)));
	}
	@Test
	public void testScanForMatchScannableEmptyPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[^abc]");
		count = 1;
		assertThat(null,is(scanner.scanForMatch(scannableEmpty,searchpattern,count)));
	}

}
