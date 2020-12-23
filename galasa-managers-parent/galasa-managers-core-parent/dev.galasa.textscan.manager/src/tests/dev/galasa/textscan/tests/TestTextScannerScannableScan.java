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

public class TestTextScannerScannableScan {

	
	ITextScanner scanner;
	ITextScannable scannableString;
	ITextScannable scannableStream;
	ITextScannable scannableEmpty;
	Pattern searchpattern;
	Pattern failpattern;
	String searchString;
	String failString;
	int count;
	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
		scannableString = new DummyScannableString();
		scannableStream = new DummyScannableInputStream();
		scannableEmpty = new DummyScannableEmpty();
	}
	
/** scan method Scannable String input Pattern 
 * @throws ManagerException 
 * @throws MissingTextException 
 * @throws FailTextFoundException 
 * @throws IncorrectOccurancesException 
 * @throws IOException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForScannableStringInputWithFailPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = Pattern.compile("[abc]");
		count = 1;
		scanner.scan(scannableString,searchpattern,failpattern,count);
	}
	@Test
	public void testScanForScannableStringInputWithSearchPatternFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = null;
		count = 1;
		assertTrue(scanner.scan(scannableString,searchpattern,failpattern,count) instanceof ITextScanner);
	
	}
	@Test(expected = MissingTextException.class)
	public void testScanForScannableStringInputWithSearchPatternNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("5");
		failpattern = null;
		count = 1;
		scanner.scan(scannableString,searchpattern,failpattern,count);
	
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForScannableStringInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[c]");
		failpattern = null;
		count = 10;
		scanner.scan(scannableString,searchpattern,failpattern,count);
	
	}
	
	/** scan method Scannable InputStream input Pattern 
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForScannableInputStreamInputWithFailPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = Pattern.compile("[abc]");
		count = 1;
		scanner.scan(scannableStream,searchpattern,failpattern,count);
	
	}
	
	@Test
	public void testScanForScannableInputStreamInputWithSearchPatternFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("c");
		failpattern = null;
		count = 1;
		assertTrue(scanner.scan(scannableStream,searchpattern,failpattern,count)instanceof ITextScanner);
	
	}

	@Test(expected = MissingTextException.class)
	public void testScanForScannableInputStreamInputWithSearchPatternNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("5");
		failpattern = null;
		count = 1;
		scanner.scan(scannableStream,searchpattern,failpattern,count);
	
	}
	
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForScannableInputStreamInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("c");
		failpattern = null;
		count = 10;
		scanner.scan(scannableStream,searchpattern,failpattern,count);
	
	}
	
	
	/** scan method Scannable String input String 
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanScannableStringInputWithFailString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		failString = "junit";
		count = 1;
		scanner.scan(scannableString,searchString,failString,count);
	}
	@Test 
	public void testScanScannableStringInputWithSearchStringFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "class";
		failString = null;
		count = 1;
		assertTrue(scanner.scan(scannableString,searchString,failString,count) instanceof ITextScanner);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForScannableStringInputWithIncorrectOccurancesString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "class";
		failString = null;
		count = 5;
		scanner.scan(scannableString,searchString,failString,count);
	}
	@Test(expected = MissingTextException.class)
	public void testScanForMatchScannableStringInputWithSearchStringNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "a dog";
		failString = null;
		count = 1;
		scanner.scan(scannableString,searchString,failString,count);
	
	}
	/** scan method Scannable InputStream input String 
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForMatchScannableInputStreamInputWithFailString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		failString = "junit";
		count = 1;
		scanner.scan(scannableStream,searchString,failString,count);
	}
	@Test
	public void testScanForMatchScannableInputStreamInputWithSearchStringFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		failString = null;
		count = 1;
		assertTrue(scanner.scan(scannableStream,searchString,failString,count)instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class)
	public void testScanScannableInputStreamInputWithSearchStringNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "lemon";
		failString = "apple";
		count = 1;
		scanner.scan(scannableStream,searchString,failString,count);
	
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMatchScannableInputStreamInputWithIncorrectOccurancesString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		failString = "apple";
		count = 5;
		scanner.scan(scannableStream,searchString,failString,count);
	
	}
	
	/** Checking for an error when occurancies number is set to less than 1
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException
	 * @throws IOException **/
	@Test (expected = InvalidParameterException.class)
	public void testScanScannableInvalidOccurancesString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		failString = null;
		count = 0;
		scanner.scan(scannableString,searchString,failString,count);
	}
	@Test (expected = InvalidParameterException.class)
	public void testScanScannableInvalidOccurancesPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = null;
		count = 0;
		scanner.scan(scannableString,searchpattern,failpattern,count);
	}
	
	/** Cheking code if scannable is neither String nor ImputStream
	 * @throws ManagerException 
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test
	public void testScanScannableEmptyString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchString = "dummy";
		failString = "test";
		count = 1;
		assertThat(null,is(scanner.scan(scannableEmpty,searchString,failString,count)));
	}
	@Test
	public void testScanScannableEmptyPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern =  Pattern.compile("dragon");
		count = 1;
		assertThat(null,is(scanner.scan(scannableEmpty,searchpattern,failpattern,count)));
	}
}
