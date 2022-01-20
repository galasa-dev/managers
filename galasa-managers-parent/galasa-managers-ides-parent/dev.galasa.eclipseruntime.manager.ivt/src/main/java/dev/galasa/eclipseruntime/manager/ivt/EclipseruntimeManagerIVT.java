/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.eclipseruntime.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.eclipseruntime.ubuntu.EclipseInstallUbuntu;
import dev.galasa.eclipseruntime.ubuntu.IEclipseInstallUbuntu;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.LinuxImage;
import dev.galasa.linux.OperatingSystem;

import org.apache.commons.logging.Log;

@Test
public class EclipseruntimeManagerIVT {
	
	@Logger
	public Log logger;
	
	@EclipseInstallUbuntu
	public IEclipseInstallUbuntu instance;
	
	@LinuxImage(operatingSystem = OperatingSystem.ubuntu, imageTag = "REECE")
	public ILinuxImage image;
	
	@Test
	public void checkNotNull() {
		assertThat(instance).isNotNull();
	}
}