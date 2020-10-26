package dev.galasa.cicsts.ceda.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.ceda.CEDA;
import dev.galasa.cicsts.ceda.CEDAManagerException;
import dev.galasa.cicsts.ceda.ICEDA;
import dev.galasa.cicsts.ceda.internal.properties.CEDAPropertiesSingleton;
import dev.galasa.cicsts.ceda.spi.ICEDAManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class CEDAManagerImpl extends AbstractManager implements ICEDAManagerSpi {
    
    protected static final String NAMESPACE = "cicsts";
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            CEDAPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new CEDAManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
           //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(CEDAManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers);
            }
        }
    }

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(CEDAManagerField.class);
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
    }
    
    @GenerateAnnotatedField(annotation=CEDA.class)
    public ICEDA generateCEDA(Field field, List<Annotation> annotations) {
        return new CEDAImpl();
    }
}