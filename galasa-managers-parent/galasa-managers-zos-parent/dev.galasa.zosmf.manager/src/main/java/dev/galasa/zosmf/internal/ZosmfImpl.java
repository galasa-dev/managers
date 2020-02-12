/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosmf.internal;

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
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.properties.Https;
import dev.galasa.zosmf.internal.properties.RequestRetry;
import dev.galasa.zosmf.internal.properties.ServerHostname;
import dev.galasa.zosmf.internal.properties.ServerImages;
import dev.galasa.zosmf.internal.properties.ServerPort;

/**
 * Implementation of {@link IZosmf}
 *
 */
public class ZosmfImpl implements IZosmf {
    
    private static final String LOG_BODY = "body: \n";
    
    private static final Log logger = LogFactory.getLog(ZosmfImpl.class);

    private String imageTag;
    private IZosImage image;
    private IHttpClient httpClient;
    private String zosmfUrl;
    protected int requestRetry;

    private HashMap<String, String> commonHeaders = new HashMap<>();

    public ZosmfImpl(IZosImage image) throws ZosmfException {
        this.image = image;
        initialize();
    }

    public ZosmfImpl(String imageTag) throws ZosmfException {
        this.imageTag = imageTag;

        if (this.image == null) {
            try {
                this.image = ZosmfManagerImpl.zosManager.getImageForTag(this.imageTag);
            } catch (ZosManagerException e) {
                throw new ZosmfException(e);
            }
        }
        
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
    public @NotNull IZosmfResponse get(String path, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.GET.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.debug(logRequest(method, zosmfResponse.getRequestUrl()));
            zosmfResponse.setHttpClientresponse(this.httpClient.getText(validPath(path)));
            logger.debug(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse postJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.POST.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.debug(logRequest(method, zosmfResponse.getRequestUrl()));
            logger.debug(LOG_BODY + requestBody);
            zosmfResponse.setHttpClientresponse(this.httpClient.putJson(validPath(path), requestBody));
            logger.debug(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse putText(String path, String requestBody, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.PUT.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.debug(logRequest(method, zosmfResponse.getRequestUrl()));
            logger.debug(LOG_BODY + requestBody);
            zosmfResponse.setHttpClientresponse(this.httpClient.putText(validPath(path), requestBody));
            logger.debug(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse putJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.PUT.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.debug(logRequest(method, zosmfResponse.getRequestUrl()));
            logger.debug(LOG_BODY + requestBody);
            zosmfResponse.setHttpClientresponse(this.httpClient.putJson(validPath(path), requestBody));
            logger.debug(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            logger.error(e);
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public @NotNull IZosmfResponse delete(String path, List<Integer> validStatusCodes) throws ZosmfException {
        String method = ZosmfRequestType.DELETE.name();
        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        ZosmfResponseImpl zosmfResponse;
        try {
            setHeader(ZosmfCustomHeaders.X_IBM_REQUESTED_METHOD.toString(), method);
            addCommonHeaders();
            zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
            logger.debug(logRequest(method, zosmfResponse.getRequestUrl()));
            zosmfResponse.setHttpClientresponse(this.httpClient.deleteJson(validPath(path)));
            logger.debug(logResponse(zosmfResponse.getStatusLine(), method, zosmfResponse.getRequestUrl()));
            if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
                throw new ZosmfException(logBadStatusCode(zosmfResponse.getStatusCode()));
            }
        } catch (MalformedURLException | HttpClientException  e) {
            throw new ZosmfException(logBadRequest(method), e);
        }
        
        return zosmfResponse;
    }

    @Override
    public IZosImage getImage() {
        return this.image;
    }

    @Override
    public String toString() {
        return this.image.getImageID() + " " + this.zosmfUrl;
    }

    private String validPath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private void initialize() throws ZosmfException {
        
        String imageId = image.getImageID();
        String clusterId = image.getClusterID();
        List<String> configuredZosmfs;
        try {
            configuredZosmfs = ServerImages.get(clusterId);
        } catch (ZosmfManagerException e) {
            throw new ZosmfException(e);
        }        
        if (!configuredZosmfs.contains(imageId)) {
            throw new ZosmfException("zOSMF server not configured for image '" + imageId + "' on cluster '" + clusterId + "' tag '" + imageTag + "'");
        }
        
        String zosmfHostname;
        try {
            zosmfHostname = ServerHostname.get(image.getImageID());
        } catch (ZosManagerException e) {
            throw new ZosmfException(e);
        }
        String zosmfPort;
        try {
            zosmfPort = ServerPort.get(image.getImageID());
        } catch (ZosmfManagerException e) {
            throw new ZosmfException(e);
        }
        String scheme = "http";
        try {
            if (Https.get(image.getImageID())) {
                scheme = "https";
            }
        } catch (ZosmfManagerException e) {
            throw new ZosmfException(e);
        }
        
        this.zosmfUrl = scheme + "://" + zosmfHostname + ":" + zosmfPort;

        this.httpClient = ZosmfManagerImpl.httpManager.newHttpClient();
        
        try {
            ICredentials creds = image.getDefaultCredentials();
            this.httpClient.setURI(new URI(this.zosmfUrl));
            if (creds instanceof ICredentialsUsernamePassword) {
                this.httpClient.setAuthorisation(((ICredentialsUsernamePassword) creds).getUsername(), ((ICredentialsUsernamePassword) creds).getPassword());
            }
            if (scheme.equals("https")) {
                this.httpClient.setTrustingSSLContext();
            }
            this.httpClient.build();
        } catch (HttpClientException | ZosManagerException | URISyntaxException e) {
            throw new ZosmfException("Unable to create HTTP Client", e);
        }        
        
        try {
            this.requestRetry = RequestRetry.get(image.getImageID());
        } catch (ZosManagerException e) {
            throw new ZosmfException(e);
        }
    }

    private void addCommonHeaders() {
        for (Entry<String, String> entry : this.commonHeaders.entrySet()) {
            logger.debug("Adding HTTP header: " + entry.getKey() + ": " + entry.getValue());
            this.httpClient.addCommonHeader(entry.getKey(), entry.getValue());
        }
        
    }

    private String logRequest(String method, URL requestUrl) {
        return "Request: " + method + " " + requestUrl;
    }

    private String logResponse(String statusLine, String method, URL requestUrl) {
        return "Response: " + statusLine + " - " + method + " " + requestUrl;
    }

    private String logBadStatusCode(int statusCode) {
        return "Unexpected HTTP status code: " + statusCode;
    }

    private String logBadRequest(String method) {
        return "Problem wth " + method + " to zOSMF server";
    }

    public int getRequestRetry() {
        return this.requestRetry;
    }
    
    
}
