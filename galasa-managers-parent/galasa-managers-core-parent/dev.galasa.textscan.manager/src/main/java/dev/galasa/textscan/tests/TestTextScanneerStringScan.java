package dev.galasa.textscan.tests;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.spi.TextScannerImpl;
import dev.galasa.textscan.IncorrectOccurancesException;

public class TestTextScanneerStringScan {
	
	String string = "This is a textscanner test";
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
	
/** scan method String input Pattern 
 * @throws MissingTextException 
 * @throws FailTextFoundException 
 * @throws IncorrectOccurancesException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForStringInputWithFailPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = Pattern.compile("[abc]");
		count = 1;
		scanner.scan(string,searchpattern,failpattern,count);
	}
	@Test
	public void testScanForStringInputWithSearchPatternFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = null;
		count = 1;
		assertTrue(scanner.scan(string,searchpattern,failpattern,count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class)
	public void testScanForStringInputWithSearchPatternNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchpattern = Pattern.compile("[5]");
		failpattern = Pattern.compile("[123]");
		scanner = new TextScannerImpl();
		count = 1;
		scanner.scan(string,searchpattern,failpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForStringInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchpattern = Pattern.compile("[abc]");
		failpattern = null;
		count = 5;
		scanner.scan(string,searchpattern,failpattern,count) ;
	}
	@Test (expected = InvalidParameterException.class)
	public void testScanForStringInputWithIncorrectOccurancesNumber() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchpattern = Pattern.compile("[abc]");
		failpattern = null;
		count = 0;
		scanner.scan(string,searchpattern,failpattern,count) ;
	}
	
	/** scan method String input String 
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForStringInputWithFailString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchString ="test";
		failString ="is";
		count =1;
		scanner.scan(string,searchString,failString,count) ;
	}
	@Test
	public void testScanForStringInputWithSearchStringFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchString ="test";
		failString =null;
		count =1;
		assertTrue(scanner.scan(string,searchString,failString,count) instanceof ITextScanner);

	}
	@Test(expected = MissingTextException.class)
	public void testScanForStringInputWithSearchStringNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchString ="Dragon";
		failString =null;
		count =1;
		scanner.scan(string,searchString,failString,count) ;
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForStringInputWithIncorrectOccurancesString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		string = "Five flying Dragons with five little dragons";
		searchString ="dragons";
		failString =null;
		count =3;
		scanner.scan(string,searchString,failString,count) ;
	}
	@Test (expected = InvalidParameterException.class)
	public void testScanForStringInputWithIncorrectOccurancesNumberString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchString = "test";
		failString = null;
		count = 0;
		scanner.scan(string,searchString,failString,count) ;
	}
	

}
