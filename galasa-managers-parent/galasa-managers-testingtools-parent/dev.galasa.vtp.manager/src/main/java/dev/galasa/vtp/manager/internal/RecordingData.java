package dev.galasa.vtp.manager.internal;

import java.util.ArrayList;

import dev.galasa.cicsts.ICicsTerminal;

public class RecordingData {
	
	private char [] recordingToken;
	private ICicsTerminal   recordingTerminal;
	private ArrayList<String> recordingTransactions;

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

	public RecordingData() {
	}

}
