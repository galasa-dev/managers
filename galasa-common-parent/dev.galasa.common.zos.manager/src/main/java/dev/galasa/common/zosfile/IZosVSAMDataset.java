package dev.galasa.common.zosfile;

import java.io.InputStream;


/**
 * Highly configurable representation of a VSAM dataset
 * 
 * @author James Bartlett
 *
 */
public interface IZosVSAMDataset {
	
	/**
	 * Backup-While-Open options for VSAM define
	 *
	 */
	public enum BWOOption {
		
		TYPECICS,
		TYPEIMS,
		NO;
	}
	
	/**
	 * Erase options for VSAM define
	 *
	 */
	public enum EraseOption {
		
		ERASE,
		NOERASE;
	}
	
	/**
	 * VSAM Dataset Organisation options
	 *
	 */
	public enum DatasetOrganisation {
		
		INDEXED,
		LINEAR,
		NONINDEXED,
		NUMBERED;
	}
	
	/**
	 * FR Log options for VSAM define
	 *
	 */
	public enum FRLogOption {
		
		NONE,
		REDO;
	}

	/**
	 * Log options for VSAM define
	 *
	 */
	public enum LogOption {
		
		NONE,
		UNDO,
		ALL;
	}

	/**
	 * Recatalog options for VSAM define
	 *
	 */
	public enum RecatalogOption {
		
		RECATALOG,
		NORECATALOG;
	}

	/**
	 * Reuse options for VSAM define
	 *
	 */
	public enum ReuseOption {
		
		REUSE,
		NOREUSE;
	}

	/**
	 * Space units for VSAM define
	 *
	 */
	public enum VSAMSpaceUnit {
		
		TRACKS,
		CYLINDERS,
		KILOBYTES,
		MEGABYTES,
		RECORDS;	
	}

	/**
	 * Spanned options for VSAM define
	 *
	 */
	public enum SpanOption {
		
		SPANNED,
		NONSPANNED;
	}

	/**
	 * Speed or Recovery options for VSAM define
	 *
	 */
	public enum SpeedRecoveryOption {
		
		SPEED,
		RECOVERY;
	}

	/**
	 * Write check options for VSAM define
	 *
	 */
	public enum WriteCheckOption {
		
		WRITECHECK,
		NOWRITECHECK;
	}
	
	/**
	 * Get the name of the VSAM file
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Set the {@link VSAMSpaceUnit} for the VSAM file, and the number of
	 * primary and secondary extents required.
	 * 
	 * @param spaceUnit
	 * @param primaryExtents
	 * @param secondaryExtents
	 */
	public void setSpace(VSAMSpaceUnit spaceUnit, int primaryExtents, int secondaryExtents);

	/**
	 * Set the volume(s) to be used in allocating this VSAM file
	 * 
	 * @param volumes
	 */
	public void setVolumes(String volumes);

	/**
	 * Set accounting info for the VSAM file
	 * 
	 * @param accountInfo
	 */
	public void setAccountInfo(String accountInfo);

	/**
	 * Set the bufferspace for the VSAM file
	 * 
	 * @param bufferspace
	 */
	public void setBufferspace(long bufferspace);

	/**
	 * Set the Backup-While-Open option for the VSAM file
	 * 
	 * @param bwoOption
	 */
	public void setBwoOption(BWOOption bwoOption);

	/**
	 * Set the control interval for the VSAM file
	 * 
	 * @param controlInterval
	 */
	public void setControlInterval(String controlInterval);

	/**
	 * Set the DataClass for the VSAM file
	 * 
	 * @param dataclass
	 */
	public void setDataclass(String dataclass);

	/**
	 * Set the erase option for the VSAM file
	 * 
	 * @param eraseOption
	 */
	public void setEraseOption(EraseOption eraseOption);

	/**
	 * Set an exception exit for the VSAM file
	 * 
	 * @param exceptionExit
	 */
	public void setExceptionExit(String exceptionExit);

	/**
	 * Set the control interval and control area free space percentages for the VSAM file
	 * 
	 * @param controlIntervalPercent
	 * @param controlAreaPercent
	 */
	public void setFreeSpaceOptions(int controlIntervalPercent, int controlAreaPercent);

	/**
	 * Set the {@link FRLogOption} for the VSAM file
	 * 
	 * @param frlogOption
	 */
	public void setFrlogOption(FRLogOption frlogOption);

	/**
	 * Set the {@link DatasetOrganisation} for the VSAM file
	 * 
	 * @param dataOrg
	 */
	public void setDatasetOrg(DatasetOrganisation dataOrg);
	
	/**
	 * Set the key length and offset for the VSAM file
	 * 
	 * @param length
	 * @param offset
	 */
	public void setKeyOptions(int length, int offset);

