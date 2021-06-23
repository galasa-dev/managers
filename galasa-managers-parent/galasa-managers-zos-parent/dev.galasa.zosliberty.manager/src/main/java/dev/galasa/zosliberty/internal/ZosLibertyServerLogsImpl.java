/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty.internal;

import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosliberty.IZosLibertyServerLogs;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyServerLogsImpl implements IZosLibertyServerLogs {

	private static final String SLASH_SYMBOL = "/";
	private IZosUNIXFile logsDirectory;

	public ZosLibertyServerLogsImpl(IZosUNIXFile logsDirectory) {
		this.logsDirectory = logsDirectory;
	}

	@Override
	public boolean hasFFDC() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int numberOfFiles() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OutputStream getLog(String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getMessagesLog() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getNextFfdc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getNext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCurrentLogName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException {
		for (Entry<String, IZosUNIXFile> entry : listServerLogsDirectory().entrySet()) {
			try {
				StringBuilder logsRasPath =  new StringBuilder();
				logsRasPath.append(rasPath);
				logsRasPath.append(SLASH_SYMBOL);
				logsRasPath.append("logs");
				if (isFfdc(entry.getKey())) {
					logsRasPath.append(SLASH_SYMBOL);
					logsRasPath.append("ffdc");
				}
				entry.getValue().saveToResultsArchive(logsRasPath.toString());
			} catch (ZosUNIXFileException e) {
				throw new ZosLibertyServerException("Unable to store '" + entry.getKey() + " to results archive store", e);
			}
		}
	}

	private SortedMap<String, IZosUNIXFile> listServerLogsDirectory() throws ZosLibertyServerException {
		SortedMap<String, IZosUNIXFile> directoryList = new TreeMap<>();
		try {
			SortedMap<String, IZosUNIXFile> sortedMap = this.logsDirectory.directoryListRecursive();
			for (Entry<String, IZosUNIXFile> entry : sortedMap.entrySet()) {
				String file = entry.getKey();
				if (isMessagesLog(file) || isTrace(file)|| isFfdc(file)) {
					entry.getValue().setDataType(UNIXFileDataType.BINARY);
					directoryList.put(entry.getKey(), entry.getValue());
					continue;
				}
			}
		} catch (ZosUNIXFileException e) {
			throw new ZosLibertyServerException("Unable to list content of logs directory", e);
		}
		return directoryList;
	}

	private boolean isMessagesLog(String file) {
		return file.matches(".*/messages.*\\.log$");
	}

	private boolean isTrace(String file) {
		return file.matches(".*/trace.*\\.log$");
	}

	private boolean isFfdc(String file) {
		return file.matches(".*/ffdc/exception_summary.*\\.log$") || file.matches(".*/ffdc/ffdc_.*\\.log$");
	}
}
