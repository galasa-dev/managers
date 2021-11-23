/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.http.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.HttpClient;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;

@Component(service = { IManager.class })
public class HttpManagerImpl extends AbstractManager implements IHttpManagerSpi {

    private static final Log  logger              = LogFactory.getLog(HttpManagerImpl.class);
    private static final int  DEFAULT_TIMEOUT     = 180000;
    private static final boolean DEFAULT_ARCHIVE  = false;
    private List<IHttpClient> instantiatedClients = new ArrayList<>();
    
    private String currentMethod                  = new String();
    private Path storedArtifactRoot;

    @GenerateAnnotatedField(annotation = HttpClient.class)
    public IHttpClient generateHttpClient(Field field, List<Annotation> annotations) {
    	boolean archiveHeaders = false;
    	for(Annotation a : annotations) {
    		if(a instanceof HttpClient) {
    			archiveHeaders = ((HttpClient)a).archiveHeaders();
    		}
    	}
        return newHttpClient(archiveHeaders);
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(HttpManagerField.class);
    }

    @Override
    public void shutdown() {
        for (IHttpClient client : instantiatedClients) {
            client.close();
        }
    }

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        this.storedArtifactRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(HttpManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
    }
    
    @Override
    public @NotNull IHttpClient newHttpClient() {
        return newHttpClient(DEFAULT_TIMEOUT, DEFAULT_ARCHIVE);
    }
    
    @Override
    public @NotNull IHttpClient newHttpClient(boolean archiveHeaders){
    	return newHttpClient(DEFAULT_TIMEOUT, archiveHeaders);
    }
    
    @Override
	public @NotNull IHttpClient newHttpClient(int timeout) {
		return newHttpClient(timeout, DEFAULT_ARCHIVE);
    }
    
    @Override
    public @NotNull IHttpClient newHttpClient(int timeout, boolean archiveHeaders) {
        IHttpClient client = new FrameworkHttpClientImpl(timeout, archiveHeaders, this);
        instantiatedClients.add(client);
        return client;
    }
    
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
    	this.currentMethod = galasaMethod.getJavaExecutionMethod().getName();
    }
    
    public String getCurrentMethod() {
    	return this.currentMethod;
    }
    
    public Path getStoredArtifactRoot() {
    	return this.storedArtifactRoot;
    }
    
    @Override
    public boolean doYouSupportSharedEnvironments() {
        return true;   // this manager does not provision resources, therefore support environments 
    }

	

}
