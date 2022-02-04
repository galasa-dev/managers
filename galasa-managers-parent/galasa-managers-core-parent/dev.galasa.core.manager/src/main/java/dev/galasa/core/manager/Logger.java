/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.core.manager;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.commons.logging.Log;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * <p>
 * Fill this field with the Logger instance for this Test Class.
 * </p>
 * <p>
 * Will only populate public {@link org.apache.commons.logging.Log} fields.
 * </p>
 *
 * @see {@link org.apache.commons.logging.Log}
 * @author Michael Baylis
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ Log.class })
public @interface Logger {

}
