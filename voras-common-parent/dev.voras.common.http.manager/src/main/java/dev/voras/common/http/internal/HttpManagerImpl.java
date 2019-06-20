package dev.voras.common.http.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.GenerateAnnotatedField;
import dev.voras.ManagerException;
import dev.voras.common.http.HttpClient;
import dev.voras.common.http.IHttpClient;
import dev.voras.common.http.IHttpManager;
import dev.voras.framework.spi.ResourceUnavailableException;

public class HttpManagerImpl extends AbstractManager implements IHttpManager {

    private final static Log logger = LogFactory.getLog(HttpManagerImpl.class);
    private List<IHttpClient> instantiatedClients = new ArrayList<IHttpClient>();

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
}