package dev.galasa.zosfile;

import java.util.Collection;

import javax.validation.constraints.NotNull;

/**
 * Representation of a non-VSAM data set
 */
public interface IZosDataset {
	
	/**
	 * Enumeration of data set types:
	 * <li>{@link #HFS}</li>
	 * <li>{@link #PDS}</li>
	 * <li>{@link #PDSE}</li>
	 * <li>{@link #LARGE}</li>
	 * <li>{@link #BASIC}</li>
	 * <li>{@link #EXTREQ}</li>
	 * <li>{@link #EXTPREF}</li>
	 */
	public enum DSType {

		HFS,
		PDS,
		PDSE,
		LARGE,
		BASIC,
		EXTREQ,
		EXTPREF;
	}
	
	/**
	 * Enumeration of record format options:
	 * <li>{@link #BLOCK}</li>
	 * <li>{@link #FIXED}</li>
	 * <li>{@link #FIXED_BLOCKED}</li>
	 * <li>{@link #VARIABLE}</li>
	 * <li>{@link #VARIABLE_BLOCKED}</li>
	 * <li>{@link #UNDEFINED}</li>
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
	 * Enumeration of data set organization options:
	 * <li>{@link #PARTITIONED}</li>
	 * <li>{@link #SEQUENTIAL}</li>
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
	 * Enumeration of space units for data set allocation:
	 * <li>{@link #TRACKS}</li>
	 * <li>{@link #CYLINDERS}</li>
	 */
	public enum SpaceUnit {
		/**
		 * Allocate data set in tracks
		 */
		TRACKS("TRK"),
		/**
		 * Allocate data set in cylinders
		 */
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
	 * Enumeration of data type for store and retrieve of data set content:
	 * <li>{@link #TEXT}</li>
	 * <li>{@link #BINARY}</li>
	 * <li>{@link #RECORD}</li>
	 */
	public enum DatasetDataType {
		/**
		 * Content is between ISO8859-1 on the client and EBCDIC on the host
		 */
		TEXT("text"),
		/**
		 * No data conversion is performed
		 */
		BINARY("binary"),
		/**
		 * No data conversion is performed. Each logical record is preceded by the 4-byte record length of the record that follows
		 */
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
	 * Delete the data set on zOS image.
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
	 * Write to content to the data set 
	 * @param content
	 * @param dataType
	 * @throws ZosDatasetException
	 */
	public void store(@NotNull String content) throws ZosDatasetException;
	
	/**
	 * Retrieve content of the data set
	 * @return data set content
	 * @throws ZosDatasetException
	 */
	public String retrieve() throws ZosDatasetException;

	/**
	 * Store the content of the data set with the test output
	 * @throws ZosDatasetException
	 */
	public void saveToTestArchive() throws ZosDatasetException;
	
	/**
	 * Returns true if the data set exists and is a partitioned data set
	 * @return
	 * @throws ZosDatasetException
	 */
	public boolean isPDS() throws ZosDatasetException;

	/**
	 * Create an empty member in the partitioned data set
	 * @param memberName
	 * @throws ZosDatasetException
	 */
	public void memberCreate(@NotNull String memberName) throws ZosDatasetException;

	/**
	 * Delete a member from the partitioned data set
	 * @param memberName
	 * @throws ZosDatasetException
	 */
	public void memberDelete(@NotNull String memberName) throws ZosDatasetException;
	
	/**
	 * Return true if the named member exists in the partitioned data set
	 * @param memberName
	 * @return
	 * @throws ZosDatasetException
	 */
	public boolean memberExists(@NotNull String memberName) throws ZosDatasetException;
	
	/**
	 * Write content to the partitioned data set member
	 * @param memberName
	 * @param content
	 * @throws ZosDatasetException
	 */
	public void memberStore(@NotNull String memberName, @NotNull String content) throws ZosDatasetException;
	
	/**
	 * Retrieve content from the partitioned data set member
	 * @param memberName
	 * @return
	 * @throws ZosDatasetException
	 */
	public String memberRetrieve(@NotNull String memberName) throws ZosDatasetException;

	/**
	 * List the members of the partitioned data set
	 * @return
	 * @throws ZosDatasetException
	 */
	public Collection<String> memberList() throws ZosDatasetException;

	/**
	 * Store the content of the partitioned data set member with the test output
	 * @throws ZosDatasetException
	 */
	public void memberSaveToTestArchive(@NotNull String memberName) throws ZosDatasetException;
	
	/**
	 * Set the data type ({@link DatasetDataType}) for store and retrieve of the data set content
	 * @param dataType
	 */
	public void setDataType(DatasetDataType dataType);

	/**
	 * Set the 	Volume serial(s) of the data set
	 * @param volumes
	 */
	public void setVolumes(String volumes);

	/**
	 * Set the unit name of the data set
	 * @param unit
	 */
	public void setUnit(String unit);

	/**
	 * Set the organization ({@link DatasetOrganization}) of the data set
	 * @param organization
	 */
	public void setDatasetOrganization(DatasetOrganization organization);

	/**
	 * Set the {@link SpaceUnit} for data set, and specify how many 
	 * primary and secondary extents to allocate.
	 * 
	 * @param spaceUnit
	 * @param primaryExtent
	 * @param secondaryExtents
	 */
	public void setSpace(SpaceUnit spaceUnit, int primaryExtents, int secondaryExtents);

	/**
	 * Set the number of directory blocks
	 * @param directoryBlocks
	 */
	public void setDirectoryBlocks(int directoryBlocks);

	/**
	 * Set the {@link RecordFormat} for the data set
	 * 
	 * @param recordFormat
	 */
	public void setRecordFormat(RecordFormat recordFormat);

	/**
	 * Set the block size for the data set
	 * 
	 * @param blockSize
	 */
	public void setBlockSize(int blockSize);

	/**
	 * Set the record length for the data set
	 * 
	 * @param recordlength
	 */
	public void setRecordlength(int recordlength);
	
	/**
	 * Set the management class of the data set
	 * @param managementClass
	 */
	public void setManagementClass(String managementClass);
	
	/**
	 * Set the storage class of the data set
	 * @param managementClass
	 */
	public void setStorageClass(String storageClass);
	
	/**
	 * Set the data class of the the data set
	 * @param managementClass
	 */
	public void setDataClass(String dataClass);

	/**
	 * Set the {@link DSType} for the data set
	 * 
	 * @param dsType
	 */
	public void setDatasetType(DSType dsType);
	
	/**
	 * Return the name of the data set 
	 * @return
	 */
	public String getDatasetName();
	
	/**
	 * Return the data type ({@link DatasetDataType}) for store and retrieve of the data set content
	 * @return
	 */
	public DatasetDataType getDataType();
	
	/**
	 * Return the Volume serial(s) of the data set
	 * @return
	 */
	public String getVolumes();

	/**
	 * Return the unit name of the data set
	 * @return
	 */
	public String getUnit();

	/**
	 * Return the organization ({@link DatasetOrganization}) of the data set
	 * @return
	 */
	public String getDatasetOrganization();

	/**
	 * Return the allocation space unit of the data set
	 * @return
	 */
	public String getSpaceUnit();

	/**
	 * Return primary allocation extents of the data set
	 * @return
	 */
	public int getPrimaryExtents();

	/**
	 * Return secondary allocation extents of the data set
	 * @return
	 */
	public int getSecondaryExtents();

	/**
	 * Return directory blocks of the partitioned data set
	 * @return
	 */
	public int getDirectoryBlocks();

	/**
	 * Return the record format of the data set
	 * @return
	 */
	public String getRecordFormat();

	/**
	 * Return block size of the data set
	 * @return
	 */
	public int getBlockSize();

	/**
	 * Return the record length of the data set
	 * @return
	 */
	public int getRecordlength();

	/**
	 * Return the management class of the data set
	 * @return
	 */
	public String getManagementClass();

	/**
	 * Return the storage class of the data set
	 * @return
	 */
	public String getStorageClass();

	/**
	 * Return the data class of the data set
	 * @return
	 */
	public String getDataClass();

	/**
	 * Get the {@link DSType} for the data set
	 * 
	 * @return
	 */
	public DSType getDatasetType();
	
	/**
	 * Return the attributes of the data set as a {@link String} 
	 * @return
	 */
	public String getDatasetAttibutesString() throws ZosDatasetException;
}
