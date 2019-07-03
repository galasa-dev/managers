package dev.voras.common.linux;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.voras.common.ipnetwork.IIpHost;
import dev.voras.common.linux.internal.LinuxManagerField;
import dev.voras.framework.spi.ValidAnnotatedFields;

/**
 * Represents a IP Host for a Linux Image that has been provisioned for the test
 * 
 * <p>Used to populate a {@link dev.voras.ipnetwork.IIpHost} field</p>
 * 
 * @author Michael Baylis
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@LinuxManagerField
@ValidAnnotatedFields({ IIpHost.class })
public @interface LinuxIpHost {
	
	/**
	 * The tag of the Linux Image this variable is to be populated with
	 */
	String imageTag() default "primary";
	
}
