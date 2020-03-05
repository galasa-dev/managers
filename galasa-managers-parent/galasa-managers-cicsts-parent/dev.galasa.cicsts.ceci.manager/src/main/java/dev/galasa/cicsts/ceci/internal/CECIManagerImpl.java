/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.cicsts.ceci.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.ceci.CECI;
import dev.galasa.cicsts.ceci.CECIManagerException;
import dev.galasa.cicsts.ceci.ICECI;
import dev.galasa.cicsts.ceci.internal.properties.CECIPropertiesSingleton;
import dev.galasa.cicsts.ceci.spi.spi.ICECIManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class CECIManagerImpl extends AbstractManager implements ICECIManagerSpi {
    
    protected static final String NAMESPACE = "cicsts";
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);
        try {
            CECIPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new CECIManagerException("Unable to request framework services", e);
        }

        //*** Check to see if any of our annotations are present in the test class
        //*** If there is,  we need to activate
        List<AnnotatedField> ourFields = findAnnotatedFields(CECIManagerField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers);
        }
    }

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(CECIManagerField.class);
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
    
    @GenerateAnnotatedField(annotation=CECI.class)
    public ICECI generateCECI(Field field, List<Annotation> annotations) {
        return new CECIImpl();
    }
}
