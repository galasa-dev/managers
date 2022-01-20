/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.eclipseruntime.ubuntu.spi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.CpuArchitecture;
import dev.galasa.eclipseruntime.EclipseManagerException;
import dev.galasa.eclipseruntime.EclipseType;
import dev.galasa.eclipseruntime.EclipseVersion;
import dev.galasa.eclipseruntime.spi.EclipseInstallImpl;
import dev.galasa.eclipseruntime.spi.IEclipseruntimeManagerSpi;
import dev.galasa.eclipseruntime.ubuntu.EclipseUbuntuManagerException;
import dev.galasa.eclipseruntime.ubuntu.IEclipseInstallUbuntu;
import dev.galasa.eclipseruntime.ubuntu.internal.EclipseUbuntuManagerImpl;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.OperatingSystem;
import dev.galasa.linux.spi.ILinuxManagerSpi;

public class EclipseUbuntuInstallImpl extends EclipseInstallImpl implements IEclipseInstallUbuntu, IEclipseruntimeManagerSpi{
	
	private static Log logger = LogFactory.getLog(EclipseUbuntuInstallImpl.class);
	
	private final String linuxTag;
	
	private EclipseUbuntuManagerImpl eclipseUbuntuManager;
	
	private EclipseVersion eclipseVer;

	private EclipseType eclipseType;
	
	private ILinuxImage image;
	private Path home;
	private Path runHome;
	private Path InstallPath;
	
	public EclipseUbuntuInstallImpl(
			EclipseUbuntuManagerImpl eclipseUbuntuManagerImpl,
			EclipseVersion eclipseVer,
			EclipseType eclipseType,
			String javaType,
			String linuxTag) {
		super(eclipseUbuntuManagerImpl.getEclipseManager(), eclipseType, eclipseVer, CpuArchitecture.x86, OperatingSystem.linux);
		this.linuxTag = linuxTag;
		this.eclipseVer = eclipseVer;
		this.eclipseType = eclipseType;
		
		this.eclipseUbuntuManager = eclipseUbuntuManagerImpl;
	}
	
	public void build() throws EclipseManagerException {
		try {
			ILinuxManagerSpi linuxManager = this.eclipseUbuntuManager.getLinuxManager();
			
			this.image = linuxManager.getImageForTag(this.linuxTag);
			
			this.home = image.getHome();
			
			this.runHome = this.image.getRunDirectory();			
		} catch (Exception e) {
			throw new EclipseManagerException("Build step for linux image failed");
		}
		InstallEclipse();
	}
	
	private void InstallEclipse() throws EclipseUbuntuManagerException {
		try {
			ICommandShell cmdLine = image.getCommandShell();
			//The space on the linux box that contains everything the manager will use.
			//I.E. This directory will contain the workspace (If required, need to check manager requirements) 
			Path managerHome = home.resolve(EclipseUbuntuManagerImpl.NAMESPACE);
			
			if(Files.notExists(managerHome)) {
				Files.createDirectories(managerHome);
			}
		
			//The space on the linux box where the archived versions of eclipse will be located locally.
			Path archivesHome = this.image.getArchivesDirectory().resolve(EclipseUbuntuManagerImpl.NAMESPACE);
			
			//Checks to see whether or not the appropriate folders have been created. This may need to be automated however this is an issue due to the
			//protected nature of the archives home.
			if(Files.notExists(archivesHome)) {
				throw new EclipseUbuntuManagerException("The archive directory "+archivesHome+" Has not been created on the machine.");
			}
			
			String filename = formatFilename();
			
			//Formats the filename without the .tar.gz suffix
			String directoryName = filename.substring(0,filename.length()-7);
			
			//Need to check to see if this version of eclipse is already installed to save time and confusion.
			if(Files.exists(archivesHome)) {
				
				Path possibleEclipseInstall = archivesHome.resolve(directoryName);
				
				String response = cmdLine.issueCommand("ls " + possibleEclipseInstall);					
				
				if(!response.contains("No such file or directory"))
				{
					//This will return as the version of eclipse is already installed in this test environment.
					logger.info("This version of eclipse is already present in the archive.");
					this.InstallPath = possibleEclipseInstall;
					return;
				}
			}
			
			//Download the tarball version of eclipse from the nexus
			Path downloadedFileLocation = downloadEclipse();
			
			Path archiveFileName = archivesHome.resolve(filename);
			
			Files.createFile(archiveFileName);
			
			Files.copy(downloadedFileLocation, archiveFileName, StandardCopyOption.REPLACE_EXISTING);
			
			try {
				//Checks to see if the tar archive version is in the archive but still uncompressed for some reason
				String tarResponse = cmdLine.issueCommand("ls " + archivesHome +" | grep " + formatFilename());
				
				if(tarResponse.contains(formatFilename()))
				{
					cmdLine.issueCommand("tar -xvzf " + archiveFileName + " -C " + archivesHome);
					
					cmdLine.issueCommand("mv " + archivesHome.resolve("eclipse") + " " + archivesHome.resolve(directoryName));
					logger.debug("The Eclipse archive has been downloaded and untarred to " +archivesHome);
					this.InstallPath = archivesHome.resolve(directoryName);
				} 
			} catch (Exception e) {
				throw new EclipseUbuntuManagerException("moving and untarring the file failed " + e);
			}
			
		} catch (Exception e) {
			throw new EclipseUbuntuManagerException("Eclipse Installation failed " + e);
		}
	}
	
	private String formatFilename()
	{
		String Filename = "eclipse-java-"+eclipseVer.getFriendlyString();
		//This is due to the fact that the filename for the 2018-09 install does not contain the "R" flag for some reason
		if(eclipseVer != EclipseVersion.V201809)
		{
			Filename = Filename+"-R";
		}
		Filename = Filename + "-linux-gtk-x86_64.tar.gz";
		return Filename;
	}
	
	public void discard()
	{
		
	}

	@Override
	public EclipseVersion getEclipseVersion() {
		return this.eclipseVer;
	}

	@Override
	public EclipseType getEclipseType() {
		return this.eclipseType;
	}
	
	@Override
	public Path getEclipseInstallLocation()
	{
		return InstallPath;
	}
}

