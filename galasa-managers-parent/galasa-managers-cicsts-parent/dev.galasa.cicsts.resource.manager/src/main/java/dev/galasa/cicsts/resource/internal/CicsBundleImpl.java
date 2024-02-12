/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.CicsBundleResourceException;
import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.CicsResourceStatus;
import dev.galasa.cicsts.cicsresource.ICicsBundle;
import dev.galasa.cicsts.resource.internal.properties.DefaultResourceTimeout;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosUNIXFileException;

public class CicsBundleImpl implements ICicsBundle {
    
    private static final Log logger = LogFactory.getLog(CicsBundleImpl.class);

	private IBundleResources testBundleResources;
    private CicsResourceManagerImpl cicsResourceManager;
	private IArtifactManager artifactManager;
    private ICicsRegion cicsRegion;
    private ICicsTerminal cicsTerminal;
    private Class<?> testClass;
    private IZosImage cicsZosImage;
    private IZosUNIXFile runTemporaryUNIXPath;
    private IZosFileHandler zosFileHandler;
	private String localBundlePath;
	private Map<String, Object> parameters = new HashMap<>();
	private boolean needsCopying;
	private String bundleRoot;
    
    private String resourceDefinitionName;
    private String resourceDefinitionGroup;
    private String resourceDefinitionDescription;
    private CicsResourceStatus resourceDefinitionStatus = CicsResourceStatus.ENABLED;
    private String resourceDefinitionBundledir;
    private int skeletonType;

	private int defaultTimeout;

    private static final String SLASH_SYBMOL = "/";

	private static final String RESOURCE_TYPE_BUNDLE = "BUNDLE";

