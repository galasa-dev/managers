package dev.galasa.zosfile.zosmf.manager.internal;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosDataset;

public class ZosDatasetImpl implements IZosDataset {

	public ZosDatasetImpl(IZosImage image, String dsName) {
		// TODO Not yet implemented
	}

	@Override
	public void setMemberName(String member) {
		// TODO Not yet implemented

	}

	@Override
	public String getMemberName() {
		// TODO Not yet implemented
		return "dummy";
	}

	@Override
	public String getDatasetName() {
		// TODO Not yet implemented
		return "dummy";
	}

	@Override
	public String getDatasetFormatString() {
		// TODO Not yet implemented
		return "dummy";
	}

	@Override
	public void setRecordFormat(RecordFormat recordFormat) {
		// TODO Not yet implemented

	}

	@Override
	public void setRecordlength(int recordlength) {
		// TODO Not yet implemented

	}

	@Override
	public void setBlockSize(int blockSize) {
		// TODO Not yet implemented

	}

	@Override
	public void setSpace(SpaceUnit spaceUnit, int primaryExtent, int secondaryExtents) {
		// TODO Not yet implemented

	}

	@Override
	public void setDatasetType(DSType dsType) {
		// TODO Not yet implemented

	}

	@Override
	public DSType getDatasetType() {
		// TODO Not yet implemented
		return DSType.BASIC;
	}

}
