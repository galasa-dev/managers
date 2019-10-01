package dev.galasa.zosfile;

import java.util.Map;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosDataset.DSType;

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
 * 
 * @author James Bartlett
 * 
 */
public interface IZosFile {

	/**
	 * Instantiate a new {@link IZosUNIXFile}, which can represent either an
	 * existing UNIX file, or directory, or one to be created. <br />
	 * <br />
	 * N.B. If a directory is to be represented, fullFilePath must end with a
	 * "/"
	 * 
	 * @param fullFilePath
	 * @return
	 */
	public IZosUNIXFile newUNIXFile(String fullFilePath);

	/**
	 * Store an {@link IZosUNIXFile} as a file on the given zOS image, creating the file
	 * and all required parent directories as required. The content stored is
	 * that added to the unixFile using the {@link IZosUNIXFile#setContent(String)}
	 * or {@link IZosUNIXFile#appendContent(String)} methods.
	 * 
	 * @param unixFile
	 * @param image
	 * @throws ZosFileException
	 */
	public void store(IZosUNIXFile unixFile, IZosImage image) throws ZosFileException;

	/**
	 * Retrieve the content of UNIX file to an {@link IZosUNIXFile} from the given zOS
	 * image. The content can then be obtained using the
	 * {@link IZosUNIXFile#getContent()} method.
	 * 
	 * @param unixFile
	 * @param image
	 * @throws ZosFileException
	 */
	public void retrieve(IZosUNIXFile unixFile, IZosImage image) throws ZosFileException;

	/**
	 * Delete the UNIX file corresponding to an {@link IZosUNIXFile} from the given zOS image.
	 * 
	 * @param unixFile
	 * @param image
	 * @throws ZosFileException
	 */
	public void delete(IZosUNIXFile unixFile, IZosImage image) throws ZosFileException;

	/**
	 * Recursively delete a directory and its contents from the given zOS image.
	 * 
	 * @param directory
	 * @param image
	 * @throws ZosFileException
	 */
	public void deleteDirectory(String directory, IZosImage image) throws ZosFileException;

	/**
	 * Create a directory and all required parent directories on the given zOS image.
	 * 
	 * @param directory
	 * @param image
	 * @throws ZosFileException
	 */
	public void createDirectory(String directory, IZosImage image) throws ZosFileException;

	/**
	 * Return true if the passed {@link IZosUNIXFile} exists on the given zOS image.
	 * 
	 * @param unixFile
	 * @param image
	 * @return
	 * @throws ZosFileException
	 */
	public boolean exists(IZosUNIXFile unixFile, IZosImage image) throws ZosFileException;

	/**
	 * Instantiate a new {@link IZosDataset}, which can represent either an
	 * existing dataset, or one to be created.
	 * 
	 * @param dsName
	 * @return
	 */
	public IZosDataset newDataset(String dsName);

	/**
	 * Store an {@link IZosDataset} as a dataset on the given zOS image. The content
	 * stored is that added to the dataset using the
	 * {@link IZosDataset#setContent(String)} or
	 * {@link IZosDataset#appendContent(String)} methods.
	 * 
	 * @param dataset
	 * @param image
	 * @throws ZosFileException
	 */
	public void store(IZosDataset dataset, IZosImage image) throws ZosFileException;

	/**
	 * Retrieve the content of a dataset to an {@link IZosDataset} from the given
	 * zOS image. The content can then be obtained using the
	 * {@link IZosDataset#getContent()} method.
	 * 
	 * @param dataset
	 * @param image
	 * @throws ZosFileException
	 */
	public void retrieve(IZosDataset dataset, IZosImage image) throws ZosFileException;

	/**
	 * Delete the dataset corresponding to an {@link IZosDataset} from the given
	 * zOS image.
	 * 
	 * @param dataset
	 * @param image
	 * @throws ZosFileException
	 */
	public void delete(IZosDataset dataset, IZosImage image) throws ZosFileException;

	/**
	 * Delete all datasets (including VSAM datasets) with the given HLQ from the
	 * given zOS image
	 * 
	 * @param prefix
	 * @param image
	 * @throws ZosFileException
	 */
	public void deleteDatasetsByPrefix(String prefix, IZosImage image) throws ZosFileException;

	/**
	 * Return true if the given {@link IZosDataset} exists on the given zOS image.
	 * 
	 * @param dataset
	 * @param image
	 * @return
	 * @throws ZosFileException
	 */
	public boolean exists(IZosDataset dataset, IZosImage image) throws ZosFileException;

