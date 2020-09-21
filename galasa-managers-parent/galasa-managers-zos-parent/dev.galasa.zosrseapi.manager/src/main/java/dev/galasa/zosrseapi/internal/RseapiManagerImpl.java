/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.Rseapi;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.properties.RseapiPropertiesSingleton;
import dev.galasa.zosrseapi.internal.properties.ServerImages;
import dev.galasa.zosrseapi.spi.IRseapiManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class RseapiManagerImpl extends AbstractManager implements IRseapiManagerSpi {
    
    private static final Log logger = LogFactory.getLog(RseapiManagerImpl.class);
    
    protected static final String NAMESPACE = "rseapi";
    
    protected static IZosManagerSpi zosManager;
    public static void setZosManager(IZosManagerSpi zosManager) {
        RseapiManagerImpl.zosManager = zosManager;
    }

    protected static IHttpManagerSpi httpManager;    
    public static void setHttpManager(IHttpManagerSpi httpManager) {
        RseapiManagerImpl.httpManager = httpManager;
    }

    private final HashMap<String, IRseapi> taggedRseapis = new HashMap<>();
    private final HashMap<String, IRseapi> rseapis = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            RseapiPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(RseapiManagerField.class);
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
            throw new RseapiManagerException("The zOS Manager is not available");
        }
        setHttpManager(addDependentManager(allManagers, activeManagers, IHttpManagerSpi.class));
        if (httpManager == null) {
            throw new RseapiManagerException("The HTTP Manager is not available");
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
        return otherManager instanceof IZosManagerSpi ||
               otherManager instanceof IHttpManagerSpi;
    }
    
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(RseapiManagerField.class);
    }
    
    @GenerateAnnotatedField(annotation=Rseapi.class)
    public IRseapi generateRseapi(Field field, List<Annotation> annotations) throws RseapiManagerException {
        Rseapi annotationRseapi = field.getAnnotation(Rseapi.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationRseapi.imageTag(), "primary");

        //*** Have we already generated this tag
        if (taggedRseapis.containsKey(tag)) {
            return taggedRseapis.get(tag);
        }

        RseapiImpl rseapi = new RseapiImpl(tag);
        taggedRseapis.put(tag, rseapi);
        rseapis.put(rseapi.getImage().getImageID(), rseapi);
        
        return rseapi;
    }


    @Override
    public IRseapi newRseapi(IZosImage image) throws RseapiException {
        if (rseapis.containsKey(image.getImageID())) {
            return this.rseapis.get(image.getImageID());
        }
        IRseapi rseapi = new RseapiImpl(image);
        this.rseapis.put(image.getImageID(), rseapi);
        return rseapi;
    }


    public Map<String, IRseapi> getRseapis(@NotNull String clusterId) throws RseapiManagerException {
        try {
            for (String imageId : ServerImages.get(clusterId)) {
                if (!this.rseapis.containsKey(imageId)) {
                    logger.info("Requesting zOS image " + imageId + " in cluster \"" + clusterId + "\" for RSE API server");
                    IZosImage rseapiImage = getImage(imageId, clusterId);
                    this.rseapis.put(rseapiImage.getImageID(), newRseapi(rseapiImage));
                }
            }
        } catch (ZosManagerException e) {
            throw new RseapiManagerException("Unable to get RSE API servers for cluster \"" + clusterId + "\"", e);
        }
        if (rseapis.isEmpty()) {
            throw new RseapiManagerException("No RSE API servers defined for cluster \"" + clusterId + "\"");
        }
        return rseapis;
    }


    @Override
    public IRseapiRestApiProcessor newRseapiRestApiProcessor(IZosImage image, boolean restrictToImage) throws RseapiManagerException {
        if (restrictToImage) {
            HashMap<String, IRseapi> rseapiMap = new HashMap<>();
            IRseapi rseapi = this.rseapis.get(image.getImageID());
            if (rseapi == null) {
                throw new RseapiManagerException("No RSE API sever configured on " + image.getImageID());
            }
            rseapiMap.put(image.getImageID(), rseapi);
            return new RseapiRestApiProcessor(rseapiMap);
        }
        return new RseapiRestApiProcessor(getRseapis(image.getClusterID()));
    }


    protected IZosImage getImage(String imageId, String clusterId) throws RseapiManagerException {
        IZosImage rseapiImage;
        try {
            rseapiImage = zosManager.getImage(imageId);
        } catch (ZosManagerException e) {
            throw new RseapiManagerException("Unable to get RSE API server zOS image \"" + imageId + "\" in cluster \"" + clusterId + "\"", e);
        }
        return rseapiImage;
    }


}
