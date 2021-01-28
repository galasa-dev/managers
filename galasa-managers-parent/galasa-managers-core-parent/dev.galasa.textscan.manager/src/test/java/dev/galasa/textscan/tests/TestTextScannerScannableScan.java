package dev.galasa.textscan.tests;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanManagerException;
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
 * @throws TextScanManagerException 
**/
	@Test(expected = FailTextFoundException.class)
	public void testScanForScannableStringInputWithFailPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException{
		searchpattern = Pattern.compile("[^abc]");
		failpattern = Pattern.compile("[abc]");
		count = 1;
		scanner.scan(scannableString,searchpattern,failpattern,count);
	}
	@Test
	public void testScanForScannableStringInputWithSearchPatternFound() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException{
		searchpattern = Pattern.compile("[^abc]");
		failpattern = null;
		count = 1;
		assertTrue(scanner.scan(scannableString,searchpattern,failpattern,count) instanceof ITextScanner);
	
	}
	@Test(expected = MissingTextException.class)
	public void testScanForScannableStringInputWithSearchPatternNotFound() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException{
		searchpattern = Pattern.compile("5");
		failpattern = null;
		count = 1;
		scanner.scan(scannableString,searchpattern,failpattern,count);
	
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForScannableStringInputWithIncorrectOccurancesPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchpattern = Pattern.compile("[c]");
		failpattern = null;
		count = 10;
		scanner.scan(scannableString,searchpattern,failpattern,count);
	
	}
	
	/** scan method Scannable InputStream input Pattern 
	 * @throws TextScanManagerException 
	  **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForScannableInputStreamInputWithFailPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = Pattern.compile("[abc]");
		count = 1;
		scanner.scan(scannableStream,searchpattern,failpattern,count);
	
	}
	
	@Test
	public void testScanForScannableInputStreamInputWithSearchPatternFound() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchpattern = Pattern.compile("c");
		failpattern = null;
		count = 1;
		assertTrue(scanner.scan(scannableStream,searchpattern,failpattern,count)instanceof ITextScanner);
	
	}

	@Test(expected = MissingTextException.class)
	public void testScanForScannableInputStreamInputWithSearchPatternNotFound() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchpattern = Pattern.compile("5");
		failpattern = null;
		count = 1;
		scanner.scan(scannableStream,searchpattern,failpattern,count);
	
	}
	
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForScannableInputStreamInputWithIncorrectOccurancesPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchpattern = Pattern.compile("c");
		failpattern = null;
		count = 10;
		scanner.scan(scannableStream,searchpattern,failpattern,count);
	
	}
	
	
	/** scan method Scannable String input String 
	 * @throws TextScanManagerException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanScannableStringInputWithFailString() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchString = "dummy";
		failString = "junit";
		count = 1;
		scanner.scan(scannableString,searchString,failString,count);
	}
	@Test 
	public void testScanScannableStringInputWithSearchStringFound() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchString = "class";
		failString = null;
		count = 1;
		assertTrue(scanner.scan(scannableString,searchString,failString,count) instanceof ITextScanner);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForScannableStringInputWithIncorrectOccurancesString() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchString = "class";
		failString = null;
		count = 5;
		scanner.scan(scannableString,searchString,failString,count);
	}
	@Test(expected = MissingTextException.class)
	public void testScanForScannableStringInputWithSearchStringNotFound() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchString = "a dog";
		failString = null;
		count = 1;
		scanner.scan(scannableString,searchString,failString,count);
	
	}
	/** scan method Scannable InputStream input String 
	 * @throws TextScanManagerException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForScannableInputStreamInputWithFailString() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchString = "dummy";
		failString = "junit";
		count = 1;
		scanner.scan(scannableStream,searchString,failString,count);
	}
	@Test
	public void testScanForScannableInputStreamInputWithSearchStringFound() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException{
		searchString = "dummy";
		failString = null;
		count = 1;
		assertTrue(scanner.scan(scannableStream,searchString,failString,count)instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class)
	public void testScanScannableInputStreamInputWithSearchStringNotFound() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchString = "lemon";
		failString = "apple";
		count = 1;
		scanner.scan(scannableStream,searchString,failString,count);
	
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForScannableInputStreamInputWithIncorrectOccurancesString() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException{
		searchString = "dummy";
		failString = "apple";
		count = 5;
		scanner.scan(scannableStream,searchString,failString,count);
	
	}
	
	/** Checking for an error when occurancies number is set to less than 1
	 * @throws TextScanManagerException **/
	@Test (expected =  IncorrectOccurancesException.class)
	public void testScanScannableInvalidOccurancesString() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException{
		searchString = "dummy";
		failString = null;
		count = 0;
		scanner.scan(scannableString,searchString,failString,count);
	}
	@Test (expected = IncorrectOccurancesException.class)
	public void testScanScannableInvalidOccurancesPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = null;
		count = 0;
		scanner.scan(scannableString,searchpattern,failpattern,count);
	}
	
	/** Cheking code if scannable is neither String nor ImputStream
	 * @throws TextScanManagerException **/
	@Test (expected = TextScanManagerException.class)
	public void testScanScannableEmptyString() throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {
		searchString = "dummy";
		failString = "test";
		count = 1;
		scanner.scan(scannableEmpty,searchString,failString,count);
	}
	@Test (expected = TextScanManagerException.class)
	public void testScanScannableEmptyPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern =  Pattern.compile("dragon");
		count = 1;
		scanner.scan(scannableEmpty,searchpattern,failpattern,count);
	}
}
