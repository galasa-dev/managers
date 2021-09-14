/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.gradle.windows;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.gradle.GradleVersion;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a Gradle installation on a Windows image.
 * 
 * <p>
 * Used to populate a {@link IGradleUbuntuInstallation} field
 * </p>
 * 
 * @author Matthew Chivers
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@GradleWindowsManagerField
@ValidAnnotatedFields({ IGradleWindowsInstallation.class })
public @interface GradleWindowsInstallation {
    
    /**
     * Version of Gradle to be installed.
     */
    GradleVersion gradleVersion() default GradleVersion.v7_1_1;

    /**
     * The tag to be assigned to this Gradle installation.
     */
    String gradleTag() default "PRIMARY";

    /**
     * The tag of the Windows Image this installation is to be associated with.
     */
    String imageTag() default "PRIMARY";

    /**
     * The tag of the Java installation this Gradle installation is to be associated with.
     * e.g. Set `org.gradle.java.home` to equal this Java installation home.
     */
    String javaTag() default "PRIMARY";

}
