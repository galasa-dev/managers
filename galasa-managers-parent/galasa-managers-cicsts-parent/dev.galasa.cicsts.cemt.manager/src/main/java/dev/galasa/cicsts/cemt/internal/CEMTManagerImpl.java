package dev.galasa.cicsts.cemt.internal;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.cemt.CEMT;
import dev.galasa.cicsts.cemt.CEMTManagerException;
import dev.galasa.cicsts.cemt.ICEMT;
import dev.galasa.cicsts.cemt.internal.properties.CEMTPropertiesSingleton;
import dev.galasa.cicsts.cemt.spi.spi.ICEMTManagerSpi;

@Component(service = { IManager.class })
public class CEMTManagerImpl extends AbstractManager implements ICEMTManagerSpi {
   
   protected static final String NAMESPACE = "cicsts";
   
   @Override
   public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
       super.initialise(framework, allManagers, activeManagers, galasaTest);
       try {
           CEMTPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
       } catch (ConfigurationPropertyStoreException e) {
           throw new CEMTManagerException("Unable to request framework services", e);
       }

       if(galasaTest.isJava()) {
           //*** Check to see if any of our annotations are present in the test class
           //*** If there is,  we need to activate
           List<AnnotatedField> ourFields = findAnnotatedFields(CEMTManagerField.class);
           if (!ourFields.isEmpty()) {
               youAreRequired(allManagers, activeManagers);
           }
       }
   }
   
   @Override
   public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers) throws ManagerException{
      if(activeManagers.contains(this)) {
         return;
      }
      
      activeManagers.add(this);
   }
   
   @GenerateAnnotatedField(annotation=CEMT.class)
   public ICEMT generateCEMT(Field field, List<Annotation> annotations) {
      return new CEMTImpl();
   }
   
}
