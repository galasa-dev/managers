package dev.voras.common.http.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.w3c.dom.Document;

import dev.voras.common.http.IHttpClient;
import dev.voras.common.http.HttpClientException;

public class HttpClientImpl implements IHttpClient{
	
	private final static String JAVA_VENDOR_PROPERTY = "java.vendor";
	
	private CloseableHttpClient httpClient;
	protected URI host = null;

	private final List<Header> commonHeaders = new ArrayList<Header>();
	
	private final int timeout = -1;

	private BasicCookieStore cookieStore;
	private SSLContext sslContext;
	private HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
	private CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	private HttpClientContext httpContext = null;
	private Set<Integer> okResponseCodes = new HashSet<Integer>();
	
	private Log logger;
	
	public HttpClientImpl(Log log) {
		this.logger = log;
		this.cookieStore = new BasicCookieStore();
		
	}
	
	@Override
	public void setURI(URI host) {
		this.host = host;
	}
	
	
	public void enableAuthCache() {
		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(new HttpHost(host.getHost(), host.getPort(), host.getScheme()), basicAuth);

		// Add AuthCache to the execution context
		httpContext = HttpClientContext.create();
		httpContext.setCredentialsProvider(credentialsProvider);
		httpContext.setAuthCache(authCache);
	}

	/**
	 * Get the SSL context used by this client
	 * 
	 * @return the {@link SSLContext} or null if there is none
	 */
	public SSLContext getSSLContext() {
		return sslContext;
	}

	/**
	 * Add a response code for the execute to ignore and treat as OK
	 * 
	 * @param responseCode
	 */
	public void addOkResponseCode(int responseCode) {
		okResponseCodes.add(responseCode);

		return;
	}

	/**
	 * Set the SSL Context to a Trust All context
	 * 
	 * @return the updated client
	 * @throws HttpClientException 
	 */
	public IHttpClient setTrustingSSLContext() throws HttpClientException {

		try {
			boolean ibmJdk = System.getProperty(JAVA_VENDOR_PROPERTY).contains("IBM");
			SSLContext sslContext;
			if(ibmJdk)
				sslContext = SSLContext.getInstance("SSL_TLSv2");
			else
				sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, new TrustManager[] { new VeryTrustingTrustManager() }, new SecureRandom());
			setSSLContext(sslContext);
		} catch (GeneralSecurityException e) {
			throw new HttpClientException("Error attempting to create SSL context", e);
		}

