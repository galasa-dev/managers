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

import dev.galasa.core.manager.internal.CoreManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**  Test change2
 * <p>
 * Fill this field with the Core Manager instance.
 * </p>
 *
 * <p>
 * Will only populate public {@link ICoreManager} fields.
 * </p>
 *
 * @author Michael Baylis
 * @see {@link ICoreManager}
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ ICoreManager.class })
public @interface CoreManager {

}
