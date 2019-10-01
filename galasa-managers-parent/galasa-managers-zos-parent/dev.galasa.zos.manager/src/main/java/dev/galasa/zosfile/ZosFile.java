package dev.galasa.zosfile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Requests access to the zOS File Manager
 * 
 * <p>Used to populate a {@link IZosFile} field</p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosFileField
@ValidAnnotatedFields({ IZosFile.class })
public @interface ZosFile {
	
	/**
	 * The tag of the zOS Image this variable is to be populated with
	 */
	String imageTag() default "primary";
}
