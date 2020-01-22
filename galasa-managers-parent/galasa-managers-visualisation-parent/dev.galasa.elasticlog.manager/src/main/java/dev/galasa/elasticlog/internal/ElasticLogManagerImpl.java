package dev.galasa.elasticlog.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.galasa.ManagerException;
import dev.galasa.elasticlog.internal.ElasticLogManagerException;
import dev.galasa.elasticlog.internal.properties.ElasticLogEndpoint;
import dev.galasa.elasticlog.internal.properties.ElasticLogIndex;
import dev.galasa.elasticlog.internal.properties.ElasticLogPropertiesSingleton;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;

@Component(service = { IManager.class })
public class ElasticLogManagerImpl extends AbstractManager {

    private static final Log                    logger          = LogFactory.getLog(ElasticLogManagerImpl.class);
    public final static String                  NAMESPACE       = "elastic";
    
    private IConfigurationPropertyStoreService  cps;
    private IDynamicStatusStoreService          dss;
    private IConfidentialTextService			ctf;

    private IHttpManagerSpi        		        httpManager;
    private IHttpClient							client;
    
    private Gson								gson;

    private String                              runId;
    private String                              testCase;
    private Date                                startTimestamp;
    private Date                                endTimestamp;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        youAreRequired(allManagers, activeManagers);

        try {
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            this.ctf = framework.getConfidentialTextService();
            this.runId = framework.getTestRunName();
            this.testCase = testClass.getSimpleName();
            ElasticLogPropertiesSingleton.setCps(this.cps);
        } catch (Exception e) {
            throw new ElasticLogManagerException("Unable to request framework services", e);
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this))
            return;
        activeManagers.add(this);

        httpManager = addDependentManager(allManagers, activeManagers, IHttpManagerSpi.class);
    }

    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        this.client = this.httpManager.newHttpClient();
        this.gson = new GsonBuilder().setDateFormat("yyy-MM-dd'T'HH:mm:ss'Z'").create();
    }

    public void startOfTestClass() throws ManagerException {
        this.startTimestamp = new Date();
    }

    public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws ManagerException {
        this.endTimestamp = new Date();
        
        ElasticLogRun run = new ElasticLogRun(this.testCase, this.runId, this.startTimestamp, this.endTimestamp, currentResult);
        String request = this.gson.toJson(run);
        sendRequest(request);
        
        return null;
    }
    
//  Re enable when supported
//  @Override
//  public void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
//     
//     ElasticLogRun run = new ElasticLogRun(this.testCase, this.runId, this.startTimestamp, this.endTimestamp, finalResult);
//     String request = this.gson.toJson(run);
//     sendRequest(request);
//  }
    
    private void sendRequest(String request) throws ElasticLogManagerException {
    	String index = ElasticLogIndex.get();
    	String endpoint = ElasticLogEndpoint.get();
		ctf.registerText(index, "ElasticLog Index");
		ctf.registerText(endpoint, "ElasticLog Endpoint");
    	try {
    		String mapping = "{\"mappings\" : {\"properties\" : {"
                    + "\"testCase\" : {\"type\" : \"text\", \"fields\" : {\"keyword\" : {\"type\" : \"keyword\"}}},"
                    + "\"runId\" : {\"type\" : \"text\", \"fields\" : {\"keyword\" : {\"type\" : \"keyword\"}}},"
                    + "\"startTimestamp\" : {\"type\" : \"date\"},"
                    + "\"endTimestamp\" : {\"type\" : \"date\"},"
                    + "\"result\" : {\"type\" : \"text\", \"fields\" : {\"keyword\" : {\"type\" : \"keyword\"}}}"
                    + "}}}";
    		
            client.setTrustingSSLContext();
            client.addOkResponseCode(201);
        	client.setURI(new URI(endpoint));
    		HttpClientResponse<String> response = client.head(endpoint + "/" + index);
    		
			if (response.getStatusCode() == 404) 
                client.putJson(index, mapping, false);
			
			client.postJson(index + "/_doc", request, false);
        
    		index = index + "single";
    		
    		HttpClientResponse<String> indexResponse = client.head(ElasticLogEndpoint.get() + "/" + index);
    		
			if (indexResponse.getStatusCode() == 404)
                client.putJson(index, mapping, false);
        
        	String id = null;
        	
			String searchResponse = client.get(index + "/_search?q=testCase:" + this.testCase);
			if (searchResponse.contains(this.testCase)) {
				JsonObject json = this.gson.fromJson(searchResponse, JsonObject.class);
				id = json.get("hits").getAsJsonObject()
								.get("hits").getAsJsonArray()
								.get(0).getAsJsonObject()
								.get("_id").getAsString();
			}
			
    		if (id != null)
    			client.delete(index + "/_doc/" + id, false);
    		client.postJson(index + "/_doc", request, false);

		} catch (Exception e) {
			throw new ElasticLogManagerException("Error in sending data to ElasticLog Endpoint", e);
		}
    }
}