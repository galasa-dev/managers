package dev.galasa.selenium.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.gson.JsonObject;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.IResource;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.spi.IKubernetesManagerSpi;
// import dev.galasa.docker.DockerManagerException;
// import dev.galasa.docker.IDockerContainer;
// import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.selenium.Browser;
import dev.galasa.selenium.IChromeOptions;
import dev.galasa.selenium.IEdgeOptions;
import dev.galasa.selenium.IFirefoxOptions;
import dev.galasa.selenium.IInternetExplorerOptions;
import dev.galasa.selenium.ISeleniumManager;
import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;
import dev.galasa.selenium.internal.properties.SeleniumGridEndpoint;
import dev.galasa.selenium.internal.properties.SeleniumKubernetesNamespace;
import dev.galasa.selenium.internal.properties.SeleniumKubernetesNodeSelector;
import dev.galasa.selenium.internal.properties.SeleniumWebDriverType;

public class RemoteDriverImpl extends DriverImpl implements ISeleniumManager {
	private static final Log logger = LogFactory.getLog(RemoteDriverImpl.class);
	
    private List<WebPageImpl>   webPages = new ArrayList<>();
    private URL                 remoteDriverEndpoint;
    private Path                screenshotRasDirectory;
    private Browser             browser;
    private SeleniumManagerImpl seleniumManager;
    private IDynamicStatusStoreService dss;
    
    private SeleniumEnvironment seleniumEnvironment;
    
    private String driverSlotName;
    
    public RemoteDriverImpl(SeleniumEnvironment seleniumEnvironment,SeleniumManagerImpl seleniumManager, 
    		Browser browser, String slotName, Path screenshotRasDirectory) throws SeleniumManagerException {
    	this.seleniumEnvironment = seleniumEnvironment;
    	this.seleniumManager = seleniumManager;
    	this.browser = browser;
    	this.driverSlotName = slotName;
        this.screenshotRasDirectory = screenshotRasDirectory.resolve(browser.getDriverName());
        this.dss = seleniumManager.getDss();
        
    	try {
			switch(SeleniumWebDriverType.get()) {
			case ("docker"):
				provisionDocker(seleniumManager.getDockerManager(), seleniumManager.getHttpManager());
				break;
			case ("kubernetes"):
				provisionK8s(seleniumManager.getKubernetesManager(), seleniumManager.getArtifactManager());
				break;
			case ("grid"):
				provisionGrid(seleniumManager.getHttpManager());
				break;
			default :
				throw new SeleniumManagerException("Unsupported Driver Type: " + SeleniumWebDriverType.get());
			}
		} catch (ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Failed to create remote driver",e);
		}
    }

    private void provisionGrid(IHttpManagerSpi httpManager) throws SeleniumManagerException {
    	try {
    		this.remoteDriverEndpoint = new URL(SeleniumGridEndpoint.get());
    		
    		IHttpClient client = httpManager.newHttpClient();
            client.setURI(remoteDriverEndpoint.toURI());
    		
            HttpClientResponse<JsonObject> resp = client.getJson("/status");
            if (resp.getStatusCode() >200 ) {
            	throw new SeleniumManagerException("Bad response from Grid: " + resp.getStatusLine());
            }
    	} catch (MalformedURLException |ConfigurationPropertyStoreException |URISyntaxException  |HttpClientException e) {
    		throw new SeleniumManagerException("Failed to provision a Grid Driver.", e);
    	}
    	
    }
 
    private void provisionDocker(IDockerManagerSpi dockerManager, IHttpManagerSpi httpManager) throws SeleniumManagerException {
        try {
            IDockerContainer container = dockerManager.provisionContainer("Selenium_Standalone_node", browser.getDockerImageName(), true, "PRIMARY");
            
            List<InetSocketAddress> dockerEndpoint = container.getExposedPorts().get("4444/tcp");
            this.remoteDriverEndpoint = new URL("http:/"+ dockerEndpoint.get(0));
            
            IHttpClient client = httpManager.newHttpClient();
            client.setURI(remoteDriverEndpoint.toURI());
            for (int i=0;i<5;i++) {
            	try {
            		HttpClientResponse<JsonObject> resp = client.getJson("/status");
                	if (resp.getStatusCode() < 202) {
                		break;
                	}
            	} catch (HttpClientException e) {
            		Thread.sleep(2000);
            		continue;
            	}
            	throw new SeleniumManagerException("Selenium node failed to become ready");
            }
        } catch (DockerManagerException | MalformedURLException | InterruptedException | URISyntaxException e) {
            throw new SeleniumManagerException(e);
        } 
        
    }
    
