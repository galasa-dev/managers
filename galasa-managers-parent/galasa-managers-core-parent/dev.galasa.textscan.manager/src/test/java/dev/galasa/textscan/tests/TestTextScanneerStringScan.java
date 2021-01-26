package dev.galasa.textscan.tests;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanManagerException;
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
 * @throws TextScanManagerException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForStringInputWithFailPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = Pattern.compile("[abc]");
		count = 1;
		scanner.scan(string,searchpattern,failpattern,count);
	}
	@Test
	public void testScanForStringInputWithSearchPatternFound() throws TextScanManagerException {
		searchpattern = Pattern.compile("[^abc]");
		failpattern = null;
		count = 1;
		assertTrue(scanner.scan(string,searchpattern,failpattern,count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class)
	public void testScanForStringInputWithSearchPatternNotFound() throws TextScanManagerException {
		searchpattern = Pattern.compile("[5]");
		failpattern = Pattern.compile("[123]");
		scanner = new TextScannerImpl();
		count = 1;
		scanner.scan(string,searchpattern,failpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForStringInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("[abc]");
		failpattern = null;
		count = 5;
		scanner.scan(string,searchpattern,failpattern,count) ;
	}
	@Test (expected = IncorrectOccurancesException.class)
	public void testScanForStringInputWithIncorrectOccurancesNumber() throws TextScanManagerException {
		searchpattern = Pattern.compile("[abc]");
		failpattern = null;
		count = 0;
		scanner.scan(string,searchpattern,failpattern,count) ;
	}
	
	/** scan method String input String 
	 * @throws TextScanManagerException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForStringInputWithFailString() throws TextScanManagerException {
		searchString ="test";
		failString ="is";
		count =1;
		scanner.scan(string,searchString,failString,count) ;
	}
	@Test
	public void testScanForStringInputWithSearchStringFound() throws TextScanManagerException {
		searchString ="test";
		failString =null;
		count =1;
		assertTrue(scanner.scan(string,searchString,failString,count) instanceof ITextScanner);

	}
	@Test(expected = MissingTextException.class)
	public void testScanForStringInputWithSearchStringNotFound() throws TextScanManagerException {
		searchString ="Dragon";
		failString =null;
		count =1;
		scanner.scan(string,searchString,failString,count) ;
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForStringInputWithIncorrectOccurancesString() throws TextScanManagerException {
		string = "Five flying Dragons with five little dragons";
		searchString ="dragons";
		failString =null;
		count =3;
		scanner.scan(string,searchString,failString,count) ;
	}
	@Test (expected = IncorrectOccurancesException.class)
	public void testScanForStringInputWithIncorrectOccurancesNumberString() throws TextScanManagerException {
		searchString = "test";
		failString = null;
		count = 0;
		scanner.scan(string,searchString,failString,count) ;
	}
	

}
