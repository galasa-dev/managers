/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.cicsts.resource.internal;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.cicsts.spi.ICicsResourceProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.spi.IZosLibertySpi;

@Component(service = { IManager.class })
public class CicsResourceManagerImpl extends AbstractManager implements ICicsResourceProvider {
    
    protected static final String NAMESPACE = "cicsresource";
    private ICicstsManagerSpi cicstsManager;    
    private IZosBatchSpi zosBatchManager;    
    private IZosFileSpi zosFileManager;
	private IZosLibertySpi zosLibertyManager;    
	private ITextScannerManagerSpi textScannerManager;
	
	private HashMap<ICicsRegion, ICicsResource> regionCicsResources = new HashMap<>();
	/* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            youAreRequired(allManagers, activeManagers, galasaTest);
        }
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        this.cicstsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
        if(cicstsManager == null) {
           throw new CicstsManagerException("The CICS Manager is not available");
        }
        this.zosBatchManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
        if (this.zosBatchManager == null) {
            throw new CicstsManagerException("The zOS Batch Manager is not available");
        }
        this.zosFileManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosFileSpi.class);
        if (this.zosFileManager == null) {
            throw new CicstsManagerException("The zOS File Manager is not available");
        }
        this.zosLibertyManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosLibertySpi.class);
        if (this.zosLibertyManager == null) {
            throw new CicstsManagerException("The zOS Liberty Manager is not available");
        }
        this.textScannerManager = addDependentManager(allManagers, activeManagers, galasaTest, ITextScannerManagerSpi.class);
        if (this.textScannerManager == null) {
            throw new CicstsManagerException("The Text Scan Manager is not available");
        }
        
        cicstsManager.registerCicsResourceProvider(this);
    }

	@Override
	public @NotNull ICicsResource getCicsResource(ICicsRegion cicsRegion) throws CicsResourceManagerException {
		ICicsResource cicsResources = this.regionCicsResources.get(cicsRegion);
        if (cicsResources == null) {
            cicsResources = new CicsResourceImpl(this, cicsRegion);
            this.regionCicsResources.put(cicsRegion, cicsResources);
        }
        return cicsResources;
	}
	
	protected ICicstsManagerSpi getCicsManager() {
		return this.cicstsManager;
	}
	
	protected IZosFileHandler getZosFileHandler() throws CicsResourceManagerException {
		try {
			return this.zosFileManager.getZosFileHandler();
		} catch (ZosFileManagerException e) {
			throw new CicsResourceManagerException("Problem getting IZosFileHandler", e);
		}
	}
	
	protected IZosBatch getBatch(IZosImage zosImage) {
		return this.zosBatchManager.getZosBatch(zosImage);
	}
	
	protected IZosLiberty getZosLiberty() throws CicsResourceManagerException {
		try {
			return this.zosLibertyManager.getZosLiberty();
		} catch (ZosLibertyManagerException e) {
			throw new CicsResourceManagerException("Problem getting IZosLiberty", e);
		}
	}
	
	protected ILogScanner getLogScanner() throws CicsResourceManagerException {
		try {
			return this.textScannerManager.getLogScanner();
		} catch (TextScanManagerException e) {
			throw new CicsResourceManagerException("Problem getting ILogScanner", e);
		}
	}
}
