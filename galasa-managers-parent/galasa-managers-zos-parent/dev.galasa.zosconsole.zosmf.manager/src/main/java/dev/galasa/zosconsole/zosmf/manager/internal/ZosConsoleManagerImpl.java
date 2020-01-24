/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsole;
import dev.galasa.zosconsole.ZosConsoleField;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosmf.spi.IZosmfManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.zosconsole.zosmf.manager.internal.properties.ZosConsoleZosmfPropertiesSingleton;

/**
 * zOS Console Manager implemented using zOS/MF
 *
 */
@Component(service = { IManager.class })
public class ZosConsoleManagerImpl extends AbstractManager {
    protected static final String NAMESPACE = "zosconsole";

    protected static IZosManagerSpi zosManager;
    public static void setZosManager(IZosManagerSpi zosManager) {
        ZosConsoleManagerImpl.zosManager = zosManager;
    }
    
    protected static IZosmfManagerSpi zosmfManager;
    public static void setZosmfManager(IZosmfManagerSpi zosmfManager) {
        ZosConsoleManagerImpl.zosmfManager = zosmfManager;
    }

    private final HashMap<String, ZosConsoleImpl> taggedZosConsoles = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);
        try {
            ZosConsoleZosmfPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosConsoleManagerException("Unable to request framework services", e);
        }

        //*** Check to see if any of our annotations are present in the test class
        //*** If there is,  we need to activate
        List<AnnotatedField> ourFields = findAnnotatedFields(ZosConsoleField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers);
        }
    }
    

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosConsoleField.class);
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        setZosManager(addDependentManager(allManagers, activeManagers, IZosManagerSpi.class));
        if (zosManager == null) {
            throw new ZosConsoleManagerException("The zOS Manager is not available");
        }
        setZosmfManager(addDependentManager(allManagers, activeManagers, IZosmfManagerSpi.class));
        if (zosmfManager == null) {
            throw new ZosConsoleManagerException("The zOSMF Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework
     * .spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi ||
               otherManager instanceof IZosmfManagerSpi;
    }

    @GenerateAnnotatedField(annotation=ZosConsole.class)
    public IZosConsole generateZosConsole(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosConsole annotationZosConsole = field.getAnnotation(ZosConsole.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosConsole.imageTag(), "primary");

        //*** Have we already generated this tag
        if (this.taggedZosConsoles.containsKey(tag)) {
            return this.taggedZosConsoles.get(tag);
        }

        IZosImage image = zosManager.getImageForTag(tag);
        IZosConsole zosConsole = new ZosConsoleImpl(image);
        this.taggedZosConsoles.put(tag, (ZosConsoleImpl) zosConsole);
        
        return zosConsole;
    }
}
