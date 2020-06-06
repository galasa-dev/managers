package dev.galasa.cicsts.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.CicstsManagerField;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.internal.dse.DseProvisioningImpl;
import dev.galasa.cicsts.internal.properties.CicstsPropertiesSingleton;
import dev.galasa.cicsts.internal.properties.ExtraBundles;
import dev.galasa.cicsts.internal.properties.ProvisionType;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.cicsts.spi.ICicsRegionProvisioner;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.zos.spi.IZosManagerSpi;

@Component(service = { IManager.class })
public class CicstsManagerImpl extends AbstractManager implements ICicstsManagerSpi {
    protected static final String NAMESPACE = "cicsts";

    private static final Log logger = LogFactory.getLog(CicstsManagerImpl.class);
    private boolean required;

    private IZosManagerSpi zosManager;

    private IDynamicStatusStoreService dss;
    
    private HashMap<String, ICicsRegionProvisioned> provisionedCicsRegions = new HashMap<>();

    private ArrayList<ICicsRegionProvisioner> provisioners = new ArrayList<>();

    private String provisionType;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);        
        //*** Check to see if any of our annotations are present in the test class
        //*** If there is,  we need to activate
        List<AnnotatedField> ourFields = findAnnotatedFields(CicstsManagerField.class);
        if (ourFields.isEmpty() && !required) {
            return;
        }

        youAreRequired(allManagers, activeManagers);

        try {
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
        } catch (Exception e) {
            throw new CicstsManagerException("Unable to request framework services", e);
        }

        this.provisionType = ProvisionType.get();
        this.provisioners.add(new DseProvisioningImpl(this));
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        this.required = true;
        activeManagers.add(this);

        this.zosManager = addDependentManager(allManagers, activeManagers, IZosManagerSpi.class);
        if (this.zosManager == null) {
            throw new CicstsManagerException("Unable to locate the zOS Manager, required for the CICS TS Manager");
        }
    }

    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws ManagerException {
        try {
            CicstsPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Unable to request framework services", e);
        }

        return ExtraBundles.get();
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        // We need zos to provision first
        if (this.zosManager == otherManager) {
            return true;
        }

        return false;
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        //*** Auto generate the fields
        generateAnnotatedFields(CicstsManagerField.class);
    }
    
    @GenerateAnnotatedField(annotation=CicsRegion.class)
    public ICicsRegion generateCicsRegion(Field field, List<Annotation> annotations) throws ManagerException {
        CicsRegion annotationCics = field.getAnnotation(CicsRegion.class);

        String tag = defaultString(annotationCics.cicsTag(), "PRIMARY").toUpperCase();
        
        // Have we already got it
        ICicsRegionProvisioned region = this.provisionedCicsRegions.get(tag);
        if (region != null) {
            return region;
        }

        for(ICicsRegionProvisioner provisioner : provisioners) {
            ICicsRegionProvisioned newRegion = provisioner.provision(tag, annotationCics.imageTag(), annotations);
            if (newRegion != null) {
                this.provisionedCicsRegions.put(tag, newRegion);
                return newRegion;
            }
        }

        throw new CicstsManagerException("Unable to provision CICS Region tagged " + tag);
    }

    @Override
    public void registerProvisioner(ICicsRegionProvisioner provisioner) {
        // TODO Auto-generated method stub

    }

    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

	public String getProvisionType() {
		return this.provisionType;
	}

}