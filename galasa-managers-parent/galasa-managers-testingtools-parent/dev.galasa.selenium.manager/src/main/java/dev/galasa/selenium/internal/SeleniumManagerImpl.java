/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium.internal;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.openqa.selenium.WebDriver;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManager;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.internal.properties.SeleniumDseInstanceName;
import dev.galasa.selenium.internal.properties.SeleniumPropertiesSingleton;

@Component(service = { IManager.class })
public class SeleniumManagerImpl extends AbstractManager implements ISeleniumManager {

    public final static String NAMESPACE = "selenium";

    private IConfigurationPropertyStoreService cps;

    private Map<String, IWebPage> taggedPages = new HashMap<String, IWebPage>();
    private List<IWebPage> webPages = new ArrayList<IWebPage>();

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        List<AnnotatedField> ourFields = findAnnotatedFields(SeleniumManagerField.class);
        if (!ourFields.isEmpty()) {
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
        for(IWebPage page : webPages) {
            page.quit();
        }
    }

    @Override
    public IWebPage allocateWebPage() throws SeleniumManagerException {
        return allocateWebPage(null, null);
    }

    @Override
    public IWebPage allocateWebPage(String url) throws SeleniumManagerException {
        return allocateWebPage(url, null);
    }

    @Override
    public IWebPage allocateWebPage(String url, String tag) throws SeleniumManagerException {
        WebDriver driver = null;

        try {
            String dseInstance = SeleniumDseInstanceName.get();
            driver = Browser.getWebDriver(dseInstance);

            if(driver == null)
                throw new SeleniumManagerException("Unsupported driver type for instance: " + dseInstance);
        } catch (ConfigurationPropertyStoreException | SeleniumManagerException | IOException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        IWebPage webPage = new WebPageImpl(driver);

        if(tag != null && !tag.trim().isEmpty())
            taggedPages.put(tag, webPage);

        if(url != null && !url.trim().isEmpty())
            webPage.get(url);
        
        this.webPages.add(webPage);
        return webPage;
    }

    @Override
    public IWebPage getWebPage(String tag) {
        return taggedPages.get(tag);
    }
    
}