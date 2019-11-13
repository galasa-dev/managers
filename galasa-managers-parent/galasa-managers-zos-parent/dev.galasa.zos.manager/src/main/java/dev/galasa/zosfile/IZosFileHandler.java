/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile;

import dev.galasa.zos.IZosImage;

/**
 * <p>
 * Tester facing interface for zOS file management.
 * </p>
 * 
 * <p>
 * Provides 3 types of files:
 * <ul>
 * <li>IUNIXFile</li>
 * <li>IDataset</li>
 * <li>IVSAMDataset</li>
 * </ul>
 * Typical usage would be to instantiate one of these using the newXXX(...)
 * methods (e.g.: {@link #newDataset(String)}), then to call methods on that
 * object to configure it, and then to call methods on this manager to
 * manipulate it.
 * </p>
 * 
 */
public interface IZosFileHandler {

	/**
	 * Instantiate a new {@link IZosDataset}, which can represent either an
	 * existing dataset, or one to be created.  Member name will be ignored
	 * 
	 * @param dsname
	 * @param image
	 * @return
	 * @throws ZosDatasetException 
	 */
	public IZosDataset newDataset(String dsname, IZosImage image) throws ZosDatasetException;

	/**
	 * Instantiate a new {@link IZosUNIXFile}, which can represent either an
	 * existing UNIX file, or directory, or one to be created. <br />
	 * <br />
	 * N.B. If a directory is to be represented, fullFilePath must end with a
	 * "/"
	 * 
	 * @param fullFilePath
	 * @return
	 * @throws ZosUNIXFileException 
	 */
	public IZosUNIXFile newUNIXFile(String fullFilePath, IZosImage image) throws ZosUNIXFileException;

	/**
	 * Instantiate a new {@link IZosVSAMDataset} object with the given name. The
	 * object returned is a 'blank-slate' and will require configuring before it
	 * can be defined or stored. See {@link #newESDS(String)},
	 * {@link #newKSDS(String)} or {@link #newRRDS(String)} for pre-configured
	 * VSAM objects.
	 * 
	 * @param dsname
	 * @return
	 * @throws ZosVSAMDatasetException 
	 */
	public IZosVSAMDataset newVSAMDataset(String dsname, IZosImage image) throws ZosVSAMDatasetException;
}
