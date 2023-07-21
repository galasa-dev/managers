/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a SEM topology to build a set of CICS regions from
 * 
 * @author Michael Baylis
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@SemManagerField
public @interface SemTopology {

    /**
     * The name of the SEM Model to use to build the CICS Regions
     */
    String model() default "SingleRegion";
    
    /**
     * Which image tag will be used by default for building the CICS complex
     */
    String imageTag() default "PRIMARY";
    
    /**
     * If required, what the secondary image tag is for building the CICS complex
     */
    String secondaryImageTag() default "";
}
