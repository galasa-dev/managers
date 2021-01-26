/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.galasa.ManagerException;
import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurancesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanManagerException;

public class TextScannerImpl implements ITextScanner {


	@Override
	public ITextScanner scan(String text, Pattern searchPattern, Pattern failPattern, int count)
			throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {

		if (count < 1) {
			throw new IncorrectOccurancesException("Invalid occurancies number");
		}

		if (failPattern != null) {
			if (failPattern.matcher(text).find()) {
				throw new FailTextFoundException("Fail Pattern '" + failPattern + "' was found");
			}
		}

		Matcher m = searchPattern.matcher(text);
		int found = 0;

		while (m.find()) {
			found++;
		}

		if (found == 0) {
			throw new MissingTextException("Search Pattern '" + searchPattern + "' was not found");
		}
		else if (found < count) {
			throw new IncorrectOccurancesException("Was looking for " + count + " instances of '" + searchPattern + "' but found " + found);
		}

		return this;
	}

	@Override
	public ITextScanner scan(String text, String searchString, String failString, int count)
			throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {

		Pattern fp = Pattern.compile("\\Q" + failString + "\\E");
		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");

		return scan(text,p,fp,count);

	}

	@Override
	public ITextScanner scan(ITextScannable scannable, Pattern searchPattern, Pattern failPattern, int count)
			throws FailTextFoundException, MissingTextException,IncorrectOccurancesException,TextScanManagerException {
		if(scannable.isScannableInputStream()) {
			try {
				return scan(scannable.getScannableInputStream(),searchPattern,failPattern,count);
			} catch (FailTextFoundException e) {
				throw new FailTextFoundException();
			} catch (IncorrectOccurancesException e) {
				throw new IncorrectOccurancesException();
			} catch (MissingTextException e) {
				throw new MissingTextException();

			} catch (ManagerException e) {
				throw new TextScanManagerException();
			}	
		}else if(scannable.isScannableString()) {
			try {
				return scan(scannable.getScannableString(),searchPattern,failPattern,count);
			}  catch (FailTextFoundException e) {
				throw new FailTextFoundException();
			} catch (IncorrectOccurancesException e) {
				throw new IncorrectOccurancesException();
			} catch (MissingTextException e) {
				throw new MissingTextException();

			} catch (ManagerException e) {
				throw new TextScanManagerException();
			}	
		}
		/** It should never reach this line, it should either be an Input Steam or String **/
		throw new TextScanManagerException("Incorrect Scannable, must be String or an Input stream");
	}

	@Override
	public ITextScanner scan(ITextScannable scannable, String searchString, String failString, int count)
			throws TextScanManagerException{
		if(scannable.isScannableInputStream()) {
			try {
				return scan(scannable.getScannableInputStream(),searchString,failString,count);
			} catch (FailTextFoundException e) {
				throw new FailTextFoundException();
			} catch (IncorrectOccurancesException e) {
				throw new IncorrectOccurancesException();
			} catch (MissingTextException e) {
				throw new MissingTextException();

			} catch (ManagerException e) {
				throw new TextScanManagerException();
			}	
		}else if(scannable.isScannableString()) {
			try {
				return scan(scannable.getScannableString(),searchString,failString,count);
			} catch (FailTextFoundException e) {
				throw new FailTextFoundException();
			} catch (IncorrectOccurancesException e) {
				throw new IncorrectOccurancesException();
			} catch (MissingTextException e) {
				throw new MissingTextException();

			} catch (ManagerException e) {
				throw new TextScanManagerException();
			}	
		}
		/** It should never reach this line, it should either be an Input Steam or String **/
		throw new TextScanManagerException("Incorrect Scannable, must be String or an Input stream");
	}

	@Override
	public ITextScanner scan(InputStream inputStream, Pattern searchPattern, Pattern failPattern, int count)
			throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException {

		if (count < 1) {
			throw new IncorrectOccurancesException("Invalid occurancies number");
		}

		ArrayList<Integer> foundPatternStartPositions = new ArrayList<>();
		LinkedList<String> buffer = new LinkedList<String>();

		int offset = 0;

		try (BufferedReader  reader = new BufferedReader (new InputStreamReader(inputStream))){
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.add(line);
				if (buffer.size() > 10) {
					offset = offset + buffer.remove(0).length() + 1;
				}

				// Build a String from the current buffer
				StringBuffer sb = new StringBuffer();
				for (String queueLine : buffer) {
					sb.append(queueLine);
					sb.append("\n");
				}

				String currentBuffer = sb.toString();

				if (failPattern != null) {
					if (failPattern.matcher(currentBuffer).find()) {
						throw new FailTextFoundException("Fail Pattern '" + failPattern + "' was found");
					}
				}

				Matcher m = searchPattern.matcher(currentBuffer);
				while (m.find()) {

					int stp = m.start() + offset;
					if (!foundPatternStartPositions.contains(stp)) {
						foundPatternStartPositions.add(stp);

					}
				}
			}
			if (foundPatternStartPositions.size() >= count) {
				return this;
			}
		} catch (IOException e) {
			throw new TextScanManagerException();
		}