	/**
	 * APF authorise the given {@link IZosDataset} on the given zOS image.
	 * 
	 * @param dataset
	 * @param image
	 * @throws ZosFileException
	 */
	public void apfAuthorise(IZosDataset dataset, IZosImage image) throws ZosFileException;

	/**
	 * Return true if the given {@link IZosDataset} is found in the APF-Auth list
	 * for the given zOS image.
	 * 
	 * @param dataset
	 * @param image
	 * @return
	 * @throws ZosFileException
	 */
	public boolean isAPFAuthorised(IZosDataset dataset, IZosImage image) throws ZosFileException;

	/**
	 * Instantiate a new {@link IZosVSAMDataset} object with the given name. The
	 * object returned is a 'blank-slate' and will require configuring before it
	 * can be defined or stored. See {@link #newESDS(String)},
	 * {@link #newKSDS(String)} or {@link #newRRDS(String)} for pre-configured
	 * VSAM objects.
	 * 
	 * @param dsName
	 * @return
	 */
	public IZosVSAMDataset newVSAMDataset(String dsName);

	/**
	 * Instantiate a new {@link IZosVSAMDataset} object, pre-configured with
	 * options appropriate for a KSDS.
	 * 
	 * @param dsName
	 * @return
	 */
	public IZosVSAMDataset newKSDS(String dsName);

	/**
	 * Instantiate a new {@link IZosVSAMDataset} object, pre-configured with
	 * options appropriate for a ESDS.
	 * 
	 * @param dsName
	 * @return
	 */
	public IZosVSAMDataset newESDS(String dsName);

	/**
	 * Instantiate a new {@link IZosVSAMDataset} object, pre-configured with
	 * options appropriate for a RRDS.
	 * 
	 * @param dsName
	 * @return
	 */
	public IZosVSAMDataset newRRDS(String dsName);

	/**
	 * Define an {@link IZosVSAMDataset} on the given zOS image.
	 * 
	 * @param vsam
	 * @param image
	 * @throws ZosFileException
	 */
	public void define(IZosVSAMDataset vsam, IZosImage image) throws ZosFileException;

	/**
	 * Store an {@link IZosVSAMDataset} as a vsam on the given zOS image. The content
	 * stored is that added to the vsam using the
	 * {@link IZosVSAMDataset#setContent(String)} or
	 * {@link IZosVSAMDataset#appendContent(String)} methods.
	 * 
	 * @param vsam
	 * @param image
	 * @throws ZosFileException
	 */
	public void store(IZosVSAMDataset vsam, IZosImage image) throws ZosFileException;

	/**
	 * Delete the VSAM corresponding to an {@link IZosVSAMDataset} from the given
	 * zOS image.
	 * 
	 * @param vsam
	 * @param image
	 * @throws ZosFileException
	 */
	public void delete(IZosVSAMDataset vsam, IZosImage image) throws ZosFileException;

	/**
	 * Return true if the given {@link IZosVSAMDataset} exists on the given zOS image.
	 * 
	 * @param vsam
	 * @param image
	 * @return
	 * @throws ZosFileException
	 */
	public boolean exists(IZosVSAMDataset vsam, IZosImage image) throws ZosFileException;

	/**
	 * This method retrieves an artifact from the 'resources' folder in a jat
	 * bundle (test or infrastructure), performs substitutions using the '++'
	 * skeleton processor to generate the content which it will then store as a
	 * UNIX file using the transfer method appropriate to the filetype. See the
	 * description of the arguments for more information.
	 * 
	 * @param resourcePath
	 *            - the path to the resource in the bundle, specified relative
	 *            to 'resources'
	 * @param unixPath
	 *            - the absolute path to the target file in UNIX (need not exist)
	 * @param fileType
	 *            - {@link IZosUNIXFile#TEXT} or {@link IZosUNIXFile#BIN}
	 * @param image
	 *            - the zOS image for the target file
	 * @param substitutionParameters
	 *            - a map of key value pairs to be substituted into the stored
	 *            content
	 * @param owningClass
	 *            - any class in the bundle containing the resource to store.
	 *            'this.getClass()' is generally the safest way to specify.
	 * @return
	 * @throws ZosFileException
	 */
	public IZosUNIXFile storeResourcesFile(String resourcePath, String unixPath,
			int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass)
			throws ZosFileException;