	public CicsBundleImpl(CicsResourceManagerImpl cicsResourceManager, ICicsRegion cicsRegion, ICicsTerminal cicsTerminal, Class<?> testClass, String name, String group, String bundlePath, String bunndledir, Map<String, Object> parameter,int skeletonType) throws CicsBundleResourceException {
        this.cicsResourceManager = cicsResourceManager;
        this.cicsResourceManager.registerCicsBundle(this);
        this.artifactManager = this.cicsResourceManager.getArtifactManager();
        try {
            this.zosFileHandler = this.cicsResourceManager.getZosFileHandler();
        } catch (CicsResourceManagerException e) {
            throw new CicsBundleResourceException("Unable to get zOS File Handler", e);
        }
        this.cicsRegion = cicsRegion;
        this.cicsZosImage = cicsRegion.getZosImage();
        this.cicsTerminal = cicsTerminal;
        this.testClass = testClass;
        this.resourceDefinitionName = name;
        this.resourceDefinitionGroup = group;
        this.skeletonType = skeletonType;
        logger.debug("Creating CICS Bundle Directory");      
        try {        	                          
			this.runTemporaryUNIXPath = this.zosFileHandler.newUNIXFile(cicsRegion.getRunTemporaryUNIXDirectory().getUnixPath() + "CICSBundles" + SLASH_SYBMOL , this.cicsZosImage);
		} catch (CicstsManagerException | ZosUNIXFileException e) {
			throw new CicsBundleResourceException("Unable to get run temporary UNIX path", e);
		}
        // CICS bundle source already stored on file system 
        if (bundlePath == null) {
            this.needsCopying = false;
            this.resourceDefinitionBundledir = bunndledir;
        } else {
            this.needsCopying = true;
	        if (bundlePath.endsWith(SLASH_SYBMOL)) {
	        	this.localBundlePath = bundlePath;
	        } else {
	        	this.localBundlePath = bundlePath + SLASH_SYBMOL;
	    	}
	        this.bundleRoot = new File(this.localBundlePath).getName();
	    	this.resourceDefinitionBundledir = this.runTemporaryUNIXPath.getUnixPath() + this.bundleRoot + SLASH_SYBMOL;
	    	logger.info("resourceDefinitionBundledir " + resourceDefinitionBundledir);
	        
	    	
	    	if (parameter != null && !parameter.isEmpty()) {
	        	this.parameters.putAll(parameter);
	        }
    		
			Iterator<Map.Entry<String, Object>> iterator = this.parameters.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = iterator.next();
				logger.info("Parameter :" + entry.getKey() +" " + entry.getValue());
			}
	    	this.testBundleResources = this.artifactManager.getBundleResources(this.testClass);
        }
	}
	



	public void copyBundleFilesToZfs() throws ZosUNIXFileException, IOException {
		//No need to mess around parsing XML.  Get the contents of the bundle directory and copy the files as per the resource type.
		try {
			Map<String, InputStream> dir;
			//Copy to resourceDefinitionBundledir
			dir = testBundleResources.retrieveDirectoryContents(localBundlePath);

			Iterator<Map.Entry<String, InputStream>> iterator = dir.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, InputStream> entry = iterator.next();
				
				logger.info("File entry :" + entry.getKey());
				
				//Get the file suffix
				String fileExtension = null;

				if(entry.getKey().contentEquals("cics.xml")) {
					fileExtension = "cics.xml";
				}
				else {
					fileExtension = FilenameUtils.getExtension(entry.getKey());
				}
				logger.info("fileExtension:" + fileExtension);
				//Get the component type to get file transfer type
				CicsBundleResourceType componentType = null;
				try { 
					componentType = CicsBundleResourceType.valueOf(fileExtension.toUpperCase());
				}
				catch (IllegalArgumentException e) {
					componentType = CicsBundleResourceType.valueOf("DEFAULT");
				}
				logger.info("File Type:"+ componentType);
				logger.info("Copying file " + entry.getKey() + " to " + resourceDefinitionBundledir);
				
				//Strip the directories in the Galasa resources directory to copy the bundle files to the resourceDefinitionBundledir correctly.
				int endIndex = entry.getKey().indexOf(this.bundleRoot)  + this.bundleRoot.length();;
				
				StringBuilder stringBuilder =  new StringBuilder(entry.getKey());  
		        StringBuilder directory = stringBuilder.delete(0,endIndex);  
		     	String resource = directory.toString(); 
				logger.info("Resource: "+ resource);
             
				IZosUNIXFile bundleFile = this.zosFileHandler.newUNIXFile(resourceDefinitionBundledir + resource, cicsZosImage);
                if (!bundleFile.exists()) {
                	logger.info("Setting permissions");
                    bundleFile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
                }
                if (componentType.isBinaryBundleResource()) {
                	logger.info("Copying binary file");
                	bundleFile.storeBinary(IOUtils.toByteArray(this.testBundleResources.retrieveSkeletonFile(entry.getKey(), this.parameters),this.skeletonType));
                } else {
                	//Convert the byte array to String and substitute the variables
                	logger.info("Copying text file");
                	String s = new String(IOUtils.toByteArray(this.testBundleResources.retrieveSkeletonFile(entry.getKey(), this.parameters),this.skeletonType), StandardCharsets.UTF_8);
                	bundleFile.storeText(s);
                }
			}
		} catch (Exception e) {
			logger.debug("Failure in copying bundle files to zFS");
			e.printStackTrace();
		} 
		logger.info("Exiting copy of bundle files");
	}
	
	@Override
	public void setDefinitionDescriptionAttribute(String value) {
		this.resourceDefinitionDescription = value;
	}

	@Override
	public void setDefinitionStatusAttribute(CicsResourceStatus value) {
		this.resourceDefinitionStatus = value;
	}

    @Override
    public String getResourceDefinitionNameAttribute() {
        return this.resourceDefinitionName;
    }

    @Override
    public String getResourceDefinitionGroupAttribute() {
        return this.resourceDefinitionGroup;
    }

    @Override
    public String getResourceDefinitionDescriptionAttribute() {
        return this.resourceDefinitionDescription;
    }

    @Override
    public CicsResourceStatus getResourceDefinitionStatusAttribute() {
        return this.resourceDefinitionStatus;
    }

    @Override
    public String getResourceDefinitionBundledirAttribute() {
        return this.resourceDefinitionBundledir;
    }	

	@Override
	public void buildResourceDefinition() throws CicsBundleResourceException {
        try {
            if (resourceDefined()) {
                throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " " + getDefinitionName() + " already exists");
            }
            boolean setUcctran = false;
            if (this.cicsTerminal.isUppercaseTranslation() == true) {
            	this.cicsTerminal.setUppercaseTranslation(false);
            	setUcctran = true;
            }
            this.cicsRegion.ceda().createResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName(), getResourceDefinitionGroupAttribute(), buildResourceParameters());
            if (setUcctran) {
            	this.cicsTerminal.setUppercaseTranslation(true);
            }

            if (!resourceDefined()) {
                throw new CicsBundleResourceException("Failed to define " + RESOURCE_TYPE_BUNDLE + " resource definition");
            } 
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Unable to build " + RESOURCE_TYPE_BUNDLE + " resource definition", e);
        }
	}
	



	@Override
	public void buildInstallResourceDefinition() throws CicsBundleResourceException {
        buildResourceDefinition();
        if (this.needsCopying) {
        	deploy();
        }
        installResourceDefinition();
	}

	@Override
	public void installResourceDefinition() throws CicsBundleResourceException {
        try {
            if (resourceInstalled()) {
                throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " " + getDefinitionName() + " already installed");
            }
            this.cicsRegion.ceda().installResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName(), this.resourceDefinitionGroup);
            if (!resourceInstalled()) {
                throw new CicsBundleResourceException("Failed to install " + RESOURCE_TYPE_BUNDLE + " resource definition");
            }
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Unable to install " + RESOURCE_TYPE_BUNDLE + " resource definition", e);
        }
	}

	@Override
	public boolean resourceDefined() throws CicsBundleResourceException {
        try {
            return this.cicsRegion.ceda().resourceExists(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName(), resourceDefinitionGroup);
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Unable to display " + RESOURCE_TYPE_BUNDLE + " resource definition", e);
        }
	}

	@Override
	public boolean resourceInstalled() throws CicsBundleResourceException {
        try {
            return this.cicsRegion.cemt().inquireResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName()) != null;
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Unable to inquire " + RESOURCE_TYPE_BUNDLE + "", e);
        }
	}

	@Override
	public void enable() throws CicsBundleResourceException {
        try {
            if (!resourceInstalled()) {
                throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " " + getDefinitionName() + " does not exist");
            }
            this.cicsRegion.cemt().setResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName(), "ENABLED");
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Problem enabling " + RESOURCE_TYPE_BUNDLE + " " + getDefinitionName(), e);
        }
	}

	@Override
	public boolean waitForEnable() throws CicsBundleResourceException {
        return waitForEnable(getDefaultTimeout());
	}

	@Override
	public boolean waitForEnable(int timeout) throws CicsBundleResourceException {
        logger.trace("Waiting " + timeout + " second(s) for " + RESOURCE_TYPE_BUNDLE + " " + getDefinitionName() + " to be enabled");
	    LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            if (isEnabled()) {
                return true;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsBundleResourceException("Interrupted during wait", e);
            }
        }
        return isEnabled();
	}

	@Override
	public boolean isEnabled() throws CicsBundleResourceException {
        if (!resourceInstalled()) {
            return false;
        }
        boolean enabled = cemtInquire().isParameterEquals("enablestatus", CicsResourceStatus.ENABLED.toString());
        if (enabled) {
            logger.trace(RESOURCE_TYPE_BUNDLE + " " +  getDefinitionName() + " is enabled");
        } else {
            logger.trace(RESOURCE_TYPE_BUNDLE + " " +  getDefinitionName() + " is NOT enabled");
        }
        return enabled;
	}

	@Override
	public boolean disable() throws CicsBundleResourceException {
        try {
            if (!resourceInstalled()) {
                throw new CicsJvmserverResourceException(RESOURCE_TYPE_BUNDLE + " " + getDefinitionName() + " does not exist");
            }
            this.cicsRegion.cemt().setResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName(), "DISABLED");
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Problem disabling " + RESOURCE_TYPE_BUNDLE + " " + getDefinitionName(), e);
        }
        return isEnabled();
	}

	@Override
	public boolean waitForDisable() throws CicsBundleResourceException {
        return waitForDisable(getDefaultTimeout());
	}

	@Override
	public boolean waitForDisable(int timeout) throws CicsBundleResourceException {
        logger.trace("Waiting " + timeout + " second(s) for " + RESOURCE_TYPE_BUNDLE + " " +  getDefinitionName() + " to be disabled");
	    LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            if (!isEnabled()) {
                return true;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsBundleResourceException("Interrupted during wait", e);
            }
        }
        if (isEnabled()) {
            throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " " + getDefinitionName() + " not disabled in " + timeout + " second(s)");
        }
        return true;
	}

	@Override
	public boolean disableDiscardInstall() throws CicsBundleResourceException {
		return disableDiscardInstall(getDefaultTimeout());
	}

	@Override
	public boolean disableDiscardInstall(int timeout) throws CicsBundleResourceException {
		disable();
		waitForDisable(timeout);
		discard();
		installResourceDefinition();
		return waitForEnable();
	}

	@Override
	public void delete() throws CicsBundleResourceException {
        try {
            if (resourceDefined()) {
                this.cicsRegion.ceda().deleteResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName(), resourceDefinitionGroup);
            }
        } catch (CicstsManagerException e) {
        	throw new CicsBundleResourceException("Problem deleteing " + RESOURCE_TYPE_BUNDLE + " " + getDefinitionName(), e);
        }
	}

	@Override
	public void discard() throws CicsBundleResourceException {
        try {
            if (resourceInstalled()) {
                this.cicsRegion.cemt().discardResource(cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName());
	            if (resourceInstalled()) {
	            	throw new CicsBundleResourceException(RESOURCE_TYPE_BUNDLE + " was not discarded" + getDefinitionName());
	            }
            }
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Problem discarding " + RESOURCE_TYPE_BUNDLE + " " + getDefinitionName(), e);
        }
	}

	@Override
	public void disableDiscardDelete() throws CicsBundleResourceException {
        disable();
        waitForDisable();
        discard();
        delete();
	}

	@Override
	public void build() throws CicsBundleResourceException {
        try {
            if (this.needsCopying) {
            	try {
					copyBundleFilesToZfs();
				} catch (ZosUNIXFileException | IOException e) {
					throw new CicsBundleResourceException("Problem copying bundle files to zFS", e);
				}
            }
            buildResourceDefinition();
            installResourceDefinition();
        } catch (CicsBundleResourceException e) {
            throw new CicsBundleResourceException("Problem building " + RESOURCE_TYPE_BUNDLE + " " + getDefinitionName(), e);
        }
	}	

	@Override
	public String getDefinitionName() {
		return this.resourceDefinitionName;
	}

	@Override
    public String toString() {
        return "[CICS Bundle] " + getDefinitionName();
    }

    protected int getDefaultTimeout() throws CicsBundleResourceException {
        if (this.defaultTimeout == -1) {
            try {
                this.defaultTimeout = DefaultResourceTimeout.get(this.cicsZosImage);
            } catch (CicsResourceManagerException e) {
                throw new CicsBundleResourceException("Problem creating getting default resource timeout", e);
            }
        }
        return this.defaultTimeout;
    }

    protected String buildResourceParameters() {
        StringBuilder resourceParameters = new StringBuilder();
        appendNotNull(resourceParameters, "DESCRIPTION", getResourceDefinitionDescriptionAttribute());
        appendNotNull(resourceParameters, "STATUS", getResourceDefinitionStatusAttribute().toString());
        appendNotNull(resourceParameters, "BUNDLEDIR", getResourceDefinitionBundledirAttribute());
        return resourceParameters.toString();
    }

    protected StringBuilder appendNotNull(StringBuilder resourceParameters, String attribute, String value) {
        if (value != null && !value.isEmpty()) {
            resourceParameters.append(attribute);
            resourceParameters.append(" (");
            resourceParameters.append(value);
            resourceParameters.append(") ");
        }
        return resourceParameters;
    }

    protected CicstsHashMap cemtInquire() throws CicsBundleResourceException {
        if (!resourceInstalled()) {
            throw new CicsBundleResourceException("CICS Bundle " + getDefinitionName() + " does not exist");
        }
        CicstsHashMap cemtMap;
        try {
            cemtMap = this.cicsRegion.cemt().inquireResource(this.cicsTerminal, RESOURCE_TYPE_BUNDLE, getDefinitionName());
        } catch (CicstsManagerException e) {
            throw new CicsBundleResourceException("Problem inquiring CICS Bundle " + getDefinitionName(), e);
        }
        return cemtMap;
    }
	
	protected void cleanup() {
        try {
            if (!resourceInstalled()) {
                logger.info(RESOURCE_TYPE_BUNDLE + " " + getDefinitionName() + " has not been installed");
            } else {
                try {
                    disable();
                    waitForDisable();
                } catch (CicsBundleResourceException e) {
                    logger.error("Problem in cleanup phase", e);
                }
                try {
                    discard();
                } catch (CicsBundleResourceException e) {
                    logger.error("Problem in cleanup phase", e);
                }
            }
        } catch (CicstsManagerException e) {
            logger.error("Problem in cleanup phase", e);
        }
        try {
            delete();
        } catch (CicsBundleResourceException e) {
            logger.error("Problem in cleanup phase", e);
        }
    }


	@Override
	public void deploy() throws CicsBundleResourceException {
		if (!this.needsCopying) {
			throw new CicsBundleResourceException("The CICS bundle content was not supplied when the ICicsBundle was created");
		}
        try {
        	copyBundleFilesToZfs();
        } catch (ZosUNIXFileException e) {
            throw new CicsBundleResourceException("Problem deploying CICS bundle to zOS UNIX file system", e);
        } catch (IOException e) {
			logger.debug("Unexpected error deploying CICS Bundle");
			e.printStackTrace();
		}
	}

	

	
	
	
}
