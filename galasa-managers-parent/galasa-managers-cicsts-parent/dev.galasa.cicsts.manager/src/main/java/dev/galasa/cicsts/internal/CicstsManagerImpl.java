/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
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
import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.CicstsManagerField;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.internal.dse.DseProvisioningImpl;
import dev.galasa.cicsts.internal.properties.CicstsPropertiesSingleton;
import dev.galasa.cicsts.internal.properties.DefaultVersion;
import dev.galasa.cicsts.internal.properties.ExtraBundles;
import dev.galasa.cicsts.internal.properties.ProvisionType;
import dev.galasa.cicsts.spi.CicsTerminalImpl;
import dev.galasa.cicsts.spi.ICicsRegionLogonProvider;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.cicsts.spi.ICicsRegionProvisioner;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zos3270.TerminalInterruptedException;

@Component(service = { IManager.class })
public class CicstsManagerImpl extends AbstractManager implements ICicstsManagerSpi {
    protected static final String NAMESPACE = "cicsts";

    private static final Log logger = LogFactory.getLog(CicstsManagerImpl.class);
    private boolean required;

    private IZosManagerSpi zosManager;

    private final HashMap<String, ICicsRegionProvisioned> provisionedCicsRegions = new HashMap<>();

    private final ArrayList<ICicsRegionProvisioner> provisioners = new ArrayList<>();
    private final ArrayList<CicsTerminalImpl> terminals = new ArrayList<>();
    private final ArrayList<ICicsRegionLogonProvider> logonProviders = new ArrayList<>();

    private String provisionType;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        // *** Check to see if any of our annotations are present in the test class
        // *** If there is, we need to activate
        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(CicstsManagerField.class);
            if (ourFields.isEmpty() && !required) {
                return;
            }

            youAreRequired(allManagers, activeManagers);
        }
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

        this.provisionType = ProvisionType.get();
        this.provisioners.add(new DseProvisioningImpl(this));
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
        if (this.zosManager == otherManager) { // NOSONAR - ignore return single statement rule as will prob need other managers soon
            return true;
        }

        return false;
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        // First, give the provisioners the opportunity to provision CICS regions
        for (ICicsRegionProvisioner provisioner : provisioners) {
            provisioner.cicsProvisionGenerate();
        }

        // Now provision all the individual annotations 

        List<AnnotatedField> annotatedFields = findAnnotatedFields(CicstsManagerField.class);

        for (AnnotatedField annotatedField : annotatedFields) {
            final Field field = annotatedField.getField();

            if (field.getType() == ICicsRegion.class) {
                CicsRegion annotation = field.getAnnotation(CicsRegion.class);
                if (annotation != null) {
                    ICicsRegion cicsRegion = generateCicsRegion(field, annotatedField.getAnnotations());
                    registerAnnotatedField(field, cicsRegion);
                }
            }
        }

        // *** Auto generate the fields
        generateAnnotatedFields(CicstsManagerField.class);
    }

    /**
     * Not using the auto generate as we need all the CICS Regions generated before
     * any other annotated field
     */
    public ICicsRegion generateCicsRegion(Field field, List<Annotation> annotations) throws ManagerException {
        CicsRegion annotationCics = field.getAnnotation(CicsRegion.class);

        String tag = defaultString(annotationCics.cicsTag(), "PRIMARY").toUpperCase();

        // Have we already got it
        ICicsRegionProvisioned region = this.provisionedCicsRegions.get(tag);
        if (region != null) {
            return region;
        }

        for (ICicsRegionProvisioner provisioner : provisioners) {
            ICicsRegionProvisioned newRegion = provisioner.provision(tag, annotationCics.imageTag(), annotations);
            if (newRegion != null) {
                this.provisionedCicsRegions.put(tag, newRegion);
                return newRegion;
            }
        }

        throw new CicstsManagerException("Unable to provision CICS Region tagged " + tag);
    }

    @GenerateAnnotatedField(annotation = CicsTerminal.class)
    public ICicsTerminal generateCicTerminal(Field field, List<Annotation> annotations) throws ManagerException {
        CicsTerminal annotation = field.getAnnotation(CicsTerminal.class);

        String tag = defaultString(annotation.cicsTag(), "PRIMARY").toUpperCase();

        ICicsRegionProvisioned region = this.provisionedCicsRegions.get(tag);
        if (region == null) {
            throw new CicstsManagerException("Unable to setup CICS Terminal for field " + field.getName()
            + ", tagged region " + tag + " was not provisioned");
        }

        try {
            CicsTerminalImpl newTerminal = new CicsTerminalImpl(this, getFramework(), region);
            this.terminals.add(newTerminal);
            return newTerminal;
        } catch (TerminalInterruptedException e) {
            throw new CicstsManagerException(
                    "Unable to setup CICS Terminal for field " + field.getName() + ", tagged region " + tag, e);
        }
    }

    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {

        // Add the default Logon Provider incase one isn't supplied
        this.logonProviders.add(new CicstsDefaultLogonProvider());

        // Start the CICS Regions

        // Start the autoconnect terminals
        logger.info("Connecting CICS Terminals");
        for (ICicsTerminal terminal : this.terminals) {
            try {
                terminal.connectToCicsRegion();
            } catch (CicstsManagerException e) {
                throw new CicstsManagerException("Failed to connect to the " + terminal.getCicsRegion(), e);
            }
        }
    }

    @Override
    public void provisionStop() {
        for (CicsTerminalImpl terminal : this.terminals) {
            try {
                terminal.disconnect();
            } catch (TerminalInterruptedException e) { // NOSONAR - wish to hide disconnect errors
            }
        }
    }

    @Override
    public void registerProvisioner(ICicsRegionProvisioner provisioner) {
        if (this.provisioners.contains(provisioner)) {
            return;
        }

        this.provisioners.add(provisioner);
    }

    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

    public String getProvisionType() {
        return this.provisionType;
    }

    @Override
    @NotNull
    public List<ICicsRegionLogonProvider> getLogonProviders() {
        return new ArrayList<>(this.logonProviders);
    }

    @Override
    public @NotNull ProductVersion getDefaultVersion() {
        return DefaultVersion.get();
    }

}