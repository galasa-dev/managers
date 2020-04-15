/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix.ssh.manager.internal;

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
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosunix.IZosUNIX;
import dev.galasa.zosunix.ZosUNIX;
import dev.galasa.zosunix.ZosUNIXField;
import dev.galasa.zosunix.spi.IZosUNIXSpi;
import dev.galasa.zosunix.ZosUNIXCommandManagerException;
import dev.galasa.zosunix.ssh.manager.internal.properties.ZosUNIXCommandSshPropertiesSingleton;

/**
 * zOS UNIX Manager implemented using ssh
 *
 */
@Component(service = { IManager.class })
public class ZosUNIXCommandManagerImpl extends AbstractManager implements IZosUNIXSpi {
    protected static final String NAMESPACE = "zosunix";

    protected static IZosManagerSpi zosManager;
    public static void setZosManager(IZosManagerSpi zosManager) {
        ZosUNIXCommandManagerImpl.zosManager = zosManager;
    }

    protected static IIpNetworkManagerSpi ipNetworkManager;
    public static void setIpNetworkManager(IIpNetworkManagerSpi ipNetworkManager) {
        ZosUNIXCommandManagerImpl.ipNetworkManager = ipNetworkManager;
    }

    private final HashMap<String, ZosUNIXImpl> taggedZosUNIXs = new HashMap<>();
    private final HashMap<IZosImage, ZosUNIXImpl> zosUNIXs = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);
        try {
            ZosUNIXCommandSshPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosUNIXCommandManagerException("Unable to request framework services", e);
        }

        //*** Check to see if any of our annotations are present in the test class
        //*** If there is,  we need to activate
        List<AnnotatedField> ourFields = findAnnotatedFields(ZosUNIXField.class);
        if (!ourFields.isEmpty()) {
            youAreRequired(allManagers, activeManagers);
        }
    }
    

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosUNIXField.class);
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
            throw new ZosUNIXCommandManagerException("The zOS Manager is not available");
        }
        setIpNetworkManager(addDependentManager(allManagers, activeManagers, IIpNetworkManagerSpi.class));
        if (ipNetworkManager == null) {
            throw new ZosUNIXCommandManagerException("The IP Network Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi || otherManager instanceof IIpNetworkManagerSpi;
    }

    @GenerateAnnotatedField(annotation=ZosUNIX.class)
    public IZosUNIX generateZosUNIX(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosUNIX annotationZosUNIX = field.getAnnotation(ZosUNIX.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosUNIX.imageTag(), "primary");

        //*** Have we already generated this tag
        if (this.taggedZosUNIXs.containsKey(tag)) {
            return this.taggedZosUNIXs.get(tag);
        }

        IZosImage image = zosManager.getImageForTag(tag);
        IZosUNIX zosUNIX = new ZosUNIXImpl(image);
        this.taggedZosUNIXs.put(tag, (ZosUNIXImpl) zosUNIX);
        
        return zosUNIX;
    }


    @Override
    public @NotNull IZosUNIX getZosUNIX(IZosImage image) throws ZosUNIXCommandManagerException {
        //*** Have we already generated this image
        if (this.zosUNIXs.containsKey(image)) {
            return this.zosUNIXs.get(image);
        }

        IZosUNIX zosUNIX = new ZosUNIXImpl(image);
        this.zosUNIXs.put(image, (ZosUNIXImpl) zosUNIX);
        
        return zosUNIX;
    }
}
