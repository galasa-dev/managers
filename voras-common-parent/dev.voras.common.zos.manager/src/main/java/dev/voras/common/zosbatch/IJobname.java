package dev.voras.common.zosbatch;

/**
 * <p>Represents a privovision zOS Jobname</p>
 * 
 * <p>Use a {@link Jobname} annotation to populate this field with</p>
 * 
 * @author Michael Baylis
 *
 */
public interface IJobname {

	/**
	 * Get the name of the zOS Jobname
	 */
	String getName();
	
}
