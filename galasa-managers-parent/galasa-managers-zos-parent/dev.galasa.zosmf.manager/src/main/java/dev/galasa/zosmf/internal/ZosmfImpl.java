package dev.galasa.zosmf.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import dev.galasa.zosmf.internal.properties.ServerHostname;
import dev.galasa.zosmf.internal.properties.ServerImages;
import dev.galasa.zosmf.internal.properties.ServerPort;

/**
 * Implementation of {@link IZosmf}
 *
 */
public class ZosmfImpl implements IZosmf {
	
	private static final Log logger = LogFactory.getLog(ZosmfImpl.class);
	
	private static final String X_IBM_JOB_MODIFY_VERSION = "X-IBM-Job-Modify-Version";
	private static final String X_IBM_REQUESTED_METHOD = "X-IBM-Requested-Method";

	private String imageTag;
	private IZosImage image;
	private IHttpClient httpClient;
	private String zosmfUrl;

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
		httpClient.addCommonHeader(key, value);
	}

	@Override
	public IZosmfResponse putText(String path, String body, List<Integer> validStatusCodes) throws ZosmfException {
		String method = "PUT";
		if (validStatusCodes == null) {
			validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
		}
		ZosmfResponseImpl zosmfResponse;
		try {
			setHeader(X_IBM_JOB_MODIFY_VERSION, "2.0");
			setHeader(X_IBM_REQUESTED_METHOD, method);
			zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
			zosmfResponse.setHttpClientresponse(httpClient.putText(validPath(path), body));
			logger.debug(zosmfResponse.getStatusLine() + " - " + method + " " + zosmfResponse.getRequestUrl());
			if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
				throw new ZosmfException("Unexpected HTTP status code: " + zosmfResponse.getStatusCode());
			}
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem with " + method + " to zOSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse putJson(String path, JsonObject body, List<Integer> validStatusCodes) throws ZosmfException {
		String method = "PUT";
		if (validStatusCodes == null) {
			validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
		}
		ZosmfResponseImpl zosmfResponse;
		try {
			setHeader(X_IBM_JOB_MODIFY_VERSION, "2.0");
			setHeader(X_IBM_REQUESTED_METHOD, method);
			zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
			zosmfResponse.setHttpClientresponse(httpClient.putJson(validPath(path), body));
			logger.debug(zosmfResponse.getStatusLine() + " - " + method + " " + zosmfResponse.getRequestUrl());
			if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
				throw new ZosmfException("Unexpected HTTP status code: " + zosmfResponse.getStatusCode());
			}
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem with " + method + " to zOSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse get(String path, List<Integer> validStatusCodes) throws ZosmfException {
		String method = "GET";
		if (validStatusCodes == null) {
			validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
		}
		ZosmfResponseImpl zosmfResponse;
		try {
			setHeader(X_IBM_JOB_MODIFY_VERSION, "2.0");
			setHeader(X_IBM_REQUESTED_METHOD, method);
			zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
			zosmfResponse.setHttpClientresponse(httpClient.getText(validPath(path)));
			logger.debug(zosmfResponse.getStatusLine() + " - " + method + " " + zosmfResponse.getRequestUrl());
			if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
				throw new ZosmfException("Unexpected HTTP status code: " + zosmfResponse.getStatusCode());
			}
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem wth " + method + " to zOSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse delete(String path, List<Integer> validStatusCodes) throws ZosmfException {
		String method = "DELETE";
		if (validStatusCodes == null) {
			validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
		}
		ZosmfResponseImpl zosmfResponse;
		try {
			setHeader(X_IBM_JOB_MODIFY_VERSION, "2.0");
			setHeader(X_IBM_REQUESTED_METHOD, method);
			zosmfResponse = new ZosmfResponseImpl(this.zosmfUrl, validPath(path));
			zosmfResponse.setHttpClientresponse(httpClient.deleteJson(validPath(path)));
			logger.debug(zosmfResponse.getStatusLine() + " - " + method + " " + zosmfResponse.getRequestUrl());
			if (!validStatusCodes.contains(zosmfResponse.getStatusCode())) {
				throw new ZosmfException("Unexpected HTTP status code: " + zosmfResponse.getStatusCode());
			}
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem with " + method + " to zOSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosImage getImage() {
		return this.image;
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
			throw new ZosmfException("Problem getting hostname for image " + image.getImageID(), e);
		}
		String zosmfPort;
		try {
			zosmfPort = ServerPort.get(image.getImageID());
		} catch (ZosmfManagerException e) {
			throw new ZosmfException("Problem getting port for zOSMF server on " + image.getImageID(), e);
		}
		this.zosmfUrl = "https://" + zosmfHostname + ":" + zosmfPort;

		this.httpClient = ZosmfManagerImpl.httpManager.newHttpClient();
		
		try {
			ICredentials creds = image.getDefaultCredentials();
			this.httpClient.setURI(new URI("https://" + zosmfHostname + ":" + zosmfPort));
			if (creds instanceof ICredentialsUsernamePassword) {
				this.httpClient.setAuthorisation(((ICredentialsUsernamePassword) creds).getUsername(), ((ICredentialsUsernamePassword) creds).getPassword());
			}
			this.httpClient.setTrustingSSLContext();
			this.httpClient.build();
		} catch (HttpClientException | ZosManagerException | URISyntaxException e) {
			throw new ZosmfException("Unable to create HTTP Client", e);
		}
	}

	@Override
	public String toString() {
		return this.image.getImageID() + " " + this.zosmfUrl;
	}
	
	
}