    private void provisionK8s(IKubernetesManagerSpi k8Manager, IArtifactManager artifactManager) throws SeleniumManagerException {
    	String seleniumPodYaml = generatePodYaml(artifactManager);
    	try {
    		IKubernetesNamespace namespace = k8Manager.getNamespaceByTag(SeleniumKubernetesNamespace.get()); 
    	
    		IResource pod = namespace.createResource(seleniumPodYaml);
    		for (int i=0;i<5;i++) {
    			if (pod.getYaml().contains("ready: true")){
    				return;
    			}
    			Thread.sleep(2000);
    			pod.refresh();
    		}
    	} catch (KubernetesManagerException | ConfigurationPropertyStoreException | InterruptedException e) {
    		throw new SeleniumManagerException("Unable to provision K8 node", e);
    	}
    	throw new SeleniumManagerException("Selenium Node took too long to ready.");
    	
    }
    
    private String generatePodYaml(IArtifactManager artifacts) throws SeleniumManagerException {
    	logger.trace("Generating Pod Yaml");
    	IBundleResources resources = artifacts.getBundleResources(getClass());
    	try {
			String yaml = resources.retrieveFileAsString("resources/selenium-node-pod.yaml");
			yaml = yaml.replace("<IMAGE_NAME>", this.browser.getDockerImageName());
			
			String runName = seleniumManager.getFramework().getTestRunName();
			yaml = yaml.replace("<RUNNAME>", runName);
			
			String nodeSelectors = "";
			String[] selectors = SeleniumKubernetesNodeSelector.get();
			if (selectors.length > 1) {
				nodeSelectors += "  nodeSelector:\n";
				for(String selector : selectors) {
					nodeSelectors += "    " + selector + "\n";
				}
			}
			yaml = yaml.replace("<NODE_SELECTOR>", nodeSelectors);
			logger.trace(yaml);
			return yaml;
		} catch (TestBundleResourceException | IOException | ConfigurationPropertyStoreException e) {
			throw new SeleniumManagerException("Unable to generate pod yaml",e);
		} 
    }

    private RemoteWebDriver remoteDriver() throws SeleniumManagerException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(browser.getDriverName());
        capabilities.setCapability("GALASA", "GALASA");

        RemoteWebDriver driver = new RemoteWebDriver(this.remoteDriverEndpoint, capabilities);
        // Selenium Environment setSessionID in Dss for grid cleanup
        try {
			if (dss.get("driver.slot."+ driverSlotName +".session") == null) {
				dss.put("driver.slot."+ driverSlotName +".session", driver.getSessionId().toString());
			} else {
				// Clear the driver of any pages left open
				discard();
				dss.put("driver.slot."+ driverSlotName +".session", driver.getSessionId().toString());
			}
		} catch (DynamicStatusStoreException e) {
			throw new SeleniumManagerException("Failed to set session to a slot", e);
		}
        
        return driver;
    }
    
    private RemoteWebDriver remoteDriver(DesiredCapabilities capabilities) throws SeleniumManagerException {
        capabilities.setBrowserName(browser.getDriverName());

        return new RemoteWebDriver(this.remoteDriverEndpoint, capabilities);
    }

    @Override
    public IWebPage allocateWebPage() throws SeleniumManagerException {
        return allocateWebPage(null);
    }

    @Override
    public IWebPage allocateWebPage(String url) throws SeleniumManagerException {
        RemoteWebDriver driver = null;

        try {
            driver = remoteDriver();

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, IFirefoxOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(options.getOptions()));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, ChromeOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(options));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, EdgeOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(options));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, InternetExplorerOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(options));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IWebPage allocateWebPage(String url, OperaOptions options) throws SeleniumManagerException {
    	RemoteWebDriver driver = null;

        try {
            driver = remoteDriver(new DesiredCapabilities(options));

            if (driver == null)
                throw new SeleniumManagerException("Unsupported driver type: " + browser.getDriverName());
        } catch (SeleniumManagerException e) {
            throw new SeleniumManagerException("Issue provisioning web driver", e);
        }

        return allocatePage(driver, url, screenshotRasDirectory);
    }

    @Override
    public IFirefoxOptions getFirefoxOptions() {
        return new FirefoxOptionsImpl();
    }

    @Override
    public IChromeOptions getChromeOptions() {
        return new ChromeOptionsImpl();
    }

    @Override
    public IEdgeOptions getEdgeOptions() {
        return new EdgeOptionsImpl();
    }

    @Override
    public IInternetExplorerOptions getInternetExplorerOptions() {
        return new InternetExplorerOptionsImpl();
    }

    public void discard() {
       discardPages();
    }
    
}