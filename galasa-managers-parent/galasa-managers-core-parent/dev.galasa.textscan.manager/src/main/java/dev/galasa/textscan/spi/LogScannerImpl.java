/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan.spi;

import java.util.regex.Pattern;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.IncorrectOccurancesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanManagerException;

public class LogScannerImpl implements ILogScanner {

    @Override
    public ILogScanner setScannable(ITextScannable scannable) throws TextScanManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner updateScannable() throws TextScanManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner updateText(String text) throws TextScanManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner reset() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner checkpoint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner resetCheckpoint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getCheckpoint() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ILogScanner scanSinceCheckpoint(Pattern searchPattern, Pattern failPattern, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scanSinceCheckpoint(Pattern searchPattern, Pattern failPattern)
            throws FailTextFoundException, MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scanSinceCheckpoint(Pattern searchPattern) throws MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scanSinceCheckpoint(String searchString, String failString, int count)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scanSinceCheckpoint(String searchString, String failString)
            throws FailTextFoundException, MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scanSinceCheckpoint(String searchString) throws MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatchSinceCheckpoint(Pattern searchPattern, int occurrance)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatchSinceCheckpoint(Pattern searchPattern) throws MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatchSinceCheckpoint(String searchString, int occurrance)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatchSinceCheckpoint(String searchString) throws MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scan(Pattern searchPattern, Pattern failPattern, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scan(Pattern searchPattern, Pattern failPattern)
            throws FailTextFoundException, MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scan(Pattern searchPattern) throws MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scan(String searchString, String failString, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scan(String searchString, String failString)
            throws FailTextFoundException, MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILogScanner scan(String searchString) throws MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(Pattern searchPattern, int occurrance)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(Pattern searchPattern) throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(String searchString, int occurrance)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(String searchString) throws MissingTextException {
        // TODO Auto-generated method stub
        return null;
    }

}
