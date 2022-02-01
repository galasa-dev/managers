/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.core.manager;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * <p>
 * Fill this field with a unique (within the ecosystem) string of a set length.
 * The CPS property core.resource.string.[length].pattern determines the make up of the random string.
 * </p>
 * <p>
 * Will only populate public {@link java.lang.Stringdev.galasa.core.manager.IResourceString} fields.
 * </p>
 *
 * @author Michael Baylis
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ dev.galasa.core.manager.IResourceString.class })
public @interface ResourceString {
    
    
    /**
     * The tag of the Resource String this variable is to be populated with
     */
    String tag() default "PRIMARY";
    
    /**
     * Generate and lock a resource string
     * 
     * @return the length of the string to generate
     */
    public int length() default 8;


}
