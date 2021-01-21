package dev.galasa.textscan.tests;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.IncorrectOccurancesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.TextScannerImpl;

public class MultilineInterleavedScanTest {
    private static final String TestString = "One\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One Two\n" +
            "One Three Two\n" +
            "One Three\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One Four\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One\n" +
            "One\n";
    private static final Pattern okRegex = Pattern.compile("Two\nOne Three", Pattern.MULTILINE);
    private static final Pattern failRegex = Pattern.compile("Four", Pattern.MULTILINE);
    @Test
    public void testRawRegexWorks() {
        System.out.println(TestString);
        Matcher matcher = okRegex.matcher(TestString);
        int count = 0;
        while(matcher.find()) {
            System.out.println(TestString.substring(matcher.start(),matcher.end()));
            count++;
        }
        assertThat(count).isEqualTo(2);
    }
    @Test
    public void testStringFindsOnlyTwoOccurances() throws FailTextFoundException, MissingTextException {
        TextScannerImpl ts = new TextScannerImpl();
        ts.scan(TestString, okRegex, null, 2);
        try {
            ts.scan(TestString, okRegex, null, 3);
            fail("Should not have found 3 occurances");
        } catch(IncorrectOccurancesException e) {}
    }
    @Test
    public void testStringFailAfterOccurances() throws IncorrectOccurancesException, MissingTextException {
        TextScannerImpl ts = new TextScannerImpl();
        try {
            ts.scan(TestString, okRegex, failRegex, 2);
            fail("Should have issued FailTextFoundException");
        } catch(FailTextFoundException e) {}
    }
    
    @Test
    public void testInputStreamFindsOnlyTwoOccurances() throws TextScanManagerException {
        ByteArrayInputStream bais = new ByteArrayInputStream(TestString.getBytes(StandardCharsets.UTF_8));
        TextScannerImpl ts = new TextScannerImpl();
        ts.scan(bais, okRegex, null, 2);
        try {
        	bais = new ByteArrayInputStream(TestString.getBytes(StandardCharsets.UTF_8));
            ts.scan(bais, okRegex, null, 3);
            fail("Should not have found 3 occurances");
        } catch(IncorrectOccurancesException e) {}
    }
    @Test
    public void testInputStreamFailAfterOccurances() throws TextScanManagerException {
        ByteArrayInputStream bais = new ByteArrayInputStream(TestString.getBytes(StandardCharsets.UTF_8));
        TextScannerImpl ts = new TextScannerImpl();
        try {
            ts.scan(bais, okRegex, failRegex, 2);
            fail("Should have issued FailTextFoundException");
        } catch(FailTextFoundException e) {}
    }
}