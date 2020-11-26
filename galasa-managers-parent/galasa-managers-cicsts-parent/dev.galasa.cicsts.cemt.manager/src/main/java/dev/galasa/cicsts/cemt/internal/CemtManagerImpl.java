/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.cicsts.cemt.internal;

import dev.galasa.framework.spi.AbstractManager;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICemt;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.cemt.spi.spi.ICemtManagerSpi;
import dev.galasa.cicsts.spi.ICemtProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;

@Component(service = { IManager.class })
public class CemtManagerImpl extends AbstractManager implements ICemtManagerSpi, ICemtProvider {
   
   protected static final String NAMESPACE = "cicsts";
   private ICicstsManagerSpi cicstsManager;
   private HashMap<ICicsRegion, ICemt> regionCemt = new HashMap<>();
   
   @Override
   public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
       super.initialise(framework, allManagers, activeManagers, galasaTest);
       
       if(galasaTest.isJava()) {
          youAreRequired(allManagers, activeManagers);
       }
   }
   
   
   @Override
   public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers) throws ManagerException{
      
      if(activeManagers.contains(this)) {
         return;
      }
      
      activeManagers.add(this);
      
      cicstsManager = addDependentManager(allManagers, activeManagers, ICicstsManagerSpi.class);
      
      if(cicstsManager == null) {
         throw new CicstsManagerException("CICS Manager is not available");
      }
      
      cicstsManager.registerCemtProvider(this);
      
   }


   @Override
   public @NotNull ICemt getCemt(ICicsRegion cicsRegion) {
      
      ICemt cemt = regionCemt.get(cicsRegion);
      
      if(cemt == null) {
         cemt = new CemtImpl(cicsRegion);
         regionCemt.put(cicsRegion, cemt);
      }
      
      return cemt;
      
   }
   
}
