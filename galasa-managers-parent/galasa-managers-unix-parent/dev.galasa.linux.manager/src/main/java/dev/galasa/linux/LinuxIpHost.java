/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2021.
 */
package dev.galasa.linux;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Represents a IP Host for a Linux Image that has been provisioned for the test
 * 
 * <p>
 * Used to populate a {@link dev.galasa.ipnetwork.IIpHost} field
 * </p>
 * 
 * @author Michael Baylis
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@LinuxManagerField
@ValidAnnotatedFields({ IIpHost.class })
public @interface LinuxIpHost {

    /**
     * The tag of the Linux Image this variable is to be populated with
     */
    String imageTag() default "PRIMARY";

}
