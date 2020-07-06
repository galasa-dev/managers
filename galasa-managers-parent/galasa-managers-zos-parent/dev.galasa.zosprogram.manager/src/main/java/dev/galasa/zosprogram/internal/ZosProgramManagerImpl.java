/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;
import dev.galasa.zosprogram.internal.properties.ZosProgramPropertiesSingleton;
import dev.galasa.zosprogram.spi.IZosProgramManagerSpi;

@Component(service = { IManager.class })
public class ZosProgramManagerImpl extends AbstractManager implements IZosProgramManagerSpi {
    
    private static final Log logger = LogFactory.getLog(ZosProgramManagerImpl.class);
    
    protected static final String NAMESPACE = "zosprogram";
    
    protected static IZosManagerSpi zosManager;
    public static void setZosManager(IZosManagerSpi zosManager) {
        ZosProgramManagerImpl.zosManager = zosManager;
    }
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosProgramPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosProgramManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers);
            }
        }
    }
    

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        setZosManager(addDependentManager(allManagers, activeManagers, IZosManagerSpi.class));
        if (zosManager == null) {
            throw new ZosProgramManagerException("The zOS Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi;
    }
    
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosProgramManagerField.class);
    }
    
    @GenerateAnnotatedField(annotation=ZosProgram.class)
    public IZosProgram generateZosProgram(Field field, List<Annotation> annotations) throws ZosProgramManagerException {
        ZosProgram annotationZosProgram = field.getAnnotation(ZosProgram.class);

        String tag = defaultString(annotationZosProgram.imageTag(), "primary");
        String name = nulled(annotationZosProgram.name());
        Language language = annotationZosProgram.language();
        String loadlib = nulled(annotationZosProgram.loadlib());
        
        return new ZosProgramImpl(tag, name, language, loadlib);
    }


    @Override
    public IZosProgram newZosProgram(IZosImage image, String name, Language language, String loadlib) throws ZosProgramManagerException {
        return new ZosProgramImpl(image.getImageID(), name, language, loadlib);
    }
}
