/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.gradle.ubuntu;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.gradle.GradleVersion;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a Gradle installation on a Ubuntu image.
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
@GradleUbuntuManagerField
@ValidAnnotatedFields({ IGradleUbuntuInstallation.class })
public @interface GradleUbuntuInstallation {
    
    /**
     * Version of Gradle to be installed.
     */
    GradleVersion gradleVersion() default GradleVersion.v7_1_1;

    /**
     * The tag to be assigned to this Gradle installation.
     */
    String gradleTag() default "PRIMARY";

    /**
     * The tag of the Linux Image this installation is to be associated with.
     */
    String imageTag() default "PRIMARY";

}
