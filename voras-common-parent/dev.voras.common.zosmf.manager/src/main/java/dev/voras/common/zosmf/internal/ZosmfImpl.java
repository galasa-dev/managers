package dev.voras.common.zosmf.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import dev.voras.ICredentials;
import dev.voras.ICredentialsUsernamePassword;
import dev.voras.common.http.HttpClientException;
import dev.voras.common.http.IHttpClient;
import dev.voras.common.zos.IZosImage;
import dev.voras.common.zos.ZosManagerException;
import dev.voras.common.zosmf.IZosmf;
import dev.voras.common.zosmf.IZosmfResponse;
import dev.voras.common.zosmf.ZosmfException;
import dev.voras.common.zosmf.ZosmfManagerException;

/**
 * Implementation of {@link IZosmf}
 *
 */
public class ZosmfImpl implements IZosmf {
	//TODO: Retry invalid requests
	
	
	private static final Log logger = LogFactory.getLog(ZosmfImpl.class);
	
	private static final String X_IBM_JOB_MODIFY_VERSION = "X-IBM-Job-Modify-Version";
	private static final String X_IBM_REQUESTED_METHOD = "X-IBM-Requested-Method";
	//TODO: Investigate this
	private static final String X_IBM_BYPASS_STATUS = "X-IBM-Bypass-Status";

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
	public IZosmfResponse putText(String path, String text) throws ZosmfException {
		ZosmfResponseImpl zosmfResponse;
		try {
			setHeader(X_IBM_JOB_MODIFY_VERSION, "2.0");
			setHeader(X_IBM_REQUESTED_METHOD, "PUT");
			String url = this.zosmfUrl + validPath(path);
			zosmfResponse = new ZosmfResponseImpl(url);
			zosmfResponse.httpClientresponse = httpClient.putText(url, text);
			logger.debug(zosmfResponse.getStatusLine() + " - PUT " + url);
			if (zosmfResponse.getStatusCode() != HttpStatus.SC_CREATED) {
				//TODO
			}
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem with PUT to z/OSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse get(String path) throws ZosmfException {
		ZosmfResponseImpl zosmfResponse;
		try {
			setHeader(X_IBM_JOB_MODIFY_VERSION, "2.0");
			setHeader(X_IBM_REQUESTED_METHOD, "GET");
			String url = this.zosmfUrl + validPath(path);
			zosmfResponse = new ZosmfResponseImpl(url);
			zosmfResponse.httpClientresponse = httpClient.getText(url);
			logger.debug(zosmfResponse.getStatusLine() + " - GET " + url);
			if (zosmfResponse.getStatusCode() != HttpStatus.SC_CREATED) {
				//TODO
			}
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem wth GET to z/OSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse getJson(String path) throws ZosmfException {
		ZosmfResponseImpl zosmfResponse;
		try {
			setHeader(X_IBM_JOB_MODIFY_VERSION, "2.0");
			setHeader(X_IBM_REQUESTED_METHOD, "GET");
			String url = this.zosmfUrl + validPath(path);
			zosmfResponse = new ZosmfResponseImpl(url);
			zosmfResponse.httpClientresponse = httpClient.getJson(url);
			if (zosmfResponse.getStatusCode() != HttpStatus.SC_OK) {
				//TODO
			}
			logger.debug(zosmfResponse.getStatusLine() + " - GET " + url);
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem with GET to z/OSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse deleteJson(String path) throws ZosmfException {

		ZosmfResponseImpl zosmfResponse;
		try {
			setHeader(X_IBM_JOB_MODIFY_VERSION, "2.0");
			setHeader(X_IBM_REQUESTED_METHOD, "DELETE");
			String url = this.zosmfUrl + validPath(path);
			zosmfResponse = new ZosmfResponseImpl(url);
			zosmfResponse.httpClientresponse = httpClient.deleteJson(url);
			if (zosmfResponse.getStatusCode() != HttpStatus.SC_OK) {
				//TODO
			}
			logger.debug(zosmfResponse.getStatusLine() + " - DELETE " + url);
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem with GET to z/OSMF server", e);
		}
		
		return zosmfResponse;
	}

	private String validPath(String path) {
		return path.startsWith("/") ? path : "/" + path;
	}

	private void initialize() throws ZosmfException {
		
		String zosmfHostname;
		try {
			zosmfHostname = image.getDefaultHostname();
		} catch (ZosManagerException e) {
			throw new ZosmfException("Problem getting default hostname for image " + image.getImageID(), e);
		}
		String zosmfPort;
		try {
			zosmfPort = ZosmfManagerImpl.zosmfProperties.getZosmfPort(image.getImageID());
		} catch (ZosmfManagerException e) {
			throw new ZosmfException("Problem getting default port for Z/OSMF server on " + image.getImageID(), e);
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
}
