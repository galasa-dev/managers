package dev.galasa.eclipseruntime.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.eclipseruntime.ubuntu.EclipseInstallUbuntu;
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