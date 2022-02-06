/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.vtp.manager.internal;

import java.util.ArrayList;
import java.util.HashMap;

import dev.galasa.cicsts.ICicsTerminal;

public class RecordingData {
	
	private char [] recordingToken;
	private ICicsTerminal   recordingTerminal;
	private ArrayList<String> recordingTransactions;
	private HashMap<String, String> exportedRecordings = new HashMap<>();

	public char[] getRecordingToken() {
		return recordingToken;
	}

	public void setRecordingToken(char[] recordingToken) {
		this.recordingToken = recordingToken;
	}

	public ICicsTerminal getRecordingTerminal() {
		return recordingTerminal;
	}

	public void setRecordingTerminal(ICicsTerminal recordingTerminal) {
		this.recordingTerminal = recordingTerminal;
	}

	public ArrayList<String> getRecordingTransactions() {
		return recordingTransactions;
	}

	public void setRecordingTransactions(ArrayList<String> recordingTransactions) {
		this.recordingTransactions = recordingTransactions;
	}
	
	public void addExportedRecording(String dsName, String method) {
		this.exportedRecordings.put(dsName, method);
	}

	public RecordingData() {
	}

}
