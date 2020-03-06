/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.elasticlog.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.galasa.ManagerException;
import dev.galasa.elasticlog.internal.properties.ElasticLogEndpoint;
import dev.galasa.elasticlog.internal.properties.ElasticLogIndex;
import dev.galasa.elasticlog.internal.properties.ElasticLogLocalRun;
import dev.galasa.elasticlog.internal.properties.ElasticLogPropertiesSingleton;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ILoggingManager;
import dev.galasa.framework.spi.IManager;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;

/**
 * ElasticLog Manager implementation
 * 
 * @author Richard Somers
 */
@Component(service = { IManager.class })
public class ElasticLogManagerImpl extends AbstractManager {

	private static final Log					logger			= LogFactory.getLog(ElasticLogManagerImpl.class);
	public final static String					NAMESPACE		= "elasticlog";
    
	private IFramework							framework;
	private IConfigurationPropertyStoreService	cps;
	private IConfidentialTextService			ctf;

	private List<IManager>						otherManagers	= new ArrayList<IManager>();

	private IHttpManagerSpi						httpManager;

	private HashMap<String, Object>				runProperties	= new HashMap<String, Object>(); 

	/**
	 * Initialise the ElasticLogManager, adding a pointer to the other active managers
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

		try {
			this.framework = framework;
			this.cps = framework.getConfigurationPropertyService(NAMESPACE);
			this.ctf = framework.getConfidentialTextService();
			ElasticLogPropertiesSingleton.setCps(this.cps);
		} catch (Exception e) {
			throw new ElasticLogManagerException("Unable to request framework services", e);
		}
        
		if(!framework.getTestRun().isLocal() || ElasticLogLocalRun.get().equals("true"))
			youAreRequired(allManagers, activeManagers);
		
		this.otherManagers = activeManagers;
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
	 * Test class result step, build and send the document request
	 * 
	 * @throws ManagerException
	 */
	@Override
	public void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
		//Record test information
		this.runProperties.put("testCase", this.framework.getTestRun().getTestClassName());
		this.runProperties.put("runId", this.framework.getTestRunName());
		this.runProperties.put("startTimestamp", Date.from(this.framework.getTestRun().getQueued()));
		this.runProperties.put("endTimestamp", Date.from(Instant.now()));
		this.runProperties.put("requestor", this.framework.getTestRun().getRequestor());
		this.runProperties.put("result", finalResult);
	    
		String testTooling        = "Galasa";
		String testType           = "Galasa";
		String testingEnvironment = "NOT_ASSIGNED";
		String productRelease     = null;
		String buildLevel         = null;
		String customBuild        = null;
		List<String> testingAreas = new ArrayList<String>();
		List<String> tags		  = new ArrayList<String>();
	
		//Ask other managers for additional logging information
		for(IManager manager : this.otherManagers) {
			if(manager instanceof ILoggingManager) {
				ILoggingManager loggingManager = (ILoggingManager) manager;
	    		
				String tooling = loggingManager.getTestTooling();
				if(tooling != null)
					testTooling = tooling;
	    		
				String type = loggingManager.getTestType();
				if(type != null)
					testType = type;
	    		
				String environment = loggingManager.getTestingEnvironment();
				if(environment != null)
					testingEnvironment = environment;
	    		
				String release = loggingManager.getProductRelease();
				if(release != null)
					productRelease = release;
	    		
				String level = loggingManager.getBuildLevel();
				if(level != null)
					buildLevel = level;
	    		
				String custom = loggingManager.getCustomBuild();
				if(custom != null)
					customBuild = custom;
	    		
				List<String> areas = loggingManager.getTestingAreas();
				if(areas != null)
					testingAreas.addAll(areas);
	    		
				List<String> tagList = loggingManager.getTags();
				if(tagList != null)
					tags.addAll(tagList);
			}
		}
		if (testingAreas.isEmpty())
			testingAreas = null;
	    
		if (tags.isEmpty())
			tags = null;
	    
		this.runProperties.put("testTooling", testTooling);
		this.runProperties.put("testType", testType);
		this.runProperties.put("testingEnvironment", testingEnvironment);
		this.runProperties.put("productRelease", productRelease);
		this.runProperties.put("buildLevel", buildLevel);
		this.runProperties.put("customBuild", customBuild);
	
		if(testingAreas != null)
			this.runProperties.put("testingAreas", testingAreas.toArray(new String[0]));
	
		if(tags != null)
			this.runProperties.put("tags", tags.toArray(new String[0]));	    	
	
		//Convert HashMap of run properties to a Json String
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
		String request = gson.toJson(this.runProperties);

		logger.info("Sending Run Request to ElasticLog Endpoint");
		logger.trace("Document Request -\n" + request);
		
		//Register endpoint data as confidential
		String index = ElasticLogIndex.get();
		String endpoint = ElasticLogEndpoint.get();
		ctf.registerText(index, "ElasticLog Index");
		ctf.registerText(endpoint, "ElasticLog Endpoint");
		try {
			//Set up http client for requests
			IHttpClient client = this.httpManager.newHttpClient();
			client.setTrustingSSLContext();
			client.addOkResponseCode(201);
			client.setURI(new URI(endpoint));

			//Send document to index
			client.postJson(index + "/_doc", request, false);
			logger.info("Run successfully logged to Elastic index " + index);
        
			//Change index to latest document index
			index = index + "_latest";
		 	String testCase = (String) this.runProperties.get("testCase");
		 	
		 	//Create new doc if doesnt exist, updates if doc already exists
			client.postJson(index + "/_doc/" + testCase + testingEnvironment, request, false);
			logger.info("Run successfully logged to Elastic index " + index);

		} catch (HttpClientException e) {
			logger.info("ElasticLog Manager failed to send information to Elastic Endpoint");
		} catch (URISyntaxException e) {
			logger.info("ElasticLog Manager failed to send parse URI of Elastic Endpoint");
		}
	}
}