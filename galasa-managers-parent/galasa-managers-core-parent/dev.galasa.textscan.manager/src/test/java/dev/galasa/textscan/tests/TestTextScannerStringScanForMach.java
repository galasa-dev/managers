package dev.galasa.textscan.tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.spi.TextScannerImpl;
import dev.galasa.textscan.IncorrectOccurancesException;

public class TestTextScannerStringScanForMach {
	
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

/** scanForMatch method String input Pattern 
 * @throws MissingTextException 
 * @throws IncorrectOccurancesException **/
	
	@Test
	public void testScanForMachForStringInputWithSearchPatternFound() throws IncorrectOccurancesException, MissingTextException {
		searchpattern = Pattern.compile("h");
		count = 1;
		assertThat("h",is(scanner.scanForMatch(string,searchpattern,count)));
	
	}
	@Test(expected = MissingTextException.class)
	public void testScanForMachForStringInputWithSearchPatternNotFound() throws IncorrectOccurancesException, MissingTextException {
		searchpattern = Pattern.compile("[5]");
		count = 1;
		scanner.scanForMatch(string,searchpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMachForStringInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, MissingTextException {
		searchpattern = Pattern.compile("[abc]");
		count = 5;
		scanner.scanForMatch(string,searchpattern,count) ;
	}
	@Test (expected = InvalidParameterException.class)
	public void testScanForStringInputWithIncorrectOccurancesNumber() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchpattern = Pattern.compile("[abc]");
		count = 0;
		scanner.scanForMatch(string,searchpattern,count) ;
	}
	
	/** ScanForMach method String input String 
	 * @throws MissingTextException 
	 * @throws IncorrectOccurancesException **/
	
	@Test
	public void testScanForMachForStringInputWithSearchStringFound() throws IncorrectOccurancesException, MissingTextException {
		searchString ="This";
		count =1;
		assertThat(searchString,is(scanner.scanForMatch(string,searchString,count)));
	}
	@Test(expected = MissingTextException.class)
	public void testScanForMachForStringInputWithSearchStringNotFound() throws IncorrectOccurancesException, MissingTextException {
		searchString ="Dragon";
		count =1;
		scanner.scanForMatch(string,searchString,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForMachForStringInputWithIncorrectOccurancesString() throws IncorrectOccurancesException, MissingTextException {
		searchString ="test";
		count =3;
		scanner.scanForMatch(string,searchString,count);
	}
	@Test (expected = InvalidParameterException.class)
	public void testScanForStringInputWithIncorrectOccurancesNumberString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException {
		searchString = "test";
		scanner = new TextScannerImpl();
		int count = 0;
		scanner.scanForMatch(string,searchString,count) ;
	}

}
