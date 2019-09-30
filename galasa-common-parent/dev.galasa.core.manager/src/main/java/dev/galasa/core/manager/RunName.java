package dev.galasa.core.manager;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.galasa.core.manager.internal.CoreManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * <p>
 * Fill this field with the name of the Test Run. Can be used for making
 * resource names unique to this run. The Test Run will be unique across all
 * Local and Automated runs that are in the system at that point.
 * </p>
 *
 * <p>
 * Will only populate public {@link java.lang.String} fields.
 * </p>
 *
 * @author Michael Baylis
 * @see {@link java.lang.String}
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ String.class })
public @interface RunName {

}
