/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */

package dev.galasa.eclipse.windows;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.eclipse.EclipseType;
import dev.galasa.eclipse.EclipseVersion;
import dev.galasa.java.JavaVersion;

/**
 * <p>
 * Represents a Java installation on a Ubuntu image
 * </p>
 * 
 * <p>
 * Used to populate a {@link IEclipseInstallWindows} field.
 * </p>
 * 
 * @author Reece Williams
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@EclipseWindowsManagerField
@ValidAnnotatedFields({ IEclipseInstallWindows.class })
public @interface EclipseInstallWindows {
	/**
	 * The version of Eclipse that should be installed format - %%%
	 */
	EclipseVersion eclipseVersion() default EclipseVersion.v4_16;
	
	/**
	 * The version of java to be used with the eclipse installation.
	 */
	JavaVersion javaVersion() default JavaVersion.v8;
	
	/**
	 * The type of eclipse to be installed.
	 */
	EclipseType eclipseType() default EclipseType.JavaDev;
}