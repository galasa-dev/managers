package dev.galasa.zosmf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zosmf.internal.ZosmfManagerField;

/**
 * Used to annotate annotations that are to be used for Test Class fields. To be
 * populated by the Manager.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ZosmfManagerField
@ValidAnnotatedFields({ IZosmf.class })
public @interface Zosmf {
	
	/**
	 * The tag of the zOS Image this variable is to be populated with
	 */
	String imageTag() default "primary";

}
