package dev.galasa.vtp.manager.internal;

import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.vtp.manager.VtpManagerException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.spi.IZos3270ManagerSpi;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.spi.IZosBatchSpi;

public class VtpTxnRecorderImpl extends VtpRecorderImpl {

	public VtpTxnRecorderImpl(HashMap<ICicsRegion, RecordingData> recordingRegions, String HLQ, Log logger, VtpManagerImpl manager) {
		super(recordingRegions, HLQ, logger, manager);
	}

	@Override
	void startRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Starting VTP Recording");
			try {
				startRecordingUsingTxn(region, terminal);
			}catch(VtpManagerException e) {
				logger.error("Was unable to start recording on region: " + region.getApplid(),e);
			}		
		}
	}
	
	private void startRecordingUsingTxn(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException{
		for(String transaction : recordingRegions.get(region).getRecordingTransactions()) {
			String command = "BZUT " + transaction;
			String expectedResponse = "RECORDING STARTED FOR TRANSACTION " + transaction;
			try {
				terminal.type(command).enter();
				terminal.waitForTextInField(expectedResponse);
				terminal.clear().wfk();
			} catch (Zos3270Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	void endRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Stopping VTP Recording");
			try {
				stopRecordingUsingTxn(region, terminal);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording on region: " + region.getApplid(),e);
			}
		}
	}
	
	private void stopRecordingUsingTxn(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException{
		try {
			terminal.type("BZUE").enter();
			terminal.waitForTextInField("RECORDING STOPPED");
			terminal.clear().wfk();
		} catch (Zos3270Exception e) {
			throw new VtpManagerException(e);
		}
	}

	@Override
	void writeRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Writing VTP Recording");
			try {
				writeRecordingUsingTxn(region, terminal);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording on region: " + region.getApplid(),e);
			}
		}
	}
	
	private void writeRecordingUsingTxn(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException{
		try {
			terminal.type("BZUW").enter();
			terminal.waitForTextInField("RECORDS WRITTEN");
			terminal.clear().wfk();
			
		} catch (Zos3270Exception e) {
			throw new VtpManagerException(e);
		} 
	}

	@Override
	void exportRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Exporting VTP Recording");
			try {
				exportRecordingUsingTxn(region, terminal);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording on region: " + region.getApplid(),e);
			}
		}
	}
	
	private void exportRecordingUsingTxn(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException {
		try {
			CicstsHashMap tdqAttrs = region.cemt().inquireResource(terminal, "TDQ", "BZUQ");
			String dsName = tdqAttrs.get("dsname");
			region.cemt().setResource(terminal, "TDQ", "BZUQ", "CLOSED");
			String dumpTargetDSName = this.dumpHLQ + ".R" + this.recordingNumber;
			this.recordingNumber++;
			HashMap<String, Object> attrs = new HashMap<>();
			attrs.put("SOURCE", dsName);
			attrs.put("TARGET", dumpTargetDSName);
			manager.copyDumpedPlaybackFile(region.getZosImage(), attrs);
			this.recordingRegions.get(region).addExportedRecording(dumpTargetDSName, this.currentMethod);
			region.cemt().setResource(terminal, "TDQ", "BZUQ", "OPEN");
		} catch (Exception e) {
			throw new VtpManagerException("unable to get dsname of TDQ BZUQ",e);
		}
		
	}

}