	/**
	 * Set the {@link LogOption} for the VSAM file
	 * 
	 * @param logOption
	 */
	public void setLogOption(LogOption logOption);

	/**
	 * Set the logstream id for the VSAM file
	 * @param logStreamID
	 */
	public void setLogStreamID(String logStreamID);

	/**
	 * Set the management class for the VSAM file
	 * 
	 * @param managementClass
	 */
	public void setManagementClass(String managementClass);

	/**
	 * Set the entry name and catalog name for the model for the VSAM file.
	 * Catalog may be null, or both entry and catalog may be null.
	 * 
	 * @param modelEntryName
	 * @param modelCatName
	 */
	public void setModel(String modelEntryName, String modelCatName);

	/**
	 * Set the owner of the VSAM file
	 * 
	 * @param owner
	 */
	public void setOwner(String owner);

	/**
	 * Set the {@link RecatalogOption} for the VSAM file
	 * 
	 * @param recatalogOption
	 */
	public void setRecatalogOption(RecatalogOption recatalogOption);

	/**
	 * Set the average and maximum record sizes for the VSAM file
	 * 
	 * @param average
	 * @param max
	 */
	public void setRecordSize(int average, int max);

	/**
	 * Set the {@link ReuseOption} for the VSAM file
	 * 
	 * @param reuseOption
	 */
	public void setReuseOption(ReuseOption reuseOption);

	/**
	 * Set cross-region and cross-system share options for the VSAM file
	 * 
	 * @param crossRegion
	 * @param crossSystem
	 */
	public void setShareOptions(int crossRegion, int crossSystem);

	/**
	 * Set the {@link SpanOption} for the VSAM file
	 * 
	 * @param spanOption
	 */
	public void setSpanOption(SpanOption spanOption);

	/**
	 * Set the {@link SpeedRecoveryOption} for the VSAM file
	 * 
	 * @param speedRecoveryOption
	 */
	public void setSpeedRecoveryOption(SpeedRecoveryOption speedRecoveryOption);

	/**
	 * Set the storage class for the VSAM file
	 * 
	 * @param storageClass
	 */
	public void setStorageClass(String storageClass);

	/**
	 * Set the {@link WriteCheckOption} for the VSAM file
	 * 
	 * @param writeCheckOption
	 */
	public void setWriteCheckOption(WriteCheckOption writeCheckOption);

	/**
	 * Indicate whether a DATA file should be added to the VSAM cluster,
	 * and whether it should be unique
	 * 
	 * @param useDATA
	 * @param unique
	 */
	public void setUseDATA(boolean useDATA, boolean unique);

	/**
	 * Set the name for the DATA file
	 * 
	 * @param dataName
	 */
	public void setDataName(String dataName);
	
	/**
	 * Set the {@link VSAMSpaceUnit} for the DATA file, and the number of
	 * primary and secondary extents required.
	 * 
	 * @param spaceUnit
	 * @param primaryExtents
	 * @param secondaryExtents
	 */
	public void setDataSpace(VSAMSpaceUnit spaceunit, int primaryExtents, int secondaryExtents);

	/**
	 * Set the volume(s) to be used for allocation of the DATA file
	 * 
	 * @param dataVolumes
	 */
	public void setDataVolumes(String dataVolumes);

	/**
	 * Set the buffer space for the DATA file
	 * 
	 * @param dataBufferspace
	 */
	public void setDataBufferspace(long dataBufferspace);

	/**
	 * Set the control interval for the DATA file
	 * 
	 * @param dataControlInterval
	 */
	public void setDataControlInterval(String dataControlInterval);

	/** 
	 * Set the {@link EraseOption} for the DATA file
	 * 
	 * @param dataEraseOption
	 */
	public void setDataEraseOption(EraseOption dataEraseOption);

	/**
	 * Set an exception exit for the DATA file
	 * 
	 * @param dataExceptionExit
	 */
	public void setDataExceptionExit(String dataExceptionExit);

	/**
	 * Set the control interval and control area free space percentages for the DATA file	 * 
	 * 
	 * @param controlIntervalPercent
	 * @param controlAreaPercent
	 */
	public void setDataFreeSpaceOptions(int controlIntervalPercent, int controlAreaPercent);
	
	/**
	 * Set the key length and offset for the DATA file
	 * 
	 * @param length
	 * @param offset
	 */
	public void setDataKeyOptions(int length, int offset);

	/**
	 * Set the entry name and catalog name for the model for the DATA file.
	 * Catalog may be null, or both entry and catalog may be null.
	 * 
	 * @param modelEntryName
	 * @param modelCatName
	 */
	public void setDataModel(String modelEntryName, String modelCatName);

	/**
	 * Set the owner for the DATA file
	 * 
	 * @param dataOwner
	 */
	public void setDataOwner(String dataOwner);

	/**
	 * Set the average and maximum record sizes for the DATA file
	 * 
	 * @param average
	 * @param max
	 */
	public void setDataRecordSize(int average, int max);

