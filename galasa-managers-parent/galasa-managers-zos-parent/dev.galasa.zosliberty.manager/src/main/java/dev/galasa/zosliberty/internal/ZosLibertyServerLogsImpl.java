/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.zosliberty.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;

import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServerLogs;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyServerLogsImpl implements IZosLibertyServerLogs {

    private IZosUNIXFile logsDirectory;
    private HashMap<String, IZosUNIXFile> logs;
    private boolean hasFFDC;
    private Iterator<Entry<String, IZosUNIXFile>> logsIterator;
    private String currentLogName;

    public ZosLibertyServerLogsImpl(IZosUNIXFile logsDirectory) throws ZosLibertyServerException {
        this.logsDirectory = logsDirectory;
        this.logs = listServerLogsDirectory();
    }

    @Override
    public boolean hasFFDC() {
        return this.hasFFDC;
    }

    @Override
    public int numberOfFiles() {
        return this.logs.size();
    }

    @Override
    public IZosUNIXFile getLog(String fileName) {
        return this.logs.get(fileName);
    }

    @Override
    public IZosUNIXFile getMessagesLog() {
        return getLog("messages.log");
    }

    @Override
    public IZosUNIXFile getNext() {
        if (this.logsIterator.hasNext()) {
            Entry<String, IZosUNIXFile> logsEntry = this.logsIterator.next();
            this.currentLogName = logsEntry.getKey();
            return getLog(this.currentLogName);
        }
        return null;
    }

    @Override
    public IZosUNIXFile getNextFfdc() {
        while (this.logsIterator.hasNext()) {
            Entry<String, IZosUNIXFile> logsEntry = this.logsIterator.next();
            this.currentLogName = logsEntry.getKey();
            if (isFfdc(logsEntry.getKey())) {
                return getLog(this.currentLogName);
            }
        }
        return null;
    }

    @Override
    public String getCurrentLogName() {
        return this.currentLogName;
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException {
        for (Entry<String, IZosUNIXFile> entry : listServerLogsDirectory().entrySet()) {
            String logsRasPath = rasPath + "/logs/";
            try {
                logsRasPath = logsRasPath + entry.getKey().substring(0, entry.getKey().length()-entry.getValue().getFileName().length());
                entry.getValue().saveToResultsArchive(logsRasPath);
            } catch (ZosUNIXFileException e) {
                throw new ZosLibertyServerException("Unable to store '" + entry.getKey() + " to results archive store", e);
            }
        }
    }

    @Override
    public void refresh() throws ZosLibertyServerException {
    	listServerLogsDirectory();
    }

    private HashMap<String, IZosUNIXFile> listServerLogsDirectory() throws ZosLibertyServerException {
        this.hasFFDC = false;
        this.logs = new HashMap<>();
        try {
            if (this.logsDirectory.exists()) {
                SortedMap<String, IZosUNIXFile> sortedMap = this.logsDirectory.directoryListRecursive();
                for (Entry<String, IZosUNIXFile> entry : sortedMap.entrySet()) {
                    IZosUNIXFile zosUnixFile = entry.getValue();
                    if (!zosUnixFile.isDirectory()) {
                        zosUnixFile.setDataType(UNIXFileDataType.BINARY);
                        String fileName = zosUnixFile.getUnixPath().substring(this.logsDirectory.getUnixPath().length());
                        if (!fileName.startsWith("state/")) {
                            this.logs.put(fileName, zosUnixFile);
                        }
                        if (isFfdc(zosUnixFile.getUnixPath())) {
                            this.hasFFDC = true;
                        }
                    }
                }
            }
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to list content of logs directory", e);
        }
        this.logsIterator = this.logs.entrySet().iterator();
        return this.logs;
	}

	private boolean isFfdc(String file) {
        return file.matches(".*/ffdc/ffdc_.*\\.log$") || file.matches("ffdc/ffdc_.*\\.log$");
    }
}
