/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.openqa.selenium.WebDriver;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManager;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.SeleniumManagerField;
import dev.galasa.selenium.internal.properties.SeleniumDseInstanceName;
import dev.galasa.selenium.internal.properties.SeleniumPropertiesSingleton;

@Component(service = { IManager.class })
public class SeleniumManagerImpl extends AbstractManager implements ISeleniumManager {

    public static final String NAMESPACE = "selenium";

    private IConfigurationPropertyStoreService cps; //NOSONAR

    private List<WebPageImpl> webPages = new ArrayList<>();

    private boolean required = false;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(SeleniumManagerField.class);
            if (ourFields.isEmpty() && !this.required) {
                return;
            }
            youAreRequired(allManagers, activeManagers);
        }

        try {
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            SeleniumPropertiesSingleton.setCps(cps);
        } catch (Exception e) {
            throw new SeleniumManagerException("Unable to request framework services", e);
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        this.required = true;

        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(SeleniumManagerField.class);
    }

    @GenerateAnnotatedField(annotation = SeleniumManager.class)
    public ISeleniumManager generateSeleniumManager(Field field, List<Annotation> annotations) {
        return this;
    }

    @Override
    public void provisionDiscard() {
        for(WebPageImpl page : webPages) {
            page.managerQuit();
        }
    }

    @Override
    public IWebPage allocateWebPage() throws SeleniumManagerException {
        return allocateWebPage(null);
    }

    @Override
    public IWebPage allocateWebPage(String url) throws SeleniumManagerException {

        WebDriver driver = null;

        try {
            String dseInstance = SeleniumDseInstanceName.get();
            driver = Browser.getWebDriver(dseInstance);

            if(driver == null)
                throw new SeleniumManagerException("Unsupported driver type for instance: " + dseInstance);
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        WebPageImpl webPage = new WebPageImpl(driver, webPages);

        if(url != null && !url.trim().isEmpty())
            webPage.get(url);
        
        this.webPages.add(webPage);
        return webPage;
    }
    
}