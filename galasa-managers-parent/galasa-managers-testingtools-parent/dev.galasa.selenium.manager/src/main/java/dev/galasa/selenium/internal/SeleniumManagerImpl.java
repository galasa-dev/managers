/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

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
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IDriver;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.SeleniumManager;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.SeleniumManagerField;
import dev.galasa.selenium.internal.properties.SeleniumPropertiesSingleton;
import dev.galasa.selenium.internal.properties.SeleniumWebDriverType;

@Component(service = { IManager.class })
public class SeleniumManagerImpl extends AbstractManager implements ISeleniumManager {

    public static final String NAMESPACE = "selenium";

    private IConfigurationPropertyStoreService cps; // NOSONAR
    private Path screenshotRasDirectory;

    private List<IDriver> drivers = new ArrayList<>();

    private boolean required = false;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if (galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(SeleniumManagerField.class);
            if (!ourFields.isEmpty() || this.required) {
                youAreRequired(allManagers, activeManagers);
            }
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
        Path storedArtifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        screenshotRasDirectory = storedArtifactsRoot.resolve("selenium").resolve("screenshots");

        generateAnnotatedFields(SeleniumManagerField.class);
    }

    @GenerateAnnotatedField(annotation = SeleniumManager.class)
    public IDriver generateSeleniumManager(Field field, List<Annotation> annotations) throws ConfigurationPropertyStoreException, SeleniumManagerException {
        SeleniumManager annoation = field.getAnnotation(SeleniumManager.class);
        Browser browser = annoation.browser();
        switch(SeleniumWebDriverType.get()) {
            case ("local"):
                return new WebDriverImpl(browser, screenshotRasDirectory);
            case ("docker"):
                // TODO
                break;
            case ("kubernetes"):
                // TODO
                break;
            case ("grid"):
                // TODO
                break;
        }
        throw new SeleniumManagerException("Unsupported Driver Type");
    }

    // @Override
    // public String endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull String currentResult,
    //         Throwable currentException) throws ManagerException {
    //     try {
    //         if (!currentResult.equals("Passed")) {
    //             if (SeleniumScreenshotFailure.get()) {
    //                 for (IWebPage page : webPages) {
    //                     page.takeScreenShot();
    //                 }
    //             }
    //         }
    //     } catch (ConfigurationPropertyStoreException e) {

    //     }
    //     return null;
    // }

    @Override
    public void provisionDiscard() {
        for (IDriver driver : drivers) {
            driver.discard();
        }
    } 
    
}