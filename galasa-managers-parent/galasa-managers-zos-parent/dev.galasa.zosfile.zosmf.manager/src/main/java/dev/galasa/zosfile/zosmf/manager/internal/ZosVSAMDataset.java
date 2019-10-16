package dev.galasa.zosfile.zosmf.manager.internal;

import java.io.InputStream;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosVSAMDataset;
import dev.galasa.zosfile.ZosVSAMDatasetException;

public class ZosVSAMDataset implements IZosVSAMDataset {

	public ZosVSAMDataset(IZosImage image, String dsName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSpace(VSAMSpaceUnit spaceUnit, int primaryExtents, int secondaryExtents) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setVolumes(String volumes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAccountInfo(String accountInfo) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBufferspace(long bufferspace) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBwoOption(BWOOption bwoOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setControlInterval(String controlInterval) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataclass(String dataclass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEraseOption(EraseOption eraseOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setExceptionExit(String exceptionExit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFreeSpaceOptions(int controlIntervalPercent, int controlAreaPercent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFrlogOption(FRLogOption frlogOption) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setDatasetOrg(DatasetOrganisation dataOrg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setKeyOptions(int length, int offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLogOption(LogOption logOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLogStreamID(String logStreamID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setManagementClass(String managementClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setModel(String modelEntryName, String modelCatName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOwner(String owner) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRecatalogOption(RecatalogOption recatalogOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRecordSize(int average, int max) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setReuseOption(ReuseOption reuseOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShareOptions(int crossRegion, int crossSystem) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSpanOption(SpanOption spanOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSpeedRecoveryOption(SpeedRecoveryOption speedRecoveryOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStorageClass(String storageClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWriteCheckOption(WriteCheckOption writeCheckOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUseDATA(boolean useDATA, boolean unique) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataName(String dataName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataSpace(VSAMSpaceUnit spaceunit, int primaryExtents, int secondaryExtents) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataVolumes(String dataVolumes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataBufferspace(long dataBufferspace) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataControlInterval(String dataControlInterval) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataEraseOption(EraseOption dataEraseOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataExceptionExit(String dataExceptionExit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataFreeSpaceOptions(int controlIntervalPercent, int controlAreaPercent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataKeyOptions(int length, int offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataModel(String modelEntryName, String modelCatName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataOwner(String dataOwner) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataRecordSize(int average, int max) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataReuseOption(ReuseOption dataReuseOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataShareOptions(int crossRegion, int crossSystem) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataSpanOption(SpanOption dataSpanOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataSpeedRecoveryOption(SpeedRecoveryOption dataSpeedRecoveryOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDataWriteCheckOption(WriteCheckOption dataWriteCheckOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUseINDEX(boolean useINDEX, boolean unique) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexName(String indexName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexSpace(VSAMSpaceUnit spacetype, int primaryExtents, int secondaryExtents) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexVolumes(String indexVolumes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexControlInterval(String indexControlInterval) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexExceptionExit(String indexExceptionExit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexModel(String indexModelEntryName, String indexModelCatName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexOwner(String indexOwner) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexReuseOption(ReuseOption indexReuseOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexShareOptions(int crossRegion, int crossSystem) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIndexWriteCheckOption(WriteCheckOption indexWriteCheckOption) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCatalog(String catalog) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDefineCommand() throws ZosVSAMDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDeleteCommand() throws ZosVSAMDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getReproCommand(String infile) throws ZosVSAMDatasetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setContent(String content) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setContent(InputStream input) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void appendContent(String content) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void appendContent(InputStream input) {
		throw new UnsupportedOperationException();
	}

}
