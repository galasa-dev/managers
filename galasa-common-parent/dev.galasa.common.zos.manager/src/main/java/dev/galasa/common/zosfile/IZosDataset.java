package dev.galasa.common.zosfile;

/**
 * Representation of a non-VSAM dataset
 * 
 * @author James Bartlett
 *
 */
public interface IZosDataset {
	
	/**
	 * Enumeration of dataset types
	 *
	 */
	public enum DSType {
		
		LIBRARY(true),
		HFS,
		PDS(true),
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
		
		public boolean isPDS() {
			return pds;
		}
	}
	
	/**
	 * Enumeration of record format options
	 *
	 */
	public enum RecordFormat {
		
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
	 * Enumeration of space units for dataset allocation
	 *
	 */
	public enum SpaceUnit {
		
		BLOCKS("BLKS"),
		TRACKS("TRKS"),
		CYLINDERS("CYLS"),
		KILOBYTES("KB"),
		MEGABYTES("MB"),
		BYTES("BYTES"),
		RECORDS("RECORDS");
		
		private String unit;
		
		SpaceUnit(String unit) {
			this.unit = unit;
		}
		
		@Override
		public String toString() {
			return unit;
		}		
	}
	
	/**
	 * Set the name of the member to address if this is a PDS.
	 * Changing this value allows you to address different members
	 * within a PDS for store, retrieve and delete operations
	 * 
	 * @param member
	 */
	public void setMemberName(String member);
	
	/**
	 * Get the member name currently addressed by this object, or
	 * null if this is not a PDS or does not currently address
	 * a member
	 * 
	 * @return
	 */
	public String getMemberName();
	
	/**
	 * Get the name of this dataset
	 * 
	 * @return
	 */
	public String getDatasetName();
	
	/**
	 * Get the string used as the Site command to define
	 * the allocation of this dataset
	 * 
	 * @return
	 */
	public String getDatasetFormatString();
	
	/**
	 * Set the {@link RecordFormat} for this dataset
	 * 
	 * @param recordFormat
	 */
	public void setRecordFormat(RecordFormat recordFormat);

	/**
	 * Set the record length for this dataset
	 * 
	 * @param recordlength
	 */
	public void setRecordlength(int recordlength);

	/**
	 * Set the block size for this dataset
	 * 
	 * @param blockSize
	 */
	public void setBlockSize(int blockSize);

	/**
	 * Set the {@link SpaceUnit} for this datset, and specify how many 
	 * primary and secondary extents to allocate.
	 * 
	 * @param spaceUnit
	 * @param primaryExtent
	 * @param secondaryExtents
	 */
	public void setSpace(SpaceUnit spaceUnit, int primaryExtent, int secondaryExtents);
	
	/**
	 * Set the {@link DSType} for this dataset
	 * 
	 * @param dsType
	 */
	public void setDatasetType(DSType dsType);
	
	/**
	 * Get the {@link DSType} for this dataset
	 * 
	 * @return
	 */
	public DSType getDatasetType();
}
