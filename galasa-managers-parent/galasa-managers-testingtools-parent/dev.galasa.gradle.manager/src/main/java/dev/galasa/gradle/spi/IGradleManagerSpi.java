/*
 * Copyright contributors to the Galasa project
 */

package dev.galasa.gradle.spi;

import dev.galasa.gradle.GradleManagerException;
import dev.galasa.gradle.IGradleInstallation;
import javax.validation.constraints.NotNull;

/**
 * Provides utilities to enable management of the manager itself.
 * e.g. Registering tags ("PRIMARY", "SECONDARY", etc.) and fetching installations represented by a tag.
 * 
 */
public interface IGradleManagerSpi {
    
    void registerGradleInstallationForTag(@NotNull String tag, @NotNull IGradleInstallation gradleInstallation) 
            throws GradleManagerException;

    @NotNull
    IGradleInstallation getInstallationForTag(@NotNull String tag) throws GradleManagerException;
    
}