	/**
	 * This method retrieves an artifact from the 'resources' folder in a jat
	 * bundle (test or infrastructure), performs substitutions using the '++'
	 * skeleton processor to generate the content which it will then store as a
	 * dataset using the transfer method appropriate to the filetype. See the
	 * description of the arguments for more information.
	 * 
	 * @param resourcePath
	 *            - the path to the resource in the bundle, specified relative
	 *            to 'resources'
	 * @param dsName
	 *            - the name of the target dataset (need not exist)
	 * @param fileType
	 *            - {@link IZosDataset#TEXT} or {@link IZosDataset#BIN}
	 * @param image
	 *            - the zOS image for the target dataset
	 * @param substitutionParameters
	 *            - a map of key value pairs to be substituted into the stored
	 *            content
	 * @param owningClass
	 *            - any class in the bundle containing the resource to store.
	 *            'this.getClass()' is generally the safest way to specify.
	 * @return
	 * @throws ZosFileException
	 */
	public IZosDataset storeResourcesDataset(String resourcePath, String dsName,
			int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass)
			throws ZosFileException;

	/**
	 * This method retrieves all contents of a given directory from the
	 * 'resources' folder in a jat bundle (test or infrastructure). It will
	 * perform substitutions using the '++' skeleton processor on each file,
	 * then write those files to a tar archive, re-encoding as necessary. The
	 * archive is then transferred to UNIX and extracted in the directory
	 * requested. <br />
	 * <br />
	 * N.B. Contents are taken from the resourcePath directory, and put into the
	 * unixPath directory. So if resourcePath = "bundle/dir/" and unixPath =
	 * "/unix/target/", then "/resources/bundle/dir/file.txt" will end up at
	 * "/unix/target/file.txt".
	 * 
	 * @param resourcePath
	 *            - the path to the directory in the bundle, specified relative
	 *            to 'resources'
	 * @param unixPath
	 *            - the name of the target directory (need not exist)
	 * @param fileType
	 *            - {@link IZosUNIXFile#TEXT} or {@link IZosUNIXFile#BIN}
	 * @param image
	 *            - the zOS image for the target directory
	 * @param substitutionParameters
	 *            - a map of key value pairs to be substituted into the stored
	 *            content
	 * @param owningClass
	 *            - any class in the bundle containing the resource to store.
	 *            'this.getClass()' is generally the safest way to specify.
	 * @return
	 * @throws ZosFileException
	 */
	public IZosUNIXFile storeResourcesDirectory(String resourcePath,
			String unixPath, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass)
			throws ZosFileException;

	/**
	 * This method retrieves all contents of a given directory from the
	 * 'resources' folder in a jat bundle (test or infrastructure). It will
	 * perform substitutions using the '++' skeleton processor on each file,
	 * then write those files to a tar archive, re-encoding as necessary. The
	 * archive is then transferred to UNIX, extracted and each file is copied as
	 * a member to the requested PDS (which will be created if it does not
	 * exist). <br />
	 * <br />
	 * N.B. All files are copied from the resource directory and all
	 * sub-directories. The files will be upper-cased, and any file extensions
	 * stripped to generate the names of the PDS members
	 * 
	 * @param resourcePath
	 *            - the path to the directory in the bundle, specified relative
	 *            to 'resources'
	 * @param dsName
	 *            - the name of the target dataset (need not exist)
	 * @param pdsType
	 *            - {@link DSType#LIBRARY} or {@link DSType#PDS}
	 * @param fileType
	 *            - {@link IZosDataset#TEXT} or {@link IZosDataset#BIN}
	 * @param image
	 *            - the zOS image for the target PDS
	 * @param substitutionParameters
	 *            - a map of key value pairs to be substituted into the stored
	 *            content
	 * @param owningClass
	 *            - any class in the bundle containing the resource to store.
	 *            'this.getClass()' is generally the safest way to specify.
	 * @return
	 * @throws ZosFileException
	 */
	public IZosDataset storeResourcesPDS(String resourcePath, String dsName,
			DSType pdsType, int fileType, IZosImage image,
			Map<String, Object> substitutionParameters, Class<?> owningClass)
			throws ZosFileException;

	/**
	 * Store the content of a UNIX file with the test output.
	 * 
	 * @param file
	 * @param image
	 * @throws ZosFileException
	 */
	public void storeFileToTestOutput(IZosUNIXFile file, IZosImage image)
			throws ZosFileException;

	/**
	 * Store the content of a Dataset with the test output.
	 * 
	 * @param dataset
	 * @param image
	 * @throws ZosFileException
	 */
	public void storeDatasetToTestOutput(IZosDataset dataset, IZosImage image)
			throws ZosFileException;

}
