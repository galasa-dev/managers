/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.IHttpClient;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.properties.Https;
import dev.galasa.zosrseapi.internal.properties.RequestRetry;
import dev.galasa.zosrseapi.internal.properties.ServerHostname;
import dev.galasa.zosrseapi.internal.properties.ServerImages;
import dev.galasa.zosrseapi.internal.properties.ServerPort;

/**
 * Implementation of {@link IRseapi}
 *
 */
public class RseapiImpl implements IRseapi {
    
    private static final String LOG_BODY = "body: \n";
    
    private static final Log logger = LogFactory.getLog(RseapiImpl.class);

    private String imageTag;
    private IZosImage image;
    private IHttpClient httpClient;
    private String rseapiUrl;
    protected int requestRetry;

    private HashMap<String, String> commonHeaders = new HashMap<>();

	private static final String PATH_SERVERDETAILS = "/rseapi/api/v1/info/serverdetails";

    public RseapiImpl(IZosImage image) throws RseapiException {
        this.image = image;
        initialize();
    }

    public RseapiImpl(String imageTag) throws RseapiException {
        this.imageTag = imageTag;
        setImage();        
        initialize();
    }

    @Override
    public void setHeader(String key, String value) {
        this.commonHeaders.put(key, value);
    }

    @Override
    public void clearHeaders() {
        this.commonHeaders.clear();
        this.httpClient.clearCommonHeaders();
    }

