package io.ejat.zos;

/**
 * <p>Represents a zOS Image (or lpar).</p>
 * 
 * <p>Use a {@link ZosImage} annotation to populate this field with</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IZosImage {

	/**
	 * Get the name of the zOS Image, may be different from the SMFID
	 */
	String getImageID();
	
	/**
	 * Get the name of the Sysplex this Image belongs to
	 */
	String getSysplexID();
	
	/**
	 * Get the name of the Cluster this Image belongs to 
	 */
	String getClusterID();
}
