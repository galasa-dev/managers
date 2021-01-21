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
import java.io.StringReader;
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
		if (failPattern != null) {
			if (failPattern.matcher(text).find()) {
				throw new FailTextFoundException("Fail Pattern '"+failPattern+"' was found");
			}
		}
		if(count<1) {
			throw new IncorrectOccurancesException("Invalid occurancies number");
		}
		Matcher m = searchPattern.matcher(text);
		int found =0;
		int occurance = count;
		boolean find = false;
		while (occurance > 0) {
			find = m.find();
			if (!find&& found ==0) {
				throw new MissingTextException("Search Pattern '"+searchPattern+"' was not found");
			}
			else if(!find) {
				throw new IncorrectOccurancesException("Was looking for "+count+" instances of '"+searchPattern+"' but found "+ found);
			}
			found++;
			occurance--;
		}
		return this;
	}

	@Override
	public ITextScanner scan(String text, String searchString, String failString, int count)
			throws FailTextFoundException, MissingTextException, IncorrectOccurancesException {

		Pattern fp = Pattern.compile("\\Q" + failString + "\\E",Pattern.DOTALL | Pattern.MULTILINE);
		Pattern p = Pattern.compile("\\Q" + searchString + "\\E",Pattern.DOTALL | Pattern.MULTILINE);

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
			throws FailTextFoundException, MissingTextException, IncorrectOccurancesException,TextScanManagerException{
		if(count<1) {
			throw new IncorrectOccurancesException("Invalid occurancies number");
		}
		searchPattern = Pattern.compile(searchPattern.toString(),Pattern.MULTILINE);
		ArrayList<Integer> array = new ArrayList<>();
		BufferedReader  reader = new BufferedReader (new InputStreamReader(inputStream));
		LinkedList<String> queue = new LinkedList<String>();
		String line = "";
		String toScan = "";
		int offset = 0;
		int stp = 0;
		int found =0;
		boolean find = false;
		int lines = 0;
		int occurance = 0;

		try {

			while((line =reader.readLine())!=null) {
				occurance = count;
				queue.add(line);
				if(queue.size()>=10) {
					toScan ="";
					for(int i=0;i<queue.size();i++) {
						toScan+=queue.get(i)+"\n";
					}
					if (failPattern != null) {
						if (failPattern.matcher(toScan).find()) {
							throw new FailTextFoundException("Fail Pattern '"+failPattern+"' was found");
						}
					}
					Matcher m = searchPattern.matcher(toScan);
					while (occurance > 0) {
						stp=0;
						find = m.find();
						if(find) {
							stp = m.start()+offset;
							if(!array.contains(stp)) {
							found++;
							array.add(stp);

							}
						}
						occurance--;
					}
					offset = offset + queue.getFirst().length()+1;
						queue.poll();
					
				}
				lines ++;
			}
		} catch (IOException e) {
			throw new TextScanManagerException();
		}
		if (lines<10) {
			toScan ="";
			for(int i=0;i<queue.size();i++) {
				toScan+=queue.get(i)+"\n";
			}
			scan(toScan,searchPattern,failPattern,count);
		}else if(found>0&&found<count) {
			throw new IncorrectOccurancesException("Was looking for "+count+" instances of '"+searchPattern+"' but found "+ found);
		}else if(found==0) {
			throw new MissingTextException("Search Pattern '"+searchPattern+"' was not found");
		}

		return this;
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

		if(occurrence<1) {
			throw new IncorrectOccurancesException("Invalid occurrance");
		}
		Matcher m = searchPattern.matcher(text);
		int found =0;
		String searchText = "";
		boolean find = false;
		while (occurrence > 0) {
			find=m.find();
			if (!find&& found ==0) {
				throw new MissingTextException("Search Pattern '"+searchPattern+"' was not found");
			}
			else if(!find) {
				throw new IncorrectOccurancesException("Was looking for "+occurrence+" instances of '"+searchPattern+"' but found "+ found);
			}
			found++;
			occurrence--;
			searchText = m.group();
		}
		return searchText;
	}

	@Override
	public String scanForMatch(String text, String searchString, int occurrence)
			throws MissingTextException, IncorrectOccurancesException {

		Pattern p = Pattern.compile("\\Q" + searchString + "\\E",Pattern.DOTALL | Pattern.MULTILINE);

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
		if(occurrence<1) {
			throw new IncorrectOccurancesException("Invalid occurancies number");
		}
		BufferedReader  reader = new BufferedReader (new InputStreamReader(inputStream)); 
		ArrayList<Integer> array = new ArrayList<>();
		LinkedList<String> queue = new LinkedList<String>();
		searchPattern = Pattern.compile(searchPattern.toString(),Pattern.MULTILINE);
		String line = "";
		String toScan = "";
		String string = "";
		int found =0;
		int offset = 0;
		int stp = 0;
		boolean find = false;
		int lines = 0;
		int count = 0;

		try {
			while((line =reader.readLine())!=null) {
				count = occurrence;
				queue.add(line);
				if(queue.size()>=10) {
					toScan ="";
					for(int i=0;i<queue.size();i++) {
						toScan+=queue.get(i)+"\n";
					}
					Matcher m = searchPattern.matcher(toScan);
					while (count > 0) {
						stp=0;
						find = m.find();
						if(find) {
							string=m.group();
							stp = m.start()+offset;
							if(!array.contains(stp)) {
							found++;
							array.add(stp);

							}
						}
						count--;
					}
					offset = offset + queue.getFirst().length()+1;
					queue.poll();
				}
				lines ++;
			}
		} catch (IOException e) {
			throw new TextScanManagerException();
		}
		if (lines<10) {
			toScan ="";
			for(int i=0;i<queue.size();i++) {
				toScan+=queue.get(i)+"\n";
			}
			string = scanForMatch(toScan,searchPattern,occurrence);

		}else if(found ==0) {
			throw new MissingTextException("Search Pattern '"+searchPattern+"' was not found");
		}else if(found>0&&found<occurrence) {
			throw new IncorrectOccurancesException("Was looking for "+count+" instances of '"+searchPattern+"' but found "+ found);
		}


		return string;
	}

	@Override
	public String scanForMatch(InputStream inputStream, String searchString, int occurrence)
			throws MissingTextException, IncorrectOccurancesException, TextScanManagerException{
		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");
		return scanForMatch(inputStream, p, occurrence);
	}

}
