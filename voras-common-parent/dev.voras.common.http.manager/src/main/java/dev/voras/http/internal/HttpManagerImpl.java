package dev.voras.http.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.http.HttpClient;
import dev.voras.http.IHttpClient;
import dev.voras.http.IHttpManager;
import io.ejat.framework.spi.AbstractManager;
import io.ejat.framework.spi.GenerateAnnotatedField;
import io.ejat.framework.spi.ManagerException;
import io.ejat.framework.spi.ResourceUnavailableException;

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