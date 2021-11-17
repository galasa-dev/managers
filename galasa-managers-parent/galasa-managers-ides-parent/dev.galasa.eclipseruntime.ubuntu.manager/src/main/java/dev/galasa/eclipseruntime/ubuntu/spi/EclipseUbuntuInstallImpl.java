/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.eclipseruntime.ubuntu.spi;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.galasa.CpuArchitecture;
import dev.galasa.eclipseruntime.EclipseManagerException;
import dev.galasa.eclipseruntime.EclipseType;
import dev.galasa.eclipseruntime.EclipseVersion;
import dev.galasa.eclipseruntime.IEclipseInstall;
import dev.galasa.eclipseruntime.spi.EclipseInstallImpl;
import dev.galasa.eclipseruntime.ubuntu.EclipseUbuntuManagerException;
import dev.galasa.eclipseruntime.ubuntu.IEclipseInstallUbuntu;
import dev.galasa.eclipseruntime.ubuntu.internal.EclipseUbuntuManagerImpl;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.OperatingSystem;
import dev.galasa.linux.spi.ILinuxManagerSpi;

public class EclipseUbuntuInstallImpl extends EclipseInstallImpl implements IEclipseInstallUbuntu {
	
	private final String linuxTag;
	
	private String downloadUri;
	
	private EclipseUbuntuManagerImpl eclipseUbuntuManager;
	
	private EclipseVersion eclipseVer;

	//Not 100% sure if i need this yet but I will see
	private EclipseType eclipseType;
	
	private ILinuxImage image;
	private Path home;
	private Path runHome;
	
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
	
	//TODO finish this method.
	public void build() throws EclipseManagerException {
		try {
			ILinuxManagerSpi linuxManager = this.eclipseUbuntuManager.getLinuxManager();
			
			this.image = linuxManager.getImageForTag(linuxTag);
			this.home = image.getHome();
			this.runHome = this.image.getRunDirectory();
		} catch (Exception e) {
			
		}
		InstallEclipse();
	}
	
	private void InstallEclipse() throws EclipseUbuntuManagerException {
		try {
			//The space on the linux box that contains everything the manager will use.
			//I.E. This directory will contain the custom 
			Path managerHome = home.resolve(EclipseUbuntuManagerImpl.NAMESPACE);
			
			//The space on the linux box where the archived versions of eclipse will be located locally.
			Path archivesHome = this.image.getArchivesDirectory().resolve(EclipseUbuntuManagerImpl.NAMESPACE);
			
			ICommandShell cmdLine = image.getCommandShell();
			
			String filename = formatFilename();
			
			String directoryName = filename.substring(0,filename.length()-7);
			
			//Need to check to see if this version of eclipse is already installed to save time and confusion.
			if(Files.exists(archivesHome)) {
					//Need to check and see if the actual files are eclipse files.
					//The below version only works if the filename stays the same after extraction but I need to investigate this with my ubuntu image.
					//I will rewrite the formatFilename method if the above is the case.
				String possibleDirectoryName = formatFilename();
				//This is to remove the .tar.gz from the filename.
				Path possibleEclipseInstall = archivesHome.resolve(directoryName);
					
				String response = cmdLine.issueCommand("ls " + possibleEclipseInstall);					
					
				if(!response.contains("No such file or directory"))
				{
					//This will return as the version of eclipse is already installed in this test environment.
					return;
				}
			}
			
			//download the tarball version of eclipse from the nexus
			Path downloadLocation = downloadEclipse();
			
			//Moves the temporary file version of the archived eclipse version to the archives directory.
			cmdLine.issueCommand("mv " + downloadLocation + " " + archivesHome.toString() + "/"+ formatFilename());
			try {
				//Checks to see if the tar archive version is in the archive uncompressed for some reason
				String tarResponse = cmdLine.issueCommand("ls | grep " + formatFilename());
				
				if(tarResponse.contains(formatFilename()))
				{
					cmdLine.issueCommand("tar -xvzf " + formatFilename());
					cmdLine.issueCommand("mv eclipse " + directoryName);
				} 
			} catch (Exception e) {
				//need to add proper catch method here
			}
			//If it is not already installed then I need to grab the archive version of it and move it to the correct archive folder.
			
			//Once moved over I need to have a (depending on whether we go for a compressed or uncompressed archive) look at untarring the archive
			
			//I then need to create some of the pre-requisites for running the eclipse install from command line which I am yet to figure out (awaiting investigation)

			
		} catch (Exception e) {
			throw new EclipseUbuntuManagerException("Eclipse Installation failed");
		}
	}
	
	private String formatFilename()
	{
		String Filename = "eclipse-java-"+eclipseVer.toString();
		//This is due to the fact that the filename for the 2018-09 install does not contain the "R" flag for some reason
		if(eclipseVer != EclipseVersion.V201809)
		{
			Filename = Filename+"-R";
		}
		Filename = Filename + "-linux-gtk-x86_64.tar.gz";
		return Filename;
	}
	
	//TODO Write this method once the download and install logic is working correctly
	public void discard()
	{
		//Super.discard?
		//need to delete eclipse or?
		//further investigation needed.
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
	public IJavaInstallation getJavaInstallation() {
		// currently unsure whether this is needed.
		return null;
	}
}

