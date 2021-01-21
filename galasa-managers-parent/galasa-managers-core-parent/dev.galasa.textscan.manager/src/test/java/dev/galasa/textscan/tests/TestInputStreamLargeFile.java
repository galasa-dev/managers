package dev.galasa.textscan.tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurancesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.TextScannerImpl;

public class TestInputStreamLargeFile {

	File file = new File("src/test/resources/bigfile.txt");
	InputStream stream;
	ITextScanner scanner;
	Pattern searchpattern;
	Pattern failpattern;
	String searchString;
	String failString;
	int count;
	@Before
	public void beforeClass() throws FileNotFoundException {
		scanner = new TextScannerImpl();
		stream = new FileInputStream(file);
	}

	/** scan method input stream input Pattern 
	 * @throws IOException 
	 * @throws TextScanManagerException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("a huge dragon landed on the house and started breathing fire");
		failpattern = Pattern.compile("Line 13\nLine 14");
		count =1;
		scanner.scan(stream,searchpattern,failpattern,count);
	}
	@Test
	public void testScanForInputStreamInputWithSearchPatternFound() throws TextScanManagerException {
		searchpattern = Pattern.compile("100");
		failpattern =null;;
		count =3;
		assertTrue(scanner.scan(stream,searchpattern,failpattern,count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForInputStreamInputWithSearchPatternNotFound() throws TextScanManagerException {
		searchpattern = Pattern.compile("the dragon fired 5 times and then decided to take a nap");
		failpattern = null;
		count =1;
		scanner.scan(stream,searchpattern,failpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("100");
		failpattern = null;
		count =10;
		scanner.scan(stream,searchpattern,failpattern,count);
	}

	/** scan method input stream input String
	 * @throws IOException 
	 * @throws TextScanManagerException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailString() throws TextScanManagerException {
		searchString = "after a nap dragon became very very hungry";
		failString = "100";
		count =1;
		scanner.scan(stream,searchString,failString,count);
	}
	@Test
	public void testScanForInputStreamInputWithSearchStringFound() throws TextScanManagerException {
		searchString = "Line 16 Eye";
		failString =null;;
		count =3;
		assertTrue(scanner.scan(stream,searchString,failString,count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForInputStreamInputWithSearchStringNotFound() throws TextScanManagerException {
		searchString = "He decided that he wants some nice pork, so he flew towards pig village";
		failString = null;
		count =1;
		scanner.scan(stream,searchString,failString,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesString() throws TextScanManagerException {
		searchString = "Line 38 Bee";
		failString = null;
		count =10;
		scanner.scan(stream,searchString,failString,count);
	}

	/** scanForMatch method input stream input Pattern 
	 * @throws IOException 
	 * @throws TextScanManagerException **/
	@Test
	public void testScanForMatchForInputStreamInputWithSearchPatternFound() throws TextScanManagerException {
		searchpattern = Pattern.compile("100");
		count =3;
		assertThat("100",is(scanner.scanForMatch(stream,searchpattern,count)));
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForForMatchInputStreamInputWithSearchPatternNotFound() throws TextScanManagerException {
		searchpattern = Pattern.compile("He carefully landed into the willage and silently lifted the roof");
		count =1;
		scanner.scanForMatch(stream,searchpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForForMatchInputStreamInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("77");
		count =10;
		scanner.scanForMatch(stream,searchpattern,count);
	}


	/** scanForMatch method input stream input String
	 * @throws IOException 
	 * @throws TextScanManagerException **/
	@Test
	public void testScanForMatchForInputStreamInputWithSearchStringFound() throws TextScanManagerException {
		searchString = "Line 76 M";
		count =3;
		assertThat(searchString,is(scanner.scanForMatch(stream,searchString,count)));
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForForMatchInputStreamInputWithSearchStringNotFound() throws TextScanManagerException {
		searchString = "In the house he found five sleeping pigs in blankets and he ate them all.";
		count =1;
		scanner.scanForMatch(stream,searchString,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForForMatchInputStreamInputWithIncorrectOccurancesString() throws TextScanManagerException {
		searchString = "10";
		count =10;
		scanner.scanForMatch(stream,searchString,count);
	}
	
	/** Check for double accounting**/

	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForForMatchInputStreamInputCheckDubleAccountingString() throws TextScanManagerException {
		searchString = "Q";
		count =2;
		scanner.scanForMatch(stream,searchString,count);
	}
	
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForForMatchInputStreamInputCheckDoubleAccountingPattern() throws TextScanManagerException {
		searchpattern = Pattern.compile("Q");
		count =2;
		scanner.scanForMatch(stream,searchpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForForMatchInputStreamInputCheckDubleAccountingString2() throws TextScanManagerException {
		searchString = "Line 7\nLine 8\nLine 7";
		count =3;
		scanner.scanForMatch(stream,searchString,count);
	}
	
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForForMatchInputStreamInputCheckDoubleAccountingPattern2() throws TextScanManagerException {
		searchpattern = Pattern.compile("Line 7\nLine 8\nLine 7");
		count =3;
		scanner.scanForMatch(stream,searchpattern,count);
	}
}