    @Override
    public @NotNull IRseapiResponse get(String path, List<Integer> validStatusCodes, boolean convert) throws RseapiException {
        String method = RseapiRequestType.GET.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        RseapiResponseImpl rseapiResponse;
        try {
            addCommonHeaders();
            rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
            logger.debug(logRequest(method, rseapiResponse.getRequestUrl()));
            if (convert) {
                rseapiResponse.setHttpClientresponse(this.httpClient.getJson(validPath(path)));
            } else {
                rseapiResponse.setHttpClientresponse(this.httpClient.getFile(validPath(path)));
            }
            
            logger.debug(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
            if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
                throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new RseapiException(logBadRequest(method), e);
        }
        
        return rseapiResponse;
    }

	@Override
	public @NotNull IRseapiResponse putJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws RseapiException {
	    String method = RseapiRequestType.PUT_JSON.getRequestType();
	    if (validStatusCodes == null) {
	        validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
	    }
	    RseapiResponseImpl rseapiResponse;
	    try {
	        addCommonHeaders();
	        rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
	        logger.debug(logRequest(method, rseapiResponse.getRequestUrl()));
	        logger.debug(LOG_BODY + requestBody);
	        rseapiResponse.setHttpClientresponse(this.httpClient.putJson(validPath(path), requestBody));
	        logger.debug(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
	        if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
	            throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
	        }
	    } catch (MalformedURLException | HttpClientException  e) {
	        logger.error(e);
	        throw new RseapiException(logBadRequest(method), e);
	    }
	    
	    return rseapiResponse;
	}

    @Override
	public @NotNull IRseapiResponse putText(String path, String requestBody, List<Integer> validStatusCodes) throws RseapiException {
	    String method = RseapiRequestType.PUT_TEXT.getRequestType();
	    if (validStatusCodes == null) {
	        validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
	    }
	    RseapiResponseImpl rseapiResponse;
	    try {
	        addCommonHeaders();
	        rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
	        logger.debug(logRequest(method, rseapiResponse.getRequestUrl()));
	        logger.debug(LOG_BODY + requestBody);
	        rseapiResponse.setHttpClientresponse(this.httpClient.putText(validPath(path), requestBody));
	        logger.debug(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
	        if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
	            throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
	        }
	    } catch (MalformedURLException | HttpClientException  e) {
	        logger.error(e);
	        throw new RseapiException(logBadRequest(method), e);
	    }
	    
	    return rseapiResponse;
	}

	@Override
    public @NotNull IRseapiResponse postJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws RseapiException {
        String method = RseapiRequestType.POST_JSON.getRequestType();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        RseapiResponseImpl rseapiResponse;
        try {
            addCommonHeaders();
            rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
            logger.debug(logRequest(method, rseapiResponse.getRequestUrl()));
            logger.debug(LOG_BODY + requestBody);
            rseapiResponse.setHttpClientresponse(this.httpClient.postJson(validPath(path), requestBody));
            logger.debug(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
            if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
                throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new RseapiException(logBadRequest(method), e);
        }
        
        return rseapiResponse;
    }

	@Override
    public @NotNull IRseapiResponse post(String path, List<Integer> validStatusCodes) throws RseapiException {
        String method = RseapiRequestType.POST_JSON.getRequestType();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        RseapiResponseImpl rseapiResponse;
        try {
            addCommonHeaders();
            rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
            logger.debug(logRequest(method, rseapiResponse.getRequestUrl()));
            rseapiResponse.setHttpClientresponse(this.httpClient.postJson(validPath(path), null));
            logger.debug(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
            if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
                throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new RseapiException(logBadRequest(method), e);
        }
        
        return rseapiResponse;
    }

    @Override
    public @NotNull IRseapiResponse delete(String path, List<Integer> validStatusCodes) throws RseapiException {
        String method = RseapiRequestType.DELETE.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        RseapiResponseImpl rseapiResponse;
        try {
            addCommonHeaders();
            rseapiResponse = new RseapiResponseImpl(this.rseapiUrl, validPath(path));
            logger.debug(logRequest(method, rseapiResponse.getRequestUrl()));
            rseapiResponse.setHttpClientresponse(this.httpClient.deleteJson(validPath(path)));
            logger.debug(logResponse(rseapiResponse.getStatusLine(), method, rseapiResponse.getRequestUrl()));
            if (!validStatusCodes.contains(rseapiResponse.getStatusCode())) {
                throw new RseapiException(logBadStatusCode(rseapiResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            throw new RseapiException(logBadRequest(method), e);
        }
        
        return rseapiResponse;
    }

    @Override
	public @NotNull JsonObject serverInfo() throws RseapiException {
        return get(PATH_SERVERDETAILS, null, true).getJsonContent();
	}

	@Override
    public IZosImage getImage() {
        return this.image;
    }

    @Override
    public String toString() {
        return this.image.getImageID() + " " + this.rseapiUrl;
    }

    protected String validPath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    protected void initialize() throws RseapiException {
        
        String imageId = image.getImageID();
        String clusterId = image.getClusterID();
        List<String> configuredRseapis;
        try {
            configuredRseapis = ServerImages.get(clusterId);
        } catch (RseapiManagerException e) {
            throw new RseapiException(e);
        }
        if (!configuredRseapis.contains(imageId)) {
            throw new RseapiException("RSE API server not configured for image '" + imageId + "' on cluster '" + clusterId + "'" + (imageTag != null ? " tag '" + imageTag + "'" : ""));
        }
        
        String rseapiHostname;
        try {
            rseapiHostname = ServerHostname.get(image.getImageID());
        } catch (ZosManagerException e) {
            throw new RseapiException(e);
        }
        String rseapiPort;
        try {
            rseapiPort = ServerPort.get(image.getImageID());
        } catch (RseapiManagerException e) {
            throw new RseapiException(e);
        }
        String scheme = "http";
        try {
            if (Https.get(image.getImageID())) {
                scheme = "https";
            }
        } catch (RseapiManagerException e) {
            throw new RseapiException(e);
        }
        this.rseapiUrl = scheme + "://" + rseapiHostname + ":" + rseapiPort;

        this.httpClient = RseapiManagerImpl.httpManager.newHttpClient();
        
        try {
			ICredentials creds = image.getDefaultCredentials();
            this.httpClient.setURI(new URI(this.rseapiUrl));
            if (creds instanceof ICredentialsUsernamePassword) {
                this.httpClient.setAuthorisation(((ICredentialsUsernamePassword) creds).getUsername(), ((ICredentialsUsernamePassword) creds).getPassword());
            }
            this.httpClient.setTrustingSSLContext();
            this.httpClient.build();
        } catch (HttpClientException | ZosManagerException | URISyntaxException e) {
            throw new RseapiException("Unable to create HTTP Client", e);
        }
        
        try {
            this.requestRetry = RequestRetry.get(image.getImageID());
        } catch (ZosManagerException e) {
            throw new RseapiException(e);
        }
    }

    protected void setImage() throws RseapiException {
        if (this.image == null) {
            try {
                this.image = RseapiManagerImpl.zosManager.getImageForTag(this.imageTag);
            } catch (ZosManagerException e) {
                throw new RseapiException(e);
            }
        }
    }

    protected void addCommonHeaders() {
        for (Entry<String, String> entry : this.commonHeaders.entrySet()) {
            logger.debug("Adding HTTP header: " + entry.getKey() + ": " + entry.getValue());
            this.httpClient.addCommonHeader(entry.getKey(), entry.getValue());
        }
        
    }

    protected String logRequest(String method, URL requestUrl) {
        return "Request: " + method + " " + requestUrl;
    }

    protected String logResponse(String statusLine, String method, URL requestUrl) {
        return "Response: " + statusLine + " - " + method + " " + requestUrl;
    }

    protected String logBadStatusCode(int statusCode) {
        return "Unexpected HTTP status code: " + statusCode;
    }

    protected String logBadRequest(String method) {
        return "Problem with " + method + " to RSE API server";
    }

    protected int getRequestRetry() {
        return this.requestRetry;
    }    
}
