package dev.galasa.common.zosfile.zosmf.manager.internal;

import java.io.InputStream;

import dev.galasa.common.zos.IZosImage;
import dev.galasa.common.zosfile.IZosVSAMDataset;
import dev.galasa.common.zosfile.ZosFileException;

public class ZosVSAMDataset implements IZosVSAMDataset {

	public ZosVSAMDataset(IZosImage image, String dsName) {
		// TODO Not yet implemented
	}

	@Override
	public String getName() {
		// TODO Not yet implemented
		return "dummy";
	}

	@Override
	public void setSpace(VSAMSpaceUnit spaceUnit, int primaryExtents, int secondaryExtents) {
		// TODO Not yet implemented

	}

	@Override
	public void setVolumes(String volumes) {
		// TODO Not yet implemented

	}

	@Override
	public void setAccountInfo(String accountInfo) {
		// TODO Not yet implemented

	}

	@Override
	public void setBufferspace(long bufferspace) {
		// TODO Not yet implemented

	}

	@Override
	public void setBwoOption(BWOOption bwoOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setControlInterval(String controlInterval) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataclass(String dataclass) {
		// TODO Not yet implemented

	}

	@Override
	public void setEraseOption(EraseOption eraseOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setExceptionExit(String exceptionExit) {
		// TODO Not yet implemented

	}

	@Override
	public void setFreeSpaceOptions(int controlIntervalPercent, int controlAreaPercent) {
		// TODO Not yet implemented

	}

	@Override
	public void setFrlogOption(FRLogOption frlogOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setDatasetOrg(DatasetOrganisation dataOrg) {
		// TODO Not yet implemented

	}

	@Override
	public void setKeyOptions(int length, int offset) {
		// TODO Not yet implemented

	}

	@Override
	public void setLogOption(LogOption logOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setLogStreamID(String logStreamID) {
		// TODO Not yet implemented

	}

	@Override
	public void setManagementClass(String managementClass) {
		// TODO Not yet implemented

	}

	@Override
	public void setModel(String modelEntryName, String modelCatName) {
		// TODO Not yet implemented

	}

	@Override
	public void setOwner(String owner) {
		// TODO Not yet implemented

	}

	@Override
	public void setRecatalogOption(RecatalogOption recatalogOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setRecordSize(int average, int max) {
		// TODO Not yet implemented

	}

	@Override
	public void setReuseOption(ReuseOption reuseOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setShareOptions(int crossRegion, int crossSystem) {
		// TODO Not yet implemented

	}

	@Override
	public void setSpanOption(SpanOption spanOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setSpeedRecoveryOption(SpeedRecoveryOption speedRecoveryOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setStorageClass(String storageClass) {
		// TODO Not yet implemented

	}

	@Override
	public void setWriteCheckOption(WriteCheckOption writeCheckOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setUseDATA(boolean useDATA, boolean unique) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataName(String dataName) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataSpace(VSAMSpaceUnit spaceunit, int primaryExtents, int secondaryExtents) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataVolumes(String dataVolumes) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataBufferspace(long dataBufferspace) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataControlInterval(String dataControlInterval) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataEraseOption(EraseOption dataEraseOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataExceptionExit(String dataExceptionExit) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataFreeSpaceOptions(int controlIntervalPercent, int controlAreaPercent) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataKeyOptions(int length, int offset) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataModel(String modelEntryName, String modelCatName) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataOwner(String dataOwner) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataRecordSize(int average, int max) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataReuseOption(ReuseOption dataReuseOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataShareOptions(int crossRegion, int crossSystem) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataSpanOption(SpanOption dataSpanOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataSpeedRecoveryOption(SpeedRecoveryOption dataSpeedRecoveryOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setDataWriteCheckOption(WriteCheckOption dataWriteCheckOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setUseINDEX(boolean useINDEX, boolean unique) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexName(String indexName) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexSpace(VSAMSpaceUnit spacetype, int primaryExtents, int secondaryExtents) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexVolumes(String indexVolumes) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexControlInterval(String indexControlInterval) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexExceptionExit(String indexExceptionExit) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexModel(String indexModelEntryName, String indexModelCatName) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexOwner(String indexOwner) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexReuseOption(ReuseOption indexReuseOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexShareOptions(int crossRegion, int crossSystem) {
		// TODO Not yet implemented

	}

	@Override
	public void setIndexWriteCheckOption(WriteCheckOption indexWriteCheckOption) {
		// TODO Not yet implemented

	}

	@Override
	public void setCatalog(String catalog) {
		// TODO Not yet implemented

	}

	@Override
	public String getDefineCommand() throws ZosFileException {
		// TODO Not yet implemented
		return "dummy";
	}

	@Override
	public String getDeleteCommand() throws ZosFileException {
		// TODO Not yet implemented
		return "dummy";
	}

	@Override
	public String getReproCommand(String infile) throws ZosFileException {
		// TODO Not yet implemented
		return "dummy";
	}

	@Override
	public void setContent(String content) {
		// TODO Not yet implemented

	}

	@Override
	public void setContent(InputStream input) {
		// TODO Not yet implemented

	}

	@Override
	public void appendContent(String content) {
		// TODO Not yet implemented

	}

	@Override
	public void appendContent(InputStream input) {
		// TODO Not yet implemented

	}

}
