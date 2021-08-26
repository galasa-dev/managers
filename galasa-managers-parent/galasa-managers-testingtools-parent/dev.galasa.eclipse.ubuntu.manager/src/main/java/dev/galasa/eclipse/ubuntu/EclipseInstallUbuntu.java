/*
* Copyright contributors to the Galasa project 
*/

package dev.galasa.eclipse.ubuntu;

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
 * Represents an Eclipse Installation on an Ubuntu Image
 * </p>
 * 
 * <p>
 * Used to populate a {@link IEclipseInstallUbuntu} field
 * </p>
 * 
 * @author Reece Williams
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@EclipseUbuntuManagerField
@ValidAnnotatedFields({ IEclipseInstallUbuntu.class })
public @interface EclipseInstallUbuntu {
	/**
	 * The version of Eclipse that should be installed format - %%%
	 */
	EclipseVersion eclipseVersion() default EclipseVersion.v4_16;
	
	/**
	 * The version of java to be used with the eclipse installation.
	 */
	JavaVersion javaVersion() default JavaVersion.v8;
	
	/**
	 * The type of eclipse to install.
	 */
	EclipseType eclipseType() default EclipseType.JavaDev;
}