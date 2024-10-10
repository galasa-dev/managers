/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.tsq.internal;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.TsqException;
import dev.galasa.cicsts.TsqManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ITsqFactory;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.tsq.internal.properties.TsqPropertiesSingleton;
import dev.galasa.cicsts.tsq.spi.ITsqManagerSpi;
import dev.galasa.cicsts.spi.ITsqProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class TsqManagerImpl extends AbstractManager implements ITsqManagerSpi, ITsqProvider {
    
    protected static final String NAMESPACE = "tsq";
    private ICicstsManagerSpi cicstsManager;
    
    protected HashMap<ICicsRegion, ITsqFactory> regionTsqs = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            TsqPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new TsqManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            youAreRequired(allManagers, activeManagers, galasaTest);
        }
    }

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
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
        
        cicstsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
        if(cicstsManager == null) {
           throw new CicstsManagerException("CICS Manager is not available");
        }
        
        cicstsManager.registerTsqProvider(this);

    }
    
    // Get ITsqFactory instance for the CICS Region
    @Override
    public @NotNull ITsqFactory getTsqFactory(ICicsRegion cicsRegion, ICicstsManagerSpi cicstsManager) throws TsqManagerException{
        ITsqFactory tsq = this.regionTsqs.get(cicsRegion);
        if (tsq == null) {
            try{
                tsq = new TsqFactoryImpl(this, cicsRegion, cicstsManager);
                this.regionTsqs.put(cicsRegion, tsq);    
            } catch (TsqException e) {
                throw new TsqManagerException("TSQ instance implemenation failed. ", e);
            }
        }
        return tsq;
    }
}
