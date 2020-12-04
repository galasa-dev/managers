/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan;

import java.util.regex.Pattern;

/**
 * Provides utility text scanning routines for tests and Managers to use, intended for use with logs or batch jobs etc.
 * <br>
 * The scanner will remember where the last scan got to, so the next search will continue from that point.
 * <br>
 * You can obtain an implementation of this interface using the {@link LogScanner} annotation.
 * You will need a separate object per log you will be scanning.
 * 
 * @author Michael Baylis
 *
 */
public interface ILogScanner {
    
    /**
     * Set the scannable that will be used with this scanner and will update
     * 
     * @param scannable - The scannable to be associated with this scanner
     * @return this interface for fluent use
     * @throws TextScanManagerException If a scannable has already been set, updateText called or is null, or the first update fails
     */
    ILogScanner setScannable(ITextScannable scannable) throws TextScanManagerException;
    
    /**
     * Update the scannable.
     * 
     * @return this interface for fluent use
     * @throws TextScanManagerException
     */
    ILogScanner updateScannable() throws TextScanManagerException;
    
    /**
     * @param text set the latest text to be used for scanning,  must include all the text scanned so far, ie this is NOT
     * an append method
     * 
     * @return this interface for fluent use
     * @throws TextScanManagerException - if this scanner is already associated with a scanner
     */
    ILogScanner updateText(String text) throws TextScanManagerException;
    
    
    /**
     * Resets this scanner so it can be reused
     * 
     * @return this interface for fluent use
     */
    ILogScanner reset();
    
    
    /**
     * Sets the checkpoint to the end of the current log
     * 
     * @return this interface for fluent use
     */
    ILogScanner checkpoint();
    
    /**
     * Resets the checkpoint back to zero
     * 
     * @return this interface for fluent use
     */
    ILogScanner resetCheckpoint();
    
    
    /**
     * @return the current position of the checkpoint
     */
    long getCheckpoint();
    
    
    
