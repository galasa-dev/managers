package dev.galasa.eclipseruntime.manager.ivt;

import dev.galasa.eclipseruntime.ubuntu.EclipseInstallUbuntu;
import dev.galasa.eclipseruntime.ubuntu.EclipseUbuntuManagerField;
import dev.galasa.eclipseruntime.ubuntu.IEclipseInstallUbuntu;
import org.apache.commons.logging.Log;

@Test
public class EclipseruntimeManagerIVT {
	
	@Logger
	public Log logger;
	
	@EclipseInstallUbuntu
	public IEclipseInstallUbuntu instance;
	
	@Test
	public void checkNotNull() {
		assertThat(instance).isNotNull();
	}
}