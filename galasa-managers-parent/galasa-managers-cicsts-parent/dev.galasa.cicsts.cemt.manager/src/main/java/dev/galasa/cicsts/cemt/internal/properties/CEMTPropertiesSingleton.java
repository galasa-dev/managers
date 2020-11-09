package dev.galasa.cicsts.cemt.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.cicsts.cemt.CEMTManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=CEMTPropertiesSingleton.class, immediate=true)
public class CEMTPropertiesSingleton {
  
   private static CEMTPropertiesSingleton singletonInstance;
   private static void setInstance(CEMTPropertiesSingleton instance) {
      singletonInstance = instance;
   }
   
   private IConfigurationPropertyStoreService cps;
   
   @Activate
   public void activate() {
      setInstance(this);
   }
   
   @Deactivate
   public void deactivate() {
      setInstance(null);
   }
   
   public static IConfigurationPropertyStoreService cps() throws CEMTManagerException{
      if(singletonInstance != null) {
         return singletonInstance.cps;
      }
      
      throw new CEMTManagerException("Attempt to access manager cps before it has been initialised");
   }
   
   public static void setCps(IConfigurationPropertyStoreService cps) throws CEMTManagerException{
      if(singletonInstance != null) {
         singletonInstance.cps = cps;
         return;
      }
      
      throw new CEMTManagerException("Attempt to set manager cps before instance created");
   }
}
