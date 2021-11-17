package dev.galasa.eclipseruntime.spi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import dev.galasa.http.HttpClientResponse;
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
	private final IJavaInstallation javaInstall;
	private String downloadLocation;
	
	private Path archive;
	
	//Make variable for different things such as eclipse and java version
	
	//Look into making path for eclipse install as a variable.
	
	//Need to look at how to do HTTP download.
	
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
		//Not sure how exactly to initalise these below variables yet.
		this.javaInstall = null;
		this.archive = null;
		//How do I get the java install from the eclipse manager?
		//Add things such as javaVersion, eclipse type etc??
		//How do I provision a Java manager? Does it need to be specific or can it be generic?
		
		
	}
	
	public Path downloadEclipse() throws EclipseManagerException {
		String downloadArchive = getDownloadLocation();
		URI uri;
		try {
			uri = new URI(downloadArchive);
		} catch (URISyntaxException e) {
			throw new EclipseManagerException("Invalid Download Location");
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
			// TODO Add proper logger.log messages
			throw new EclipseManagerException("There was an error downloading the eclipse version");
		} catch (IOException e) {
			// TODO add proper logger.log messages for this error
			throw new EclipseManagerException("Failed to copy contents of file over.");
		} 
	}
	
//	protected String formatURL(String fileName)
//	{
//		//This URL will be offloaded to a property once I set them up correctly
//		String downloadURL = "https://archive.eclipse.org/technology/epp/downloads/release/";
//		downloadURL = downloadURL + eclipseVersion.getFriendlyString() + "/R/" + fileName;
//		return downloadURL;
//	}
	

	public Path getEclipseInstallPath() {
		return archive;
	}
	
	private String getDownloadLocation() throws EclipseManagerException
	{
		if (this.downloadLocation!=null)
		{
			return this.downloadLocation;
		}
		this.downloadLocation = DownloadLocation.get(eclipseType, eclipseVersion, operatingSystem, cpuArchitecture);
		if(this.downloadLocation == null) {
			throw new EclipseManagerException("Unable to retrieve download archive for " +eclipseVersion.getFriendlyString() + "version of "
					+ "eclipse on " + this.operatingSystem);
		}
		if(this.downloadLocation.startsWith("http") || this.downloadLocation.startsWith("https:"))
		{
			return this.downloadLocation;
		} else {
			throw new EclipseManagerException("This download archive location is unsupported. Download archives should begin with http(s):");
		}
	}
	
	public void discard() {
		//How specific to platform is this process and do i need to 
		//only implement the very basics in here?
		//Need to provide path to eclipse install or files to try a deletion.
	}
	
	
	
}