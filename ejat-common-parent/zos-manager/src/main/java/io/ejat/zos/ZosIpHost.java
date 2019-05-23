package io.ejat.zos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.ejat.framework.spi.ValidAnnotatedFields;
import io.ejat.ipnetwork.IIpHost;
import io.ejat.zos.internal.ZosManagerField;

/**
 * Represents a IP Host for a zOS Image that has been provisioned for the test
 * 
 * <p>Used to populate a {@link io.ejat.ipnetwork.IIpHost} field</p>
 * 
 * @author Michael Baylis
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosManagerField
@ValidAnnotatedFields({ IIpHost.class })
public @interface ZosIpHost {
	
	/**
	 * The tag of the zOS Image this variable is to be populated with
	 */
	String imageTag() default "primary";
	
}
