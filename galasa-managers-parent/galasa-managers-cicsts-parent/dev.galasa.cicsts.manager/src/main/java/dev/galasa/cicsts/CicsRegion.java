/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Represents a CICS TS region that has been provisioned for the test
 * 
 * <p>
 * Used to populate a {@link ICicsRegion} field
 * </p>
 * 
 * @author Michael Baylis
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CicstsManagerField
@ValidAnnotatedFields({ ICicsRegion.class })
public @interface CicsRegion {

    /**
     * The tag of the CICS region this variable is to be populated with
     */
    String cicsTag() default "PRIMARY";
    
    /**
     * The tag of the zOS Image that this region will be provisioned on 
     */
    String imageTag() default "PRIMARY";
}
