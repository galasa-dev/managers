package dev.galasa.textscan.tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurancesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.spi.TextScannerImpl;

public class TestInputStreamLargeFile {

	File file = new File("/Users/dianaslepikaite/Documents/Galasa/managers/galasa-managers-parent/galasa-managers-core-parent/dev.galasa.textscan.manager/src/main/java/loremIpsum.txt");
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
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchpattern = Pattern.compile("a huge dragon landed on the house and started breathing fire");
		failpattern = Pattern.compile("Suspendisse id laoreet felis. Donec aliquet molestie consectetur.");
		count =1;
		scanner.scan(stream,searchpattern,failpattern,count);
	}
	@Test
	public void testScanForInputStreamInputWithSearchPatternFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchpattern = Pattern.compile("Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.");
		failpattern =null;;
		count =3;
		assertTrue(scanner.scan(stream,searchpattern,failpattern,count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForInputStreamInputWithSearchPatternNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchpattern = Pattern.compile("the dragon fired 5 times and then decided to take a nap");
		failpattern = null;
		count =1;
		scanner.scan(stream,searchpattern,failpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchpattern = Pattern.compile("Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.");
		failpattern = null;
		count =10;
		scanner.scan(stream,searchpattern,failpattern,count);
	}

	/** scan method input stream input String
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchString = "after a nap dragon became very very hungry";
		failString = "Suspendisse id laoreet felis. Donec aliquet molestie consectetur.";
		count =1;
		scanner.scan(stream,searchString,failString,count);
	}
	@Test
	public void testScanForInputStreamInputWithSearchStringFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchString = "Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.";
		failString =null;;
		count =3;
		assertTrue(scanner.scan(stream,searchString,failString,count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForInputStreamInputWithSearchStringNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchString = "He decided that he wants some nice pork, so he flew towards pigs village";
		failString = null;
		count =1;
		scanner.scan(stream,searchString,failString,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchString = "Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.";
		failString = null;
		count =10;
		scanner.scan(stream,searchString,failString,count);
	}

	/** scanForMatch method input stream input Pattern 
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test
	public void testScanForMatchForInputStreamInputWithSearchPatternFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchpattern = Pattern.compile("Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.");
		count =3;
		assertThat("Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.",is(scanner.scanForMatch(stream,searchpattern,count)));
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForForMatchInputStreamInputWithSearchPatternNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchpattern = Pattern.compile("He carefully landed into the willage and silently lifted the roof");
		count =1;
		scanner.scanForMatch(stream,searchpattern,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForForMatchInputStreamInputWithIncorrectOccurancesPattern() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchpattern = Pattern.compile("Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.");
		count =10;
		scanner.scanForMatch(stream,searchpattern,count);
	}


	/** scanForMatch method input stream input String
	 * @throws MissingTextException 
	 * @throws FailTextFoundException 
	 * @throws IncorrectOccurancesException 
	 * @throws IOException **/
	@Test
	public void testScanForMatchForInputStreamInputWithSearchStringFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchString = "Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.";
		count =3;
		assertThat(searchString,is(scanner.scanForMatch(stream,searchString,count)));
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForForMatchInputStreamInputWithSearchStringNotFound() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchString = "In the house he found five sleeping pigs in blankets and he ate them all.";
		count =1;
		scanner.scanForMatch(stream,searchString,count);
	}
	@Test(expected = IncorrectOccurancesException.class)
	public void testScanForForMatchInputStreamInputWithIncorrectOccurancesString() throws IncorrectOccurancesException, FailTextFoundException, MissingTextException, IOException {
		searchString = "Aliquam erat volutpat. Donec posuere nisl in consectetur gravida.";
		count =10;
		scanner.scanForMatch(stream,searchString,count);
	}

}
