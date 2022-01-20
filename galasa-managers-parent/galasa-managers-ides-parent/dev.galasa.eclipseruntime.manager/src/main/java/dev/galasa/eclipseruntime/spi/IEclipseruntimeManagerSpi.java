/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.eclipseruntime.spi;

import java.nio.file.Path;

import javax.validation.constraints.NotNull;

public interface IEclipseruntimeManagerSpi {
	
	@NotNull
	Path getEclipseInstallLocation();
	
	//Maybe need a way to get the java manager as well?? but where to put it.
}