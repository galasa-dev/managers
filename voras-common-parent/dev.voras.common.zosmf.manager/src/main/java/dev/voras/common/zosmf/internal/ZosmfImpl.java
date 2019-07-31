package dev.voras.common.zosmf.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

public class ZosmfImpl implements IZosmf {
	
	private static final Log logger = LogFactory.getLog(ZosmfImpl.class);

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
	public IZosmfResponse putText(String uri, String text) throws ZosmfException {
		ZosmfResponseImpl zosmfResponse;
		try {
			String url = this.zosmfUrl + validUri(uri);
			zosmfResponse = new ZosmfResponseImpl(url);
			zosmfResponse.httpClientresponse = httpClient.putText(url, text);
			logger.debug(zosmfResponse.getStatusLine() + " - " + url);
			if (zosmfResponse.getStatusCode() != 201) {
				//TODO
			}
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem with PUT to z/OSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse getText(String uri) throws ZosmfManagerException {
		ZosmfResponseImpl zosmfResponse;
		try {
			String url = this.zosmfUrl + validUri(uri);
			zosmfResponse = new ZosmfResponseImpl(url);
			zosmfResponse.httpClientresponse = httpClient.getText(url);
			logger.debug(zosmfResponse.getStatusLine() + " - " + url);
			if (zosmfResponse.getStatusCode() != 201) {
				//TODO
			}
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem wth GET to z/OSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse getJson(String uri) throws ZosmfManagerException {
		ZosmfResponseImpl zosmfResponse;
		try {
			String url = this.zosmfUrl + validUri(uri);
			zosmfResponse = new ZosmfResponseImpl(url);
			zosmfResponse.httpClientresponse = httpClient.getJson(url);
			if (zosmfResponse.getStatusCode() != 200) {
				//TODO
			}
			logger.debug(zosmfResponse.getStatusLine() + " - " + url);
		} catch (MalformedURLException | HttpClientException  e) {
			throw new ZosmfException("Problem with GET to z/OSMF server", e);
		}
		
		return zosmfResponse;
	}

	@Override
	public IZosmfResponse getJsonArray(String uri) throws ZosmfManagerException {
		return getText(uri);
	}

	private String validUri(String uri) {
		return uri.startsWith("/") ? uri : "/" + uri;
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
