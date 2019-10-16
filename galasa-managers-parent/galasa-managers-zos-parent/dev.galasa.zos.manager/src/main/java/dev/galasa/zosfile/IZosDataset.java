package dev.galasa.zosfile;

import java.util.Collection;

import javax.validation.constraints.NotNull;

/**
 * Representation of a non-VSAM data set
 */
public interface IZosDataset {
	
	/**
	 * Enumeration of data set types
	 */
	public enum DSType {

		HFS,
		PDS(true),
		PDSE(true),
		LARGE,
		BASIC,
		EXTREQ,
		EXTPREF;
		
		private boolean pds;
		
		DSType() {
			this(false);
		}
		
		DSType(boolean pds) {
			this.pds = pds;
		}
		
		/**
		 * @return - true if the type is a PDS or Library
		 */
		public boolean isPDS() {
			return pds;
		}
	}
	
	/**
	 * Enumeration of record format options
	 */
	public enum RecordFormat {
		
		BLOCK("B"),
		FIXED("F"),
		FIXED_BLOCKED("FB"),
		VARIABLE("V"),
		VARIABLE_BLOCKED("VB"),
		UNDEFINED("U");
		
		private String format;
		
		RecordFormat(String format) {
			this.format = format;
		}
		
		@Override
		public String toString() {
			return format;
		}
	}
	
	/**
	 * Enumeration of data set format options
	 */
	public enum DatasetOrganization {
		
		PARTITIONED("PO"),
		SEQUENTIAL("PS");
		
		private String format;
		
		DatasetOrganization(String format) {
			this.format = format;
		}
		
		@Override
		public String toString() {
			return format;
		}
	}
	
	/**
	 * Enumeration of space units for data set allocation
	 */
	public enum SpaceUnit {
		TRACKS("TRK"),
		CYLINDERS("CYL");
		
		private String unit;
		
		SpaceUnit(String spaceUnit) {
			this.unit = spaceUnit;
		}
		
		@Override
		public String toString() {
			return unit;
		}
	}
	
	/**
	 * Enumeration of data set data type
	 */
	public enum DatasetDataType {
		
		TEXT("text"),
		BINARY("binary"),
		RECORD("record");
		
		private String dataType;
		
		DatasetDataType(String dataType) {
			this.dataType = dataType;
		}
		
		@Override
		public String toString() {
			return dataType;
		}
	}
	
	/**
	 * Allocate the data set
	 * @throws ZosDatasetException 
	 */
	public void create() throws ZosDatasetException;

	/**
	 * Delete the datas et on zOS image.
	 * @throws ZosDatasetException
	 */
	public void delete() throws ZosDatasetException;

	/**
	 * Returns true if the data set exists 
	 * @return 
	 * @throws ZosDatasetException
	 */
	public boolean exists() throws ZosDatasetException;

	/**
	 * Write to content to data set 
	 * @param content
	 * @param dataType
	 * @throws ZosDatasetException
	 */
	public void store(@NotNull String content) throws ZosDatasetException;
	
	/**
	 * Retrieve content of data set
	 * @return data set content
	 * @throws ZosDatasetException
	 */
	public String retrieve() throws ZosDatasetException;
	
	public boolean isPDS() throws ZosDatasetException;

	public void memberCreate(@NotNull String memberName) throws ZosDatasetException;

	public void memberDelete(@NotNull String memberName) throws ZosDatasetException;
	
	public boolean memberExists(@NotNull String memberName) throws ZosDatasetException;
	
	public void memberStore(@NotNull String memberName, @NotNull String content) throws ZosDatasetException;
	
	public String memberRetrieve(@NotNull String memberName) throws ZosDatasetException;

	public Collection<String> memberList() throws ZosDatasetException;
	
	public void setDataType(DatasetDataType dataType);

	public void setVolumes(String volumes);

	public void setUnit(String unit);

	public void setDatasetOrganization(DatasetOrganization organization);

	/**
	 * Set the {@link SpaceUnit} for this datset, and specify how many 
	 * primary and secondary extents to allocate.
	 * 
	 * @param spaceUnit
	 * @param primaryExtent
	 * @param secondaryExtents
	 */
	public void setSpace(SpaceUnit spaceUnit, int primaryExtents, int secondaryExtents);

	public void setDirectoryBlocks(int directoryBlocks);

	/**
	 * Set the {@link RecordFormat} for this data set
	 * 
	 * @param recordFormat
	 */
	public void setRecordFormat(RecordFormat recordFormat);

	/**
	 * Set the block size for this data set
	 * 
	 * @param blockSize
	 */
	public void setBlockSize(int blockSize);

	/**
	 * Set the record length for this data set
	 * 
	 * @param recordlength
	 */
	public void setRecordlength(int recordlength);
	
	public void setManagementClass(String managementClass);
	public void setStorageClass(String storageClass);
	public void setDataClass(String dataClass);

	/**
	 * Set the {@link DSType} for this data set
	 * 
	 * @param dsType
	 */
	public void setDatasetType(DSType dsType);

	
	/**
	 * Get the name of this data set
	 * 
	 * @return
	 */
	public String getDatasetName();
	
	public DatasetDataType getDataType();
	
	public String getVolumes();

	public String getUnit();

	public String getDatasetOrganization();

	public String getSpaceUnit();

	public int getPrimaryExtents();

	public int getSecondaryExtents();

	public int getDirectoryBlocks();

	public String getRecordFormat();

	public int getBlockSize();

	public int getRecordlength();

	public String getManagementClass();

	public String getStorageClass();

	public String getDataClass();

	/**
	 * Get the {@link DSType} for this data set
	 * 
	 * @return
	 */
	public DSType getDatasetType();
	
	public String getDatasetAttibutesString() throws ZosDatasetException;
}
