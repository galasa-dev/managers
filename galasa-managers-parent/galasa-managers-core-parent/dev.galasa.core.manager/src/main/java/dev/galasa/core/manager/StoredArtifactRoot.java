/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.core.manager;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;

import dev.galasa.core.manager.internal.CoreManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * <p>
 * Fill this field Root path of the Stored Artifacts for this Test Run. This can
 * be used to record test logs, job output, trace files etc.
 * </p>
 *
 * <p>
 * Will only populate public {@link java.nio.files.Path} fields.
 * </p>
 *
 * @author Michael Baylis
 * @see {@link java.nio.files.Path}
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ Path.class })
public @interface StoredArtifactRoot {

}
