package io.ejat.zos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.ejat.framework.spi.ValidAnnotatedFields;
import io.ejat.ipnetwork.IIpPort;
import io.ejat.zos.internal.ZosManagerField;

/**
 * Represents a provisioned IP Port for a zOS Image that has been provisioned for the test
 * 
 * <p>Used to populate a {@link io.ejat.ipnetwork.IIpPort} field</p>
 * 
 * @author Michael Baylis
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosManagerField
@ValidAnnotatedFields({ IIpPort.class })
public @interface ZosIpPort {
	
	/**
	 * The tag of the zOS Image this variable is to be populated with
	 */
	String imageTag() default "primary";
	
	
	/**
	 * The type of IP Port you require, this can be used to obtain ATTLS ports
	 * The default is a standard non-special IP port
	 */
	String type() default "standard";
	
}