	/**
	 * Set the {@link ReuseOption} for the DATA file
	 * 
	 * @param dataReuseOption
	 */
	public void setDataReuseOption(ReuseOption dataReuseOption);

	/**
	 * Set the cross-region and cross-system share options for the DATA file
	 * 
	 * @param crossRegion
	 * @param crossSystem
	 */
	public void setDataShareOptions(int crossRegion, int crossSystem);

	/**
	 * Set the {@link SpanOption} for the DATA file
	 * 
	 * @param dataSpanOption
	 */
	public void setDataSpanOption(SpanOption dataSpanOption);

	/**
	 * Set the {@link SpeedRecoveryOption} for the DATA file
	 * 
	 * @param dataSpeedRecoveryOption
	 */
	public void setDataSpeedRecoveryOption(SpeedRecoveryOption dataSpeedRecoveryOption);

	/**
	 * Set the {@link WriteCheckOption} for the DATA file
	 * 
	 * @param dataWriteCheckOption
	 */
	public void setDataWriteCheckOption(WriteCheckOption dataWriteCheckOption);

	/**
	 * Indicate whether an INDEX file should be allocated in the cluster, 
	 * and wheth it should be unique
	 * 
	 * @param useINDEX
	 * @param unique
	 */
	public void setUseINDEX(boolean useINDEX, boolean unique);

	/**
	 * Set the name of the INDEX file
	 * 
	 * @param indexName
	 */
	public void setIndexName(String indexName);

	/**
	 * Set the {@link VSAMSpaceUnit} for the INDEX file, and the number of
	 * primary and secondary extents required.
	 * 
	 * @param spacetype
	 * @param primaryExtents
	 * @param secondaryExtents
	 */
	public void setIndexSpace(VSAMSpaceUnit spacetype, int primaryExtents, int secondaryExtents);

	/**
	 * Set the volume(s) on which to allocate the INDEX file
	 * 
	 * @param indexVolumes
	 */
	public void setIndexVolumes(String indexVolumes);

	/**
	 * Set the control interval for the INDEX file
	 * 
	 * @param indexControlInterval
	 */
	public void setIndexControlInterval(String indexControlInterval);

	/**
	 * Set an exception exit for the INDEX file
	 * 
	 * @param indexExceptionExit
	 */
	public void setIndexExceptionExit(String indexExceptionExit);

	/**
	 * Set the entry name and catalog name for the model for the INDEX file.
	 * Catalog may be null, or both entry and catalog may be null.
	 * 
	 * @param indexModelEntryName
	 * @param indexModelCatName
	 */
	public void setIndexModel(String indexModelEntryName, String indexModelCatName);

	/**
	 * Set the owner of the INDEX file
	 * 
	 * @param indexOwner
	 */
	public void setIndexOwner(String indexOwner);

	/**
	 * Set the {@link ReuseOption} for the INDEX file
	 * 
	 * @param indexReuseOption
	 */
	public void setIndexReuseOption(ReuseOption indexReuseOption);

	/**
	 * Set the cross-region and cross-system share options for the INDEX file
	 * 
	 * @param crossRegion
	 * @param crossSystem
	 */
	public void setIndexShareOptions(int crossRegion, int crossSystem);

	/**
	 * Set the {@link WriteCheckOption} for the INDEX file
	 * 
	 * @param indexWriteCheckOption
	 */
	public void setIndexWriteCheckOption(WriteCheckOption indexWriteCheckOption);

	/**
	 * Set a catalog for the VSAM cluster
	 * 
	 * @param catalog
	 */
	public void setCatalog(String catalog);
	
	/**
	 * Get the IDCAMS define command for this VSAM cluster
	 * 
	 * @return
	 * @throws ZosFileException
	 */
	public String getDefineCommand() throws ZosFileException;
	
	/**
	 * Get the IDCAMS delete command for this VSAM cluster
	 * 
	 * @return
	 * @throws ZosFileException
	 */
	public String getDeleteCommand() throws ZosFileException;

	/**
	 * Get the IDCAMS repro command for inputting data from an infile
	 * 
	 * @param infile
	 * @return
	 * @throws ZosFileException
	 */
	public String getReproCommand(String infile) throws ZosFileException;

	/**
	 * Set the content to be stored in the VSAM file
	 * 
	 * @param content
	 */
	public void setContent(String content);
	
	/**
	 * Set the content to be stored in the VSAM file
	 * 
	 * @param input
	 */
	public void setContent(InputStream input);

	/**
	 * Append content to be stored in the VSAM file
	 * 
	 * @param content
	 */
	public void appendContent(String content);
	
	/**
	 * Append content to be stored in the VSAM file
	 * 
	 * @param input
	 */
	public void appendContent(InputStream input);
}