		return this;
	}	

	/**
	 * Set up Client Authentication SSL Context and install
	 * 
	 * @param clientKeyStore
	 * @param serverKeyStore
	 * @param alias
	 * @param password
	 * @return  the updated client
	 * @throws HttpClientException
	 */
	public IHttpClient setupClientAuth(KeyStore clientKeyStore, KeyStore serverKeyStore, String alias, String password) throws HttpClientException {

		try {
			// Create the Key Manager Factory
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(clientKeyStore, password.toCharArray());

			// Create the Trust Managers
			TrustManager trustManagers[] = { new ClientAuthTrustManager(serverKeyStore, alias) };

			// Create the SSL Conetext
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), trustManagers, null);

			setSSLContext(ctx);
		} catch (GeneralSecurityException e) {
			throw new HttpClientException("Error attempting to create SSL context", e);
		}

		return this;
	}


	/**
	 * Set the SSL Context
	 * 
	 * @param sslContext
	 * @return the updated client
	 */
	public IHttpClient setSSLContext(SSLContext sslContext) {

		this.sslContext = sslContext;

		return this;
	}

	/**
	 * Set the hostname verifier

	 * 
	 * @param hostnameVerifier
	 * @return the updated client
	 */
	public IHttpClient setHostnameVerifier(HostnameVerifier hostnameVerifier) {

		this.hostnameVerifier = hostnameVerifier;

		return this;
	}

	/**
	 * Set the hostname verifier to a no-op verifier
	 * 
	 * @return the updated client
	 */
	public IHttpClient setNoopHostnameVerifier() {

		this.hostnameVerifier = NoopHostnameVerifier.INSTANCE;

		return this;
	}

	/**
	 * Get the username set for this client
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return credentialsProvider.getCredentials(AuthScope.ANY).getUserPrincipal().getName();
	}

	/**
	 * Get the username set for this client for a specific scope
	 * 
	 * @param scope
	 * @return the username
	 */
	public String getUsername(URI scope) {
		return credentialsProvider.getCredentials(new AuthScope(scope.getHost(), scope.getPort())).getUserPrincipal().getName();
	}

	/**
	 * Set the username and password for all scopes
	 * 
	 * @param username
	 * @param password
	 * @return the updated client
	 */
	public IHttpClient setAuthorisation(String username, String password) {
		credentialsProvider.clear();
		credentialsProvider.setCredentials(AuthScope.ANY, 
				new UsernamePasswordCredentials(username, password));

		return this;
	}

	/**
	 * Set the username and password for a specific scope
	 * 
	 * @param username
	 * @param password
	 * @param scope
	 * @return the updated client
	 */
	public IHttpClient setAuthorisation(String username, String password, URI scope) {
		credentialsProvider.setCredentials(new AuthScope(scope.getHost(), scope.getPort()), 
				new UsernamePasswordCredentials(username, password));

		return this;
	}

	/**
	 * Build the client
	 * 
	 * @return the built client
	 */
	public IHttpClient build() {

		HttpClientBuilder builder = HttpClients.custom().setDefaultCookieStore(cookieStore);
		builder.setDefaultCredentialsProvider(credentialsProvider);
		builder.setDefaultHeaders(commonHeaders);
		
		if (timeout > 0) {
			RequestConfig.Builder requestBuilder = RequestConfig.custom()
					.setConnectTimeout(timeout)
			        .setConnectionRequestTimeout(timeout)
			        .setSocketTimeout(timeout);
			builder.setDefaultRequestConfig(requestBuilder.build());
		}

		if (sslContext != null) {
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
			builder.setSSLSocketFactory(csf);
		}

		httpClient = builder.build();

		return this;
	}

	private void addHeaders(AbstractHttpMessage message,
			ContentType contentType, ContentType[] acceptTypes) {

		if (contentType != null) {
			message.addHeader(HttpHeaders.CONTENT_TYPE, contentType.getC().getMimeType());
		}

		if (acceptTypes.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (ContentType acceptType : acceptTypes) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(acceptType.getMimeType());
			}

			message.addHeader(HttpHeaders.ACCEPT, sb.toString());
		}

		for (Header header : commonHeaders) {
			message.addHeader(header);
		}

	}

	private byte[] execute(HttpUriRequest request, boolean retry)
			throws HttpClientException {

		while (true) {
			CloseableHttpResponse response = null;
			try {

				response = httpClient.execute(request, httpContext);
				StatusLine status = response.getStatusLine();
				if (status.getStatusCode() != HttpStatus.SC_OK 
						&& status.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY 
						&& !okResponseCodes.contains(status.getStatusCode())) {
					String message = "HTTP " + request.getMethod() + " to "
							+ request.getURI().toASCIIString() + " failed with "
							+ status.getStatusCode() + ": '"
							+ status.getReasonPhrase() + "'";

					if (retry && status.getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
						logger.warn(message + ", retrying");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							throw new HttpClientException(
									"JAT HTTP Client retry failed due to interruption",
									e1);
						}
						continue;
					}
					throw new HttpClientException(message);
				}

				HttpEntity entity = response.getEntity();

				byte[] content = IOUtils.toByteArray(entity.getContent());

				return content;

			} catch (Exception e) {
				throw new HttpClientException(e);
			} finally {

				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						logger.error(
								"Exception received when trying to close an http response from "
										+ request.getURI().toASCIIString(), e);
					}
				}
			}
		}
	}

	private URI buildUri(String path, Map<String, String> queryParams)
			throws HttpClientException {

		if (queryParams == null) {
			queryParams = new HashMap<String, String>();
		}

		// Create a multi-valued map since we can have more than one value for each param in the path
		Map<String,List<String>> multiMap = new HashMap<String,List<String>>();
		for(Entry<String,String> entry : queryParams.entrySet()) {
			List<String> list = new ArrayList<String>();
			list.add(entry.getValue());
			multiMap.put(entry.getKey(), list);
		}

		Pattern p = Pattern.compile("^(.*)\\?((?:.+=.*&?)+)$", Pattern.MULTILINE);
		Matcher m = p.matcher(path);
		if (m.find()) {
			path = m.group(1);

			String[] pairs = m.group(2).split("&");
			for (String pair : pairs) {
				String[] parts = pair.split("=");
				if (parts.length != 2) {
					throw new HttpClientException("Illegal query parameter found: '" + pair + "'");
				}

				try {
					String param = URLDecoder.decode(parts[0], "UTF-8");
					String value = URLDecoder.decode(parts[1], "UTF-8");
					if(multiMap.containsKey(param)) {
						multiMap.get(param).add(value);
					} else {
						List<String> list = new ArrayList<String>();
						list.add(value);
						multiMap.put(param, list);
					}
				} catch (UnsupportedEncodingException e) {
					throw new HttpClientException("Unable to decode query parameter: '" + pair + "'", e);
				}
			}
		}

		URIBuilder ub = new URIBuilder(host);
		appendPath(ub, path);

		// Iterate through the multi-value map to add all the parameters
		for(Entry<String,List<String>> entry : multiMap.entrySet()) {
			for(String value : entry.getValue()) {
				ub.addParameter(entry.getKey(), value);
			}
		}

		try {
			return ub.build();
		} catch (URISyntaxException e) {
			throw new HttpClientException(
					"Cannot construct URI using path: '" + path + "'", e);
		}
	}

	private void appendPath(URIBuilder ub, String path) {

		if (path.isEmpty()) {
			return;
		}

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		String commonPath = ub.getPath();

		if (commonPath != null) {
			if (!path.toLowerCase().startsWith(commonPath.toLowerCase())) {
				path = commonPath + path;
			}
		}
		ub.setPath(path);
	}

	private Object unmarshall(byte[] content, Class<?>[] jaxbClasses) throws HttpClientException {

		try {
			if (jaxbClasses != null && jaxbClasses.length > 0) {
				JAXBContext ctx = JAXBContext.newInstance(jaxbClasses);
				Object o = ctx.createUnmarshaller().unmarshal(new ByteArrayInputStream(content));
				return o;
			}
		} catch (JAXBException e) {
		}

		return new String(content);

	}

	private byte[] marshall(Object object, Class<?>[] jaxbClasses) throws HttpClientException {

		if (object == null) {
			return new byte[0];
		}

		if (object.getClass().isAnnotationPresent(XmlType.class) &&
				jaxbClasses != null && jaxbClasses.length > 0) {
			try {
				JAXBContext ctx = JAXBContext.newInstance(jaxbClasses);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ctx.createMarshaller().marshal(object, os);

				return os.toByteArray();
			} catch (JAXBException e) {
				throw new HttpClientException(e);
			}
		} else {
			return ((String) object).getBytes();
		}
	}

	@Override
	public Object get(String path, boolean retry,
			Class<?>... jaxbClasses) throws HttpClientException {
		return get(path, null, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON }, 
				jaxbClasses, retry);
	}

	@Override
	public Object get(String path,
			Map<String, String> queryParams, boolean retry, Class<?>... jaxbClasses) throws HttpClientException {
		return get(path, queryParams, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON }, 
				jaxbClasses, retry);
	}
	
	@Override
	public String get(String path) throws HttpClientException {

		return get(path, false);
	}
	
	@Override
	public String get(String path, boolean retry) throws HttpClientException {

		return (String) get(path, null, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}

	@Override
	public Object get(String path, Map<String, String> queryParams,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry)
					throws HttpClientException {

		HttpGet get = new HttpGet(buildUri(path, queryParams));
		addHeaders(get, null, acceptTypes);

		byte[] response = execute(get, retry);

		return unmarshall(response, jaxbClasses);
	}

	@Override
	public Object delete(String path, boolean retry)
			throws HttpClientException {

		return delete(path, null, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}

	@Override
	public Object delete(String path, Map<String, String> queryParams,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry)
					throws HttpClientException {

		HttpDelete delete = new HttpDelete(buildUri(path, queryParams));
		addHeaders(delete, null, acceptTypes);

		byte[] response = execute(delete, retry);

		return unmarshall(response, jaxbClasses);
	}
	@Override
	public Object putText(String path, String data, boolean retry)
			throws HttpClientException {

		return put(path, null, ContentType.TEXT_PLAIN, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}

	@Override
	public Object putText(String path, String data,
			Map<String, String> queryParams, boolean retry) throws HttpClientException {
		return put(path, queryParams, ContentType.TEXT_PLAIN, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}

	@Override
	public Object putXml(String path, String data, boolean retry)
			throws HttpClientException {

		return put(path, null, ContentType.APPLICATION_XML, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}

	@Override
	public Object putJson(String path, String data, boolean retry)
			throws HttpClientException {

		return put(path, null, ContentType.APPLICATION_JSON, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}
	@Override
	public Object put(String path, Map<String, String> queryParams,
			ContentType contentType, Object data,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry)
					throws HttpClientException {

		byte[] dataBytes = marshall(data, jaxbClasses);
		data = null;
		
		HttpPut put = new HttpPut(buildUri(path, queryParams));
		put.setEntity(new ByteArrayEntity(dataBytes));
		addHeaders(put, contentType, acceptTypes);

		byte[] response = execute(put, retry);
		dataBytes = null;
		
		return unmarshall(response, jaxbClasses);
	}
	
	@Override
	public Object putMultipart(String path, List<RequestPart> parts, boolean retry) throws HttpClientException {
		return putMultipart(path, parts, null, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}
	
	@Override
	public Object putMultipart(String path, List<RequestPart> parts,
			Map<String, String> queryParams, ContentType[] acceptTypes, 
			Class<?>[] jaxbClasses, boolean retry) throws HttpClientException {

		String boundary = "arbitraryBoundaryString";
		MultipartEntityBuilder meb = MultipartEntityBuilder.create();
		meb.setMode(HttpMultipartMode.STRICT);
		meb.setBoundary(boundary);

		for (RequestPart part : parts) {
			meb.addPart(part.getType(), part.getBody());
		}

		HttpPut put = new HttpPut(buildUri(path, queryParams));
		put.setEntity(meb.build());
		addHeaders(put, null, acceptTypes);
		put.addHeader("Content-Type",
				"multipart/mixed; boundary=" + boundary);

		byte[] response = execute(put, retry);

		return unmarshall(response, jaxbClasses);
	}

	@Override
	public Object postText(String path, String data, boolean retry)
			throws HttpClientException {

		return post(path, null, ContentType.TEXT_PLAIN, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}
	
	@Override
	public Object postTextAsXML(String path, String data, boolean retry)
			throws HttpClientException {

		return post(path, null, ContentType.TEXT_XML, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN, ContentType.TEXT_XML }, null, retry);
	}

	@Override
	public Object postXml(String path, String data, boolean retry)
			throws HttpClientException {

		return post(path, null, ContentType.APPLICATION_XML, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}

	@Override
	public Object postJson(String path, String data, boolean retry)
			throws HttpClientException {

		return post(path, null, ContentType.APPLICATION_JSON, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN }, null, retry);
	}

	@Override
	public Object post(String path, Map<String, String> queryParams,
			ContentType contentType, Object data,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry)
					throws HttpClientException {

		byte[] dataBytes = marshall(data, jaxbClasses);

		HttpPost post = new HttpPost(buildUri(path, queryParams));
		post.setEntity(new ByteArrayEntity(dataBytes));
		addHeaders(post, contentType, acceptTypes);

		byte[] response = execute(post, retry);

		return unmarshall(response, jaxbClasses);
	}

	@Override
	public Object postForm(String path, Map<String, String> queryParams,
			HashMap<String, String> fields,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses,
			boolean retry)
					throws HttpClientException {

		HttpPost post = new HttpPost(buildUri(path, queryParams));
		addHeaders(post, ContentType.APPLICATION_FORM_URLENCODED, acceptTypes);

		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
		for(Entry<String, String> field : fields.entrySet()) {
			nvps.add(new BasicNameValuePair(field.getKey(), field.getValue()));
		}
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e) {
			throw new HttpClientException("Unable to encode form", e);
		}

		byte[] response = execute(post, retry);

		return unmarshall(response, null);
	}

	@Override
	public Object postJAXB(String path, Object data, boolean retry,
			Class<?>... jaxbClasses) throws HttpClientException {
		return post(path, null, ContentType.APPLICATION_XML, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN}, jaxbClasses, retry);
	}

	@Override
	public Object postJAXB(String path, Object data,
			Map<String, String> queryParams, boolean retry, Class<?>... jaxbClasses) throws HttpClientException {
		return post(path, queryParams, ContentType.APPLICATION_XML, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN}, jaxbClasses, retry);
	}

	
	@Override
	public Object putJAXB(String path, Object data, boolean retry,
			Class<?>... jaxbClasses) throws HttpClientException {
		return put(path, null, ContentType.APPLICATION_XML, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN}, jaxbClasses, retry);
	}

	
	@Override
	public Object putJAXB(String path, Object data,
			Map<String, String> queryParams, boolean retry, Class<?>... jaxbClasses) throws HttpClientException {
		return put(path, queryParams, ContentType.APPLICATION_XML, data, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN}, jaxbClasses, retry);
	}

	
	@Override
	public Object delete(String path, Map<String, String> queryParams,
			boolean retry, Class<?>[] jaxbClasses) throws HttpClientException {
		return delete(path, queryParams, new ContentType[] {
				ContentType.APPLICATION_XML, ContentType.APPLICATION_JSON,
				ContentType.TEXT_PLAIN}, jaxbClasses, retry);
	}

	
	@Override
	public void addCommonHeader(String name, String value) {
		commonHeaders.add(new BasicHeader(name, value));

		return;
	}

	
	@Override
	public HttpClientResponse<byte[]> executeByteRequest(
			HttpClientRequest request) throws HttpClientException {

		return HttpClientResponse.byteResponse(execute(request.buildRequest()));
	}

	
	@Override
	public HttpClientResponse<String> getText(String url) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newGetRequest(url,
				new ContentType[] { ContentType.TEXT_PLAIN });

		return executeTextRequest(request);
	}

	
	@Override
	public HttpClientResponse<String> putText(String url, String text) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newPutRequest(url,
				new ContentType[] { ContentType.TEXT_PLAIN },
				ContentType.TEXT_PLAIN);
		request.setBody(text);

		return executeTextRequest(request);
	}

	
	@Override
	public HttpClientResponse<String> postText(String url, String text) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newPostRequest(url,
				new ContentType[] { ContentType.TEXT_PLAIN },
				ContentType.TEXT_PLAIN);
		request.setBody(text);

		return executeTextRequest(request);
	}

	
	@Override
	public HttpClientResponse<String> deleteText(String url) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newDeleteRequest(url,
				new ContentType[] { ContentType.TEXT_PLAIN });

		return executeTextRequest(request);
	}

	
	@Override
	public HttpClientResponse<String> executeTextRequest(
			HttpClientRequest request) throws HttpClientException {

		return HttpClientResponse.textResponse(execute(request.buildRequest()));
	}

	
	@Override
	public HttpClientResponse<JSONObject> getJson(String url) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newGetRequest(url,
				new ContentType[] { ContentType.APPLICATION_JSON });

		return executeJsonRequest(request);
	}

	
	@Override
	public HttpClientResponse<JSONObject> putJson(String url, JSONObject json) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newPutRequest(url,
				new ContentType[] { ContentType.APPLICATION_JSON },
				ContentType.APPLICATION_JSON);
		request.setJSONBody(json);

		return executeJsonRequest(request);
	}

	
	@Override
	public HttpClientResponse<JSONObject> postJson(String url, JSONObject json) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newPostRequest(url,
				new ContentType[] { ContentType.APPLICATION_JSON },
				ContentType.APPLICATION_JSON);
		request.setJSONBody(json);

		return executeJsonRequest(request);
	}

	
	@Override
	public HttpClientResponse<JSONObject> deleteJson(String url) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newDeleteRequest(url,
				new ContentType[] { ContentType.APPLICATION_JSON });

		return executeJsonRequest(request);
	}

	
	@Override
	public HttpClientResponse<JSONObject> executeJsonRequest(
			HttpClientRequest request) throws HttpClientException {

		return HttpClientResponse.jsonResponse(execute(request.buildRequest()));
	}

	
	@Override
	public HttpClientResponse<Document> executeXmlRequest(
			HttpClientRequest request) throws HttpClientException {

		return HttpClientResponse.xmlResponse(execute(request.buildRequest()));
	}

	
	@Override
	public HttpClientResponse<Object> getJaxb(String url,
			Class<?>... responseTypes) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newGetRequest(url,
				new ContentType[] { ContentType.APPLICATION_XML });

		return executeJaxbRequest(request, responseTypes);
	}

	
	@Override
	public HttpClientResponse<Object> putJaxb(String url, Object jaxbObject,
			Class<?>... responseTypes) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newPutRequest(url,
				new ContentType[] { ContentType.APPLICATION_XML },
				ContentType.APPLICATION_XML);
		request.setJAXBBody(jaxbObject);

		return executeJaxbRequest(request, responseTypes);
	}

	
	@Override
	public HttpClientResponse<Object> postJaxb(String url, Object jaxbObject,
			Class<?>... responseTypes) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newPostRequest(url,
				new ContentType[] { ContentType.APPLICATION_XML },
				ContentType.APPLICATION_XML);
		request.setJAXBBody(jaxbObject);

		return executeJaxbRequest(request, responseTypes);
	}

	@Override
	public HttpClientResponse<Object> deleteJaxb(String url,
			Class<?>... responseTypes) throws HttpClientException {

		HttpClientRequest request = HttpClientRequest.newDeleteRequest(url,
				new ContentType[] { ContentType.APPLICATION_XML });

		return executeJaxbRequest(request, responseTypes);
	}

	
	@Override
	public HttpClientResponse<Object> executeJaxbRequest(
			HttpClientRequest request, Class<?>... responseTypes) throws HttpClientException {

		return HttpClientResponse.jaxbResponse(execute(request.buildRequest()),
				responseTypes);
	}

	
	@Override
	public HttpClientResponse<String> head(String url) throws HttpClientException {
		HttpClientRequest request = HttpClientRequest.newHeadRequest(url);

		return HttpClientResponse.textResponse(execute(request.buildRequest()));
	}

	private CloseableHttpResponse execute(HttpUriRequest request) throws HttpClientException {

		//		CloseableHttpResponse response = null;

		for (Header header : commonHeaders) {
			request.addHeader(header);
		}

		//		try {

		try {
			return httpClient.execute(request, httpContext);
		} catch (IOException e) {
			throw new HttpClientException("Error executing http request", e);
		}
	}
	@Override
	public void close() {
		try {
			httpClient.close();
		} catch (IOException e) {
		}
		
	}

}
