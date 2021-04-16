/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.textscan;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * <p>
 * Fill this field with a Text Scanner object.
 * </p>
 *
 * <p>
 * Will only populate public {@link ITextScanner} fields.
 * </p>
 *
 * @author Michael Baylis
 * @see {@link ITextScanner}
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
@TextScanManagerField
@ValidAnnotatedFields({ ITextScanner.class })
public @interface TextScanner {

}
