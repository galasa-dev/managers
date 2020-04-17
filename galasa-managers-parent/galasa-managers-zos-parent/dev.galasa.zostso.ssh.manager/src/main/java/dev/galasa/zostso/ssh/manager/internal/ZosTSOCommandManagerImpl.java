/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso.ssh.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zostso.IZosTSO;
import dev.galasa.zostso.ZosTSO;
import dev.galasa.zostso.ZosTSOCommandManagerException;
import dev.galasa.zostso.ZosTSOField;
import dev.galasa.zostso.spi.IZosTSOSpi;
import dev.galasa.zostso.ssh.manager.internal.properties.ZosTSOCommandSshPropertiesSingleton;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;
import dev.galasa.zosunix.spi.IZosUNIXSpi;

/**
 * zOS TSO Command Manager implemented using ssh
 *
 */
@Component(service = { IManager.class })
public class ZosTSOCommandManagerImpl extends AbstractManager implements IZosTSOSpi {
    protected static final String NAMESPACE = "zostso";

    protected static IZosManagerSpi zosManager;
    public static void setZosManager(IZosManagerSpi zosManager) {
        ZosTSOCommandManagerImpl.zosManager = zosManager;
    }

    protected static IZosUNIXSpi zosUnixCommandManager;
    public static void setZosUnixCommandManager(IZosUNIXSpi zosUnixCommandManager) {
        ZosTSOCommandManagerImpl.zosUnixCommandManager = zosUnixCommandManager;
    }

    private final HashMap<String, ZosTSOImpl> taggedZosTSOs = new HashMap<>();
    private final HashMap<IZosImage, ZosTSOImpl> zosTSOs = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);
        try {
            ZosTSOCommandSshPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosTSOCommandManagerException("Unable to request framework services", e);
        }

        //*** Check to see if any of our annotations are present in the test class
        //*** If there is,  we need to activate
        List<AnnotatedField> ourFields = findAnnotatedFields(ZosTSOField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers);
        }
    }
    

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosTSOField.class);
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
            throw new ZosTSOCommandManagerException("The zOS Manager is not available");
        }
        setZosUnixCommandManager(addDependentManager(allManagers, activeManagers, IZosUNIXSpi.class));
        if (zosUnixCommandManager == null) {
            throw new ZosUNIXCommandManagerException("The zOS UNIX Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi || otherManager instanceof IZosUNIXSpi;
    }

    @GenerateAnnotatedField(annotation=ZosTSO.class)
    public IZosTSO generateZosTSO(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosTSO annotationZosTSO = field.getAnnotation(ZosTSO.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosTSO.imageTag(), "primary");

        //*** Have we already generated this tag
        if (this.taggedZosTSOs.containsKey(tag)) {
            return this.taggedZosTSOs.get(tag);
        }

        IZosImage image = zosManager.getImageForTag(tag);
        IZosTSO zosTSO = new ZosTSOImpl(image);
        this.taggedZosTSOs.put(tag, (ZosTSOImpl) zosTSO);
        
        return zosTSO;
    }


    @Override
    public @NotNull IZosTSO getZosTSO(IZosImage image) throws ZosTSOCommandManagerException {
        if (this.zosTSOs.containsKey(image)) {
            return this.zosTSOs.get(image);
        }

        ZosTSOImpl zosTSO = new ZosTSOImpl(image);
        this.zosTSOs.put(image, zosTSO);
        
        return zosTSO;
    }
}
