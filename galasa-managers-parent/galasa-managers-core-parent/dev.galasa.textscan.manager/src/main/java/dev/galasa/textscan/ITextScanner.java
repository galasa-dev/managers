/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import dev.galasa.ManagerException;

/**
 * Provides utility text scanning routines for tests and Managers to use, intended for use with logs or batch jobs etc.
 * <br>
 * The implementation of this interface will not record the text being searched, so the implementation
 * can be reused against different resources.
 * <br>
 * If you require the ability to remember where the scan reached in a "log", use the {@link ILogScanner}.
 * <br>
 * You can obtain an implementation of this interface using the {@link TextScanner} annotation.
 * 
 * @author Michael Baylis
 *
 */
public interface ITextScanner {
    
    /**
     * Search a String for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * 
     * @param text - The text to be searched
     * @param searchPattern - The regex to search for
     * @param failPattern - Failure regex to search for, can be null meaning no fail search
     * @param count - At least how many occurrences of the searchPattern must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException - If the failurePattern was found
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     */
    ITextScanner scan(String text, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scan(text, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param text - The text to be searched
     * @param searchLiteral - The exact text to search for
     * @param failLiteral - The exact failure text to search for, can be null meaning no fail search
     * @param count - At least how many occurrences of the searchLiteral must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException - If the failureLiteral was found
     * @throws MissingTextException - If no occurrences of the searchLiteral was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     */
    ITextScanner scan(String text, String searchLiteral, String failLiteral, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException;
    
    /**
     * Search a IScannable for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * 
     * @param scannable - The scannable to be searched
     * @param searchPattern - The regex to search for
     * @param failPattern - Failure regex to search for, can be null meaning no fail search
     * @param count - At least how many occurrences of the searchPattern must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException - If the failurePattern was found
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     * @throws ManagerException  
     * @throws IOException 
     */
    ITextScanner scan(ITextScannable scannable, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException, ManagerException, IOException;
    
    /**
     * Convenience method for scan(scannable, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param scannable - The scannable to be searched
     * @param searchLiteral - The exact text to search for
     * @param failLiteral - The exact failure text to search for, can be null meaning no fail search
     * @param count - At least how many occurrences of the searchLiteral must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException - If the failureLiteral was found
     * @throws MissingTextException - If no occurrences of the searchLiteral was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     * @throws ManagerException 
     * @throws IOException 
     */
    ITextScanner scan(ITextScannable scannable, String searchLiteral, String failLiteral, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException, ManagerException, IOException;
    
    /**
     * Search an InputStream for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * <br>
     * NOTE: unlike the scannable/string scans, this method will scan the text on a line by line basis, using a BufferedReader, to prevent the JVM Heap from being exceeded.
     * therefore you will not be able to use multiline patterns
     * 
     * @param inputStream - The inputStream  to be searched
     * @param searchPattern - The regex to search for
     * @param failPattern - Failure regex to search for, can be null meaning no fail search
     * @param count - At least how many occurrences of the searchPattern must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException - If the failurePattern was found
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     * @throws IOException 
     */
    ITextScanner scan(InputStream inputStream, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException, IOException;
    
    /**
     * Convenience method for scan(inputStream, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param inputStream - The inputStream to be searched
     * @param searchLiteral - The exact text to search for
     * @param failLiteral - The exact failure text to search for, can be null meaning no fail search
     * @param count - At least how many occurrences of the searchText must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException - If the failureLiteral was found
     * @throws MissingTextException - If no occurrences of the searchLiteral was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     * @throws IOException 
     */
    ITextScanner scan(InputStream inputStream, String searchLiteral, String failLiteral, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException, IOException;
    
    /**
     * Will search the text looking for the searchPattern.  When it finds the "occurrence" of the string, it will return the found text.
     * Useful for returning the actual value of the searchPattern
     * 
     * @param text - the text being searched
     * @param searchPattern - The searchPattern to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     * @throws IncorrectOccurancesException - The searchPattern was found, but not the x occurrence
     */
    String scanForMatch(String text, Pattern searchPattern, int occurrence) throws MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scanForMatch(text, Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param text - the text being searched
     * @param searchLiteral - The exact text to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchLiteral found
     * @throws MissingTextException - The searchLiteral was not found at all
     * @throws IncorrectOccurancesException - The searchLiteral was found, but not the x occurrence
     */
    String scanForMatch(String text, String searchLiteral, int occurrence) throws MissingTextException, IncorrectOccurancesException;
    
    /**
     * Will search the scannable looking for the searchPattern.  When it finds the "occurrence" of the string, it will return the found text.
     * Useful for returning the actual value of the searchPattern
     * 
     * @param scannable - the scannable being searched
     * @param searchPattern - The searchPattern to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     * @throws IncorrectOccurancesException - The searchPattern was found, but not the x occurrence
     * @throws ManagerException 
     * @throws IOException 
     */
    String scanForMatch(ITextScannable scannable, Pattern searchPattern, int occurrence) throws MissingTextException, IncorrectOccurancesException, ManagerException, IOException;
    
    /**
     * Convenience method for scanForMatch(scannable, Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param scannable - the text being searched
     * @param searchLiteral - The exact string to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchLiteral found
     * @throws MissingTextException - The searchLiteral was not found at all
     * @throws IncorrectOccurancesException - The searchLiteral was found, but not the x occurrence
     * @throws ManagerException 
     * @throws IOException 
     */
    String scanForMatch(ITextScannable scannable, String searchLiteral, int occurrence) throws MissingTextException, IncorrectOccurancesException, ManagerException, IOException;
    
    /**
     * Will search the inputStream looking for the searchPattern.  When it finds the "occurrence" of the string, it will return the found text.
     * Useful for returning the actual value of the searchPattern
     * 
     * NOTE: unlike the scannable/string scanForMatch, this method will scan the text on a line by line basis, using a BufferedReader, to prevent the JVM Heap from being exceeded.
     * therefore you will not be able to use multiline patterns
     * 
     * @param inputStream - the inputStream being searched
     * @param searchPattern - The searchPattern to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     * @throws IncorrectOccurancesException - The searchPattern was found, but not the x occurrence
     * @throws IOException 
     */
    String scanForMatch(InputStream inputStream, Pattern searchPattern, int occurrence) throws MissingTextException, IncorrectOccurancesException, IOException;
    
    /**
     * Convenience method for scanForMatch(inputStream, Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param inputStream - the text being searched
     * @param searchLieteral - The exact text to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchLiteral found
     * @throws MissingTextException - The searchLiteral was not found at all
     * @throws IncorrectOccurancesException - The searchLiteral was found, but not the x occurrence
     * @throws IOException 
     */
    String scanForMatch(InputStream inputStream, String searchLiteral, int occurrence) throws MissingTextException, IncorrectOccurancesException, IOException;
    
}
