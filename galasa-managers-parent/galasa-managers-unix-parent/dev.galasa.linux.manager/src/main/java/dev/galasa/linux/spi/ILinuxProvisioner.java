/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2021.
 */
package dev.galasa.linux.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.linux.OperatingSystem;

public interface ILinuxProvisioner {

    ILinuxProvisionedImage provisionLinux(@NotNull String tag, @NotNull OperatingSystem operatingSystem,
            @NotNull List<String> capabilities) throws ManagerException, ResourceUnavailableException, InsufficientResourcesAvailableException;

}
