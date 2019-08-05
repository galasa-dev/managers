package dev.voras.common.http.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.voras.ManagerException;
import dev.voras.common.http.HttpClient;
import dev.voras.common.http.IHttpClient;
import dev.voras.common.http.IHttpManager;
import dev.voras.common.http.spi.IHttpManagerSpi;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.AnnotatedField;
import dev.voras.framework.spi.GenerateAnnotatedField;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IManager;
import dev.voras.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class HttpManagerImpl extends AbstractManager implements IHttpManagerSpi {

    private static final Log logger = LogFactory.getLog(HttpManagerImpl.class);
    private List<IHttpClient> instantiatedClients = new ArrayList<>();

    @GenerateAnnotatedField(annotation=HttpClient.class)
	public IHttpClient generateHttpClient(Field field, List<Annotation> annotations) {
    	IHttpClient client = new HttpClientImpl(logger);
    	instantiatedClients.add(client);
		return client;
	}
    

	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		generateAnnotatedFields(HttpManagerField.class);
	}

	@Override
	public void provisionStop() {
		for(IHttpClient client : instantiatedClients) {
			client.close();
		}
	}


	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);
		
		List<AnnotatedField> ourFields = findAnnotatedFields(HttpManagerField.class);
		if (!ourFields.isEmpty()) {
			youAreRequired(allManagers, activeManagers);
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
	public @NotNull IHttpClient newHttpClient() {
		IHttpClient client = new HttpClientImpl(logger);
    	instantiatedClients.add(client);
		return client;
	}
}