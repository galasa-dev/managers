/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.java.spi;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import dev.galasa.CpuArchitecture;
import dev.galasa.OperatingSystem;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.JavaType;
import dev.galasa.java.JavaVersion;
import dev.galasa.java.internal.JavaManagerImpl;
import dev.galasa.java.internal.properties.DefaultVersion;
import dev.galasa.java.internal.properties.DownloadLocation;

public abstract class JavaInstallationImpl implements IJavaInstallation {
    
    private final static Log                   logger       = LogFactory.getLog(JavaInstallationImpl.class);
    
    private final JavaManagerImpl javaManager;
    private final JavaType        javaType; 
    private final OperatingSystem operatingSystem;
    private final CpuArchitecture cpuArchitecture;
    private final JavaVersion     javaVersion;
    private final String          javaJvm;
    private final String          javaTag; 
    
    private Path archive;
    private String downloadLocation;
    
    public JavaInstallationImpl(IJavaManagerSpi javaManager, 
            JavaType javaType, 
            OperatingSystem operatingSystem, 
            CpuArchitecture cpuArchitecture,
            JavaVersion     javaVersion,
            String javaJvm, 
            String javaTag) throws JavaManagerException {
        this.javaManager     = (JavaManagerImpl)javaManager;
        this.javaType        = javaType;
        this.operatingSystem = operatingSystem;
        this.cpuArchitecture = cpuArchitecture;
        this.javaJvm         = javaJvm;
        this.javaTag         = javaTag;
        
        this.javaManager.registerJavaInstallationForTag(javaTag, this);
        
        if (javaVersion == JavaVersion.vDefault) {
            this.javaVersion = DefaultVersion.get();
            logger.info("Using default Java version " + this.javaVersion + " for installation tag " + this.javaTag);
        } else {
            this.javaVersion     = javaVersion;
        }
    }

    @Override
    public Path retrieveArchive() throws JavaManagerException {
        if (archive != null) {
            return archive;
        }
        
        String downloadLocation = getDownloadLocation();
        if (downloadLocation.startsWith("http:") || downloadLocation.startsWith("https:")) {
            return downloadHttp(downloadLocation);
        } else {
            throw new JavaManagerException("Unsupported archive location " + downloadLocation);
        }
    }

    private Path downloadHttp(String downloadLocation) throws JavaManagerException {
        
        logger.trace("Retrieving Java archive from " + downloadLocation);
        
        URI uri;
        try {
            uri = new URI(downloadLocation);
        } catch (URISyntaxException e) {
            throw new JavaManagerException("Invalid Java archive download location", e);
        }
        
        IHttpManagerSpi httpManager = this.javaManager.getHttpManager();
        IHttpClient client = httpManager.newHttpClient();
        client.setURI(uri);
        
        try (CloseableHttpResponse response = client.getFile(uri.getPath())) {
           
            Path archive = Files.createTempFile("galasa.java.", ".archive");
            archive.toFile().deleteOnExit();
            
            HttpEntity entity = response.getEntity();
            
            Files.copy(entity.getContent(), archive, StandardCopyOption.REPLACE_EXISTING);
            
            this.archive = archive;
            
            return archive;
        } catch (Exception e) {
            throw new JavaManagerException("Unable to download Java archive " + downloadLocation, e);
        }
    }
    
    @Override
    public String getArchiveFilename() throws JavaManagerException {
        String downloadLocation = getDownloadLocation();
        
        URL url;
        try {
            url = new URL(downloadLocation);
        } catch (MalformedURLException e) {
            throw new JavaManagerException("Invalid Java archive download location", e);
        }
        
        String fileName = url.getFile();
        while(fileName.startsWith("/") || fileName.startsWith("\\")) {
            fileName = fileName.substring(1);
        }
        
        return fileName;
    }

    private String getDownloadLocation() throws JavaManagerException {
        if (this.downloadLocation != null) {
            return this.downloadLocation;
        }
        
        this.downloadLocation = DownloadLocation.get(this.javaType, this.operatingSystem, this.cpuArchitecture, this.javaVersion, this.javaJvm);
        if (this.downloadLocation == null) {
            throw new JavaManagerException("Unable to determine location of Java archive for type=" 
                    + this.javaType
                    + ", os=" 
                    + this.operatingSystem
                    + ", cpu="
                    + this.cpuArchitecture
                    + ", version="
                    + this.javaVersion
                    + ", jvm="
                    + this.javaJvm);
        }
        
        
        if (this.downloadLocation.startsWith("http:") || this.downloadLocation.startsWith("https:")) {
            return this.downloadLocation;
        } else {
            throw new JavaManagerException("Unsupported archive location " + this.downloadLocation);
        }
    }
    
    protected void discard() {
        if (this.archive != null) {
            try {
                Files.delete(archive);
            } catch(Exception e) {
                logger.trace("Failed to delete downloaded Java archive at " + this.archive);
            }
        }
    }
    
    public String getTag() {
        return this.javaTag;
    }
    

}
