/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan.spi;

import java.io.InputStream;
import java.util.regex.Pattern;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurancesException;
import dev.galasa.textscan.MissingTextException;

public class TextScannerImpl implements ITextScanner {

    @Override
    public ITextScanner scan(String text, Pattern searchPattern, Pattern failPattern, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITextScanner scan(String text, String searchString, String failString, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITextScanner scan(ITextScannable scannable, Pattern searchPattern, Pattern failPattern, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITextScanner scan(ITextScannable scannable, String searchString, String failString, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITextScanner scan(InputStream inputStream, Pattern searchPattern, Pattern failPattern, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITextScanner scan(InputStream inputStream, String searchString, String failString, int count)
            throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(String text, Pattern searchPattern, int occurrence)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(String text, String searchString, int occurrence)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(ITextScannable scannable, Pattern searchPattern, int occurrence)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(ITextScannable scannable, String searchString, int occurrence)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(InputStream inputStream, Pattern searchPattern, int occurrence)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String scanForMatch(InputStream inputStream, String searchString, int occurrence)
            throws MissingTextException, IncorrectOccurancesException {
        // TODO Auto-generated method stub
        return null;
    }

}
