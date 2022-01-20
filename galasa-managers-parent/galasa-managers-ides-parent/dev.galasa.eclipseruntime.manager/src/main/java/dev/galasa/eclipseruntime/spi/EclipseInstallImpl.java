/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.eclipseruntime.spi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import dev.galasa.CpuArchitecture;
import dev.galasa.OperatingSystem;
import dev.galasa.eclipseruntime.EclipseManagerException;
import dev.galasa.eclipseruntime.EclipseType;
import dev.galasa.eclipseruntime.EclipseVersion;
import dev.galasa.eclipseruntime.IEclipseInstall;
import dev.galasa.eclipseruntime.internal.EclipseRuntimeManagerImpl;
import dev.galasa.eclipseruntime.manager.internal.properties.DownloadLocation;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.java.JavaManagerException;

public abstract class EclipseInstallImpl implements IEclipseInstall {
	private final static Log logger = LogFactory.getLog(EclipseInstallImpl.class);
	private final EclipseRuntimeManagerImpl eclipseManager;
	private final EclipseType eclipseType;
	private final EclipseVersion eclipseVersion;
	private final CpuArchitecture cpuArchitecture;
	private final OperatingSystem operatingSystem;
	private String downloadLocation;
	
	private Path archive;
	
	public EclipseInstallImpl (IEclipseruntimeManagerSpi eclipseManager,
			EclipseType eclipseType,
			EclipseVersion eclipseVersion,
			CpuArchitecture cpuArchitecture,
			OperatingSystem operatingSystem){
		this.eclipseManager = (EclipseRuntimeManagerImpl)eclipseManager;
		this.eclipseType = eclipseType;
		this.eclipseVersion = eclipseVersion;
		this.operatingSystem = operatingSystem;
		this.cpuArchitecture = cpuArchitecture;
		this.archive = null;
	}
	
	public Path downloadEclipse() throws EclipseManagerException {
		String downloadArchive = getDownloadLocation();
		URI uri;
		try {
			uri = new URI(downloadArchive);
		} catch (URISyntaxException e) {
			throw new EclipseManagerException("Invalid Download Location", e);
		}
		
		IHttpManagerSpi httpManager = eclipseManager.getHttpManager();
		IHttpClient client = httpManager.newHttpClient();
		client.setURI(uri);
		
		try (CloseableHttpResponse response = client.getFile(uri.getPath())) {
			//Creates a temporary file location to download the eclipse Install to.
			Path archive = Files.createTempFile("galasa.eclipseruntime.", ".archive");
			
			//sets a flag so that the file will be deleted when the test/machine terminates
			archive.toFile().deleteOnExit();
			
			//Grabs the entity from the closableHttpResponse that was made above.
			HttpEntity entity = response.getEntity();
			
			//replaces the temporary file with the file downloaded from the nexus.
			Files.copy(entity.getContent(), archive, StandardCopyOption.REPLACE_EXISTING);
			
			this.archive = archive;
			
			//returns the path to the downloaded eclipse version
			return archive;
		} catch (HttpClientException e) {
			logger.error("There was an issue downloading the file from the download location.");
			throw new EclipseManagerException("There was an error downloading the eclipse version");
		} catch (IOException e) {
			logger.error("There was an issue copying the downloaded file to the archive");
			throw new EclipseManagerException("Unable to copy contents of temporary downloaded file to the archive path.");
		} 
	}
	
	@Override
	public IJavaInstallation getJavaInstallation() throws JavaManagerException {
		return eclipseManager.getJavaManager().getInstallationForTag("PRIMARY");
	}
	
	public Path getLocalArchivePath() {
		return archive;
	}
	
	private String getDownloadLocation() throws EclipseManagerException
	{
		if (this.downloadLocation!=null)
		{
			logger.trace("The download location has already been set as " + downloadLocation+ " and has been retrieved");
			return this.downloadLocation;
		}
		
		logger.debug("Retreiving download location for " + eclipseVersion.getFriendlyString() +" "+ operatingSystem);
		this.downloadLocation = DownloadLocation.get(eclipseType, eclipseVersion, operatingSystem, cpuArchitecture);
		
		
		if(this.downloadLocation == null) {
			throw new EclipseManagerException("Unable to retrieve download archive for " +eclipseVersion.getFriendlyString() + "version of "
					+ "eclipse on " + this.operatingSystem);
		}
		if(this.downloadLocation.startsWith("http") || this.downloadLocation.startsWith("https:"))
		{
			logger.trace("The download location has been retrieved as " + this.downloadLocation);
			return this.downloadLocation;
		} else {
			throw new EclipseManagerException("This download archive location is unsupported. Download archives should begin with http(s):");
		}
	}
	
	public void discard() {
		
	}
}

