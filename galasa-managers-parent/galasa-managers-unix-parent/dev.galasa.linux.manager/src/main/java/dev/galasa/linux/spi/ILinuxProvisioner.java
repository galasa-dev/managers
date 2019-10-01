package dev.galasa.linux.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.linux.OperatingSystem;

public interface ILinuxProvisioner {

	ILinuxProvisionedImage provision(@NotNull String tag, @NotNull OperatingSystem operatingSystem, @NotNull List<String> capabilities) throws ManagerException;

}
