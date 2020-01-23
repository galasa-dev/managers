package dev.galasa.elasticlog.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.time.Instant;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.galasa.ManagerException;
import dev.galasa.elasticlog.internal.properties.ElasticLogEndpoint;
import dev.galasa.elasticlog.internal.properties.ElasticLogIndex;
import dev.galasa.elasticlog.internal.properties.ElasticLogPropertiesSingleton;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;

/**
 * ElasticLog Manager implementation
 * 
 * @author Richard Somers
 */
@Component(service = { IManager.class })
public class ElasticLogManagerImpl extends AbstractManager {

    private static final Log                    logger          = LogFactory.getLog(ElasticLogManagerImpl.class);
    public final static String                  NAMESPACE       = "elastic";
    
    private IFramework							framework;
    private IConfigurationPropertyStoreService  cps;
    private IConfidentialTextService			ctf;

    private IHttpManagerSpi        		        httpManager;
    private IHttpClient							client;
    
    private Gson								gson;

    private HashMap<String, Object>				runProperties   = new HashMap<String, Object>(); 

    /**
     * Initialies the ElasticLogManager, adding the requirement of the HttpManager
     *  
     * @param IFramework - the galasa framework
     * @param List<IManager> - list of all the managers
     * @param List<Imanager> - list of all the active managers
     * @param Class<?> - the test class
     * @throws ManagerException
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, testClass);

        youAreRequired(allManagers, activeManagers);

        try {
            this.framework = framework;
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            this.ctf = framework.getConfidentialTextService();
            ElasticLogPropertiesSingleton.setCps(this.cps);
        } catch (Exception e) {
            throw new ElasticLogManagerException("Unable to request framework services", e);
        }
    }
    
	/**
	 * Makes sure that the elastic log manager is added to the list of active managers, and adds the dependency on http manager.
	 * 
	 * @param List<IManager> - list of all the managers
	 * @param List<IManager> - list of the active managers
	 * @throws ManagerException
	 */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
        if (activeManagers.contains(this))
            return;
        activeManagers.add(this);

        httpManager = addDependentManager(allManagers, activeManagers, IHttpManagerSpi.class);
    }

    /**
     * Provision build step, build the http client and gson object used by the manager 
     * 
     * @throws ManagerException
     * @throws ResourceUnavailableException
     */
    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        this.client = this.httpManager.newHttpClient();
        this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        logger.info("ElasticLog Clients Initialised");
    }

    /**
     * End of test class step, build and send the document request
     * 
     * @throws ManagerException
     */
    public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws ManagerException {
        //Record test information
    	this.runProperties.put("testCase", this.framework.getTestRun().getTestClassName());
    	this.runProperties.put("runId", this.framework.getTestRunName());
        this.runProperties.put("startTimestamp", Date.from(this.framework.getTestRun().getQueued()));
        //this.runProperties.put("endTimestamp", Date.from(this.framework.getTestRun().getFinished()));
        this.runProperties.put("endTimestamp", Date.from(Instant.now()));
        this.runProperties.put("requestor", this.framework.getTestRun().getRequestor());
        //this.runProperties.put("result", this.framework.getTestRun().getResult());
        this.runProperties.put("result", currentResult);
        
        String request = this.gson.toJson(this.runProperties);
        logger.info("Sending Run Request to ElasticLog Endpoint");
        logger.trace("Document Request -\n" + request);
        sendRunData(request);
        
        return null;
    }
    
//  Re enable when supported
//  @Override
//  public void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
//    
//  }
    
    /**
     * Create the required indexes and send document requests
     * 
     * @param request - document request json
     * @throws ElasticLogManagerException
     */
    private void sendRunData(String request) throws ElasticLogManagerException {
        //Register endpoint data as confidential
    	String index = ElasticLogIndex.get();
    	String endpoint = ElasticLogEndpoint.get();
		ctf.registerText(index, "ElasticLog Index");
		ctf.registerText(endpoint, "ElasticLog Endpoint");
    	try {
            //Create mapping Json
    		String mapping = createIndexMapping();
    		logger.trace("Index Mapping Request -\n" + mapping);
            
            //Set up http client for requests
            client.setTrustingSSLContext();
            client.addOkResponseCode(201);
            client.setURI(new URI(endpoint));
            
            //Create index if not present
    		HttpClientResponse<String> response = client.head(endpoint + "/" + index);
    		if (response.getStatusCode() == 404) 
                client.putJson(index, mapping, false);
            
            //Send document to index
			client.postJson(index + "/_doc", request, false);
        
            //Create single index if not present
    		index = index + "single";
    		HttpClientResponse<String> indexResponse = client.head(ElasticLogEndpoint.get() + "/" + index);
			if (indexResponse.getStatusCode() == 404)
                client.putJson(index, mapping, false);
        
            //Obtain id of document if test case is already recorded
        	String id = null;
        	String testCase = (String) this.runProperties.get("testCase");
			String searchResponse = client.get(index + "/_search?q=testCase:" + testCase);
			if (searchResponse.contains(testCase)) {
				JsonObject json = this.gson.fromJson(searchResponse, JsonObject.class);
				id = json.get("hits").getAsJsonObject()
                         .get("hits").getAsJsonArray()
                         .get(0).getAsJsonObject()
                         .get("_id").getAsString();
			}
            
            //Delete document if already present then send new document
    		if (id != null)
    			client.delete(index + "/_doc/" + id, false);
    		client.postJson(index + "/_doc", request, false);

		} catch (HttpClientException e) {
			throw new ElasticLogManagerException("Error in sending request to ElasticLog Endpoint", e);
		} catch (URISyntaxException e) {
			throw new ElasticLogManagerException("Error in parsing ElasticLog Endpoint to URI", e);
		}
    }

    /**
     * Create a mapping properties request from run properties
     * 
     * @return - Mapping properties request
     */
    private String createIndexMapping() {
        JsonObject mapProp = new JsonObject();
    	for(String mappingKey : this.runProperties.keySet()) {
    		String typeString;
    		if(this.runProperties.get(mappingKey) instanceof Date)
    			typeString = "date";
    		else
    			typeString = "text";
    		
    		JsonObject type = new JsonObject();
    		type.addProperty("type", typeString);
    		
    		//Add keyword field to make term aggregatable if text
    		if (typeString.equals("text")) {
				JsonObject fields = new JsonObject();
				JsonObject keyword = new JsonObject();
				keyword.addProperty("type", "keyword");
				fields.add("keyword", keyword);
				type.add("fields", fields);
			}
			mapProp.add(mappingKey, type);
    	}
		JsonObject map = new JsonObject();
		map.add("properties", mapProp);
		JsonObject mapRequest = new JsonObject();
		mapRequest.add("mappings", map);
		return gson.toJson(mapRequest);
    }
}