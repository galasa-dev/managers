package dev.voras.common.linux.spi;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.voras.ManagerException;
import dev.voras.common.linux.OperatingSystem;

public interface ILinuxProvisioner {

	ILinuxProvisionedImage provision(@NotNull String tag, @NotNull OperatingSystem operatingSystem, @NotNull List<String> capabilities) throws ManagerException;

}
