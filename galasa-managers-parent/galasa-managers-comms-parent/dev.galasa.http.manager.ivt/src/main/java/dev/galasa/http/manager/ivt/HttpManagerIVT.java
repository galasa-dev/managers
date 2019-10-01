package dev.galasa.http.manager.ivt;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;

import com.google.gson.JsonObject;

import dev.galasa.Test;
import dev.galasa.common.http.HttpClient;
import dev.galasa.common.http.HttpClientException;
import dev.galasa.common.http.HttpClientResponse;
import dev.galasa.common.http.IHttpClient;
import dev.galasa.core.manager.Logger;

@Test
public class HttpManagerIVT {
	
    @Logger
    public Log logger;
    
    @HttpClient
    public IHttpClient client1;
    
    @HttpClient
    public IHttpClient client2;
    
    @Test
    public void checkClientNotNull() throws Exception {
        if (client1 == null) {
            throw new Exception("client1 is null, should have been filled by the http Manager");
        }
        if (client2 == null) {
            throw new Exception("client2 is null, should have been filled by the http Manager");
        }
    }
    
    @Test
    public void makeOutBoundHttpCall() throws Exception, URISyntaxException, HttpClientException{
    	client1.setURI(new URI("http://google.com"));
        String response = client1.get("/images", false);
        if(response == null){
            throw new Exception("Unable to speak to external endpoint");
        }
    }
    
    @Test
    public void makeJsonRequest() throws HttpClientException, URISyntaxException, Exception{
    	client2.setURI(new URI("http://jsonplaceholder.typicode.com"));
    	HttpClientResponse<JsonObject> resp = client2.getJson("/todos/1");
    	JsonObject json = resp.getContent();
    	String title = json.get("title").getAsString();
    	if(!title.equals("delectus aut autem")) {
    		logger.error("Did not get correct title");
    		throw new Exception("Did not receive title as expected");
    	}
    }

}