		if(foundPatternStartPositions.isEmpty()) {
			throw new MissingTextException("Search Pattern '" + searchPattern + "' was not found");
		}
		throw new IncorrectOccurancesException("Was looking for " + count + " instances of '" + searchPattern + "' but found " + foundPatternStartPositions.size());
	}

	@Override
	public ITextScanner scan(InputStream inputStream, String searchString, String failString, int count)
			throws FailTextFoundException, MissingTextException, IncorrectOccurancesException, TextScanManagerException {

		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");
		Pattern fp = Pattern.compile("\\Q" + failString + "\\E");
		return scan(inputStream,p,fp,count);
	}

	@Override
	public String scanForMatch(String text, Pattern searchPattern, int occurrence)
			throws MissingTextException, IncorrectOccurancesException {

		if(occurrence < 1) {
			throw new IncorrectOccurancesException("Invalid occurrance");
		}

		Matcher m = searchPattern.matcher(text);
		int found =0;
		String searchText = "";

		while (m.find()) {
			found++;
			searchText = m.group();
		}

		if (found == 0) {
			throw new MissingTextException("Search Pattern '" + searchPattern + "' was not found");
		}
		else if (found < occurrence) {
			throw new IncorrectOccurancesException("Was looking for " + occurrence + " instances of '" + searchPattern + "' but found " + found);
		}

		return searchText;
	}

	@Override
	public String scanForMatch(String text, String searchString, int occurrence)
			throws MissingTextException, IncorrectOccurancesException {

		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");

		return scanForMatch(text, p, occurrence);
	}

	@Override
	public String scanForMatch(ITextScannable scannable, Pattern searchPattern, int occurrence)
			throws MissingTextException, IncorrectOccurancesException, TextScanManagerException{
		if(scannable.isScannableInputStream()) {
			try {
				return scanForMatch(scannable.getScannableInputStream(),searchPattern,occurrence);
			} catch (IncorrectOccurancesException e) {
				throw new IncorrectOccurancesException();
			} catch (MissingTextException e) {
				throw new MissingTextException();

			} catch (ManagerException e) {
				throw new TextScanManagerException();
			}		
		}else if(scannable.isScannableString()) {
			try {
				return scanForMatch(scannable.getScannableString(),searchPattern,occurrence);
			} catch (IncorrectOccurancesException e) {
				throw new IncorrectOccurancesException();
			} catch (MissingTextException e) {
				throw new MissingTextException();

			} catch (ManagerException e) {
				throw new TextScanManagerException();
			}	
		}
		/** It should never reach this line, it should either be an Input Steam or String **/
		throw new TextScanManagerException("Incorrect Scannable, must be String or an Input stream");
	}

	@Override
	public String scanForMatch(ITextScannable scannable, String searchString, int occurrence)
			throws MissingTextException, IncorrectOccurancesException, TextScanManagerException {
		if(scannable.isScannableInputStream()) {
			try {
				return scanForMatch(scannable.getScannableInputStream(),searchString,occurrence);
			} catch (IncorrectOccurancesException e) {
				throw new IncorrectOccurancesException();
			} catch (MissingTextException e) {
				throw new MissingTextException();

			} catch (ManagerException e) {
				throw new TextScanManagerException();
			}		
		}else if(scannable.isScannableString()) {
			try {
				return scanForMatch(scannable.getScannableString(),searchString,occurrence);
			} catch (IncorrectOccurancesException e) {
				throw new IncorrectOccurancesException();
			} catch (MissingTextException e) {
				throw new MissingTextException();

			} catch (ManagerException e) {
				throw new TextScanManagerException();
			}	
		}
		/** It should never reach this line, it should either be an Input Steam or String **/
		throw new TextScanManagerException("Incorrect Scannable, must be String or an Input stream");
	}

	@Override
	public String scanForMatch(InputStream inputStream, Pattern searchPattern, int occurrence)
			throws MissingTextException, IncorrectOccurancesException, TextScanManagerException {

		if (occurrence < 1) {
			throw new IncorrectOccurancesException("Invalid occurancies number");
		}

		ArrayList<Integer> foundPatternStartPositions = new ArrayList<>();
		LinkedList<String> buffer = new LinkedList<String>();

		int offset = 0;
		String foundString = "";

		try (BufferedReader  reader = new BufferedReader (new InputStreamReader(inputStream))) {
			String line = "";
			while ((line =reader.readLine()) != null) {
				buffer.add(line);
				if (buffer.size() > 10) {
					offset = offset + buffer.remove(0).length() + 1;
				}

				// Build a String from the current buffer
				StringBuffer sb = new StringBuffer();
				for (String queueLine : buffer) {
					sb.append(queueLine);
					sb.append("\n");
				}

				String currentBuffer = sb.toString();
				Matcher m = searchPattern.matcher(currentBuffer);

				while (m.find()) {

					int stp = m.start() + offset;
					if (!foundPatternStartPositions.contains(stp)) {
						foundPatternStartPositions.add(stp);
						foundString = m.group();
					} 
					if (foundPatternStartPositions.size() >= occurrence) {
						return foundString;
					}
				}
			}

		} catch (IOException e) {
			throw new TextScanManagerException();
		}
		if(foundPatternStartPositions.isEmpty()) {
			throw new MissingTextException("Search Pattern '" + searchPattern + "' was not found");
		}
		throw new IncorrectOccurancesException("Was looking for " + occurrence + " instances of '" + searchPattern + "' but found " + foundPatternStartPositions.size());
	}

	@Override
	public String scanForMatch(InputStream inputStream, String searchString, int occurrence)
			throws MissingTextException, IncorrectOccurancesException, TextScanManagerException{
		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");
		return scanForMatch(inputStream, p, occurrence);
	}

}
