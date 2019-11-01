/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosfile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Requests access to the zOS File Manager
 * 
 * <p>Used to populate a {@link IZosFileHandler} field</p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosFileManagerField
@ValidAnnotatedFields({ IZosFileHandler.class })
public @interface ZosFileHandler {

}