    /**
     * Search the log for regex patterns, from the last checkpoint.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find atleast "count" number of searchPatterns in the text.
     * <br>NOTE: This method will scan from the start of the log,  it will not use the checkpoint
     * 
     * @param searchPattern - The regex to search for
     * @param failPattern - Failure regex to search for, can be null meaning no fail search
     * @param count - Atleast how many occurrences of the searchPattern must exist
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failurePattern was found
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     */
    ILogScanner scanSinceCheckpoint(Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scan(searchPattern, failPattern, 1)
     * 
     * @param searchPattern - The regex to search for
     * @param failPattern - Failure regex to search for, can be null meaning no fail search
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failurePattern was found
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     */
    ILogScanner scanSinceCheckpoint(Pattern searchPattern, Pattern failPattern) throws FailTextFoundException, MissingTextException;
    
    /**
     * Convenience method for scan(searchPattern, null, 1)
     * 
     * @param searchPattern - The regex to search for
     * @return This log scanner for fluent calls
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     */
    ILogScanner scanSinceCheckpoint(Pattern searchPattern) throws MissingTextException;
    
    
    
    
    /**
     * Convenience method for scanSinceCheckpoint(Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param searchString - The text to search for
     * @param failString - Failure text to search for, can be null meaning no fail search
     * @param count - Atleast how many occurrences of the searchString must exist
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failString was found
     * @throws MissingTextException - If no occurrences of the searchString was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     */
    ILogScanner scanSinceCheckpoint(String searchString, String failString, int count) throws MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scanSinceCheckpoint(searchString, failString, 1)
     * 
     * @param searchString - The text to search for
     * @param failString - Failure text to search for, can be null meaning no fail search
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failString was found
     * @throws MissingTextException - If no occurrences of the searchString was found 
     */
    ILogScanner scanSinceCheckpoint(String searchString, String failString) throws FailTextFoundException, MissingTextException;
    
    /**
     * Convenience method for scanSinceCheckpoint(searchString, null, 1)
     * 
     * @param searchString - The text to search for
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failString was found
     */
    ILogScanner scanSinceCheckpoint(String searchString) throws MissingTextException;
    
    
    
    /**
     * Will search the log from the checkpoint looking for the searchPattern.  When it finds the "occurrence" of the string, it will return the found text.
     * Useful for returning the actual value of the searchPattern
     * 
     * @param searchPattern - The searchPattern to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     * @throws IncorrectOccurancesException - The searchPattern was found, but not the x occurrence
     */
    String scanForMatchSinceCheckpoint(Pattern searchPattern, int occurrance) throws MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scanForMatchSinceCheckpoint(searchPattern, 1)
     * 
     * @param searchPattern - The searchPattern to look for
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     */
    String scanForMatchSinceCheckpoint(Pattern searchPattern) throws MissingTextException;
    
    /**
     * Convenience method for scanForMatchSinceCheckpoint(Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param searchString - The searchString to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchString was not found at all
     * @throws IncorrectOccurancesException - The searchString was found, but not the x occurrence
     */
    String scanForMatchSinceCheckpoint(String searchString, int occurrance) throws MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scanForMatchSinceCheckpoint(searchString, 1)
     * 
     * @param searchString - The searchString to look for
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchString was not found at all
     */
    String scanForMatchSinceCheckpoint(String searchString) throws MissingTextException;
    

    
    
    /**
     * Search the log for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find atleast "count" number of searchPatterns in the text.
     * <br>NOTE: This method will scan from the start of the log,  it will not use the checkpoint
     * 
     * @param searchPattern - The regex to search for
     * @param failPattern - Failure regex to search for, can be null meaning no fail search
     * @param count - Atleast how many occurrences of the searchPattern must exist
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failurePattern was found
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     */
    ILogScanner scan(Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scan(searchPattern, failPattern, 1)
     * 
     * @param searchPattern - The regex to search for
     * @param failPattern - Failure regex to search for, can be null meaning no fail search
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failurePattern was found
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     */
    ILogScanner scan(Pattern searchPattern, Pattern failPattern) throws FailTextFoundException, MissingTextException;
    
    /**
     * Convenience method for scan(searchPattern, null, 1)
     * 
     * @param searchPattern - The regex to search for
     * @return This log scanner for fluent calls
     * @throws MissingTextException - If no occurrences of the searchPattern was found 
     */
    ILogScanner scan(Pattern searchPattern) throws MissingTextException;
    
    
    
    
    /**
     * Convenience method for scan(Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param searchText - The text to search for
     * @param failText - Failure text to search for, can be null meaning no fail search
     * @param count - Atleast how many occurrences of the searchText must exist
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failText was found
     * @throws MissingTextException - If no occurrences of the searchText was found 
     * @throws IncorrectOccurancesException - If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     */
    ILogScanner scan(String searchString, String failString, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scan(searchText, failText, 1)
     * 
     * @param searchText - The text to search for
     * @param failText - Failure text to search for, can be null meaning no fail search
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException - If the failText was found
     * @throws MissingTextException - If no occurrences of the searchText was found 
     */
    ILogScanner scan(String searchString, String failString) throws FailTextFoundException, MissingTextException;

    /**
     * Convenience method for scan(searchText, null, 1)
     * 
     * @param searchText - The text to search for
     * @return This log scanner for fluent calls
     * @throws MissingTextException - If no occurrences of the searchText was found 
     */
    ILogScanner scan(String searchString) throws MissingTextException;

    
    
    
    
    /**
     * Will search the log looking for the searchPattern.  When it finds the "occurrence" of the string, it will return the found text.
     * Useful for returning the actual value of the searchPattern
     * 
     * @param searchPattern - The searchPattern to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     * @throws IncorrectOccurancesException - The searchPattern was found, but not the x occurrence
     */
    String scanForMatch(Pattern searchPattern, int occurrance) throws MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scanForMatch(searchText, 1)
     * 
     * @param searchPattern - The searchPattern to look for
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     */
    String scanForMatch(Pattern searchPattern) throws MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scanForMatch(Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param searchString - The searchString to look for
     * @param occurrence - The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     * @throws IncorrectOccurancesException - The searchPattern was found, but not the x occurrence
     */
    String scanForMatch(String searchString, int occurrance) throws MissingTextException, IncorrectOccurancesException;
    
    /**
     * Convenience method for scanForMatch(searchString, 1)
     * 
     * @param searchString - The searchString to look for
     * @return The text of the searchPattern found
     * @throws MissingTextException - The searchPattern was not found at all
     */
    String scanForMatch(String searchString) throws MissingTextException;
    
    
}
