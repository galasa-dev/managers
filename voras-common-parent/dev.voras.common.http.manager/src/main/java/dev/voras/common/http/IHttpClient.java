package dev.voras.common.http;

import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.json.JSONObject;
import org.w3c.dom.Document;

import dev.voras.common.http.HttpClientException;

import dev.voras.common.http.internal.ContentType;
import dev.voras.common.http.internal.HttpClientRequest;
import dev.voras.common.http.internal.HttpClientResponse;
import dev.voras.common.http.internal.RequestPart;

public interface IHttpClient {
	/**
	 * Add a header that will be used on all http requests
	 * 
	 * @param name
	 * @param value
	 */
	void addCommonHeader(String name, String value);
	
	/**
	 * Add a response code for the execute to ignore and treat as OK
	 * 
	 * @param responseCode
	 */
	public void addOkResponseCode(int responseCode);
	
	/**
	 * Build the client
	 * 
	 * @return the built client
	 */
	public IHttpClient build();
	
	/**
	 * close the underlying HTTPClient
	 */
	public void close();
	
	/**
	 * Issue DELETE request to the given path, request will be retried if retry is set
	 * @param path
	 * @param retry
	 * @return
	 * @throws HttpClientException
	 */
	Object delete(String path, boolean retry) throws HttpClientException;
	
	/**
	 * Issue DELETE request to the given path with the given query parameters. If JAXB classes are expected to be returned they must also be passed.
	 * 
	 * @param path
	 * @param queryParams
	 * @param retry
	 * @param jaxbClasses
	 * @return
	 * @throws HttpClientException
	 */
	Object delete(String path, Map<String, String> queryParams, boolean retry, Class<?>[] jaxbClasses)
			throws HttpClientException;
	
	/**
	 * Issue DELETE request to the given path with the given query parameters. If JAXB classes are expected to be returned they must also be passed.
	 * @param path
	 * @param queryParams
	 * @param acceptTypes
	 * @param jaxbClasses
	 * @param retry
	 * @return
	 * @throws HttpClientException
	 */
	Object delete(String path, Map<String, String> queryParams, ContentType[] acceptTypes, Class<?>[] jaxbClasses,
			boolean retry) throws HttpClientException;
	
	/**
	 * Issue an HTTP DELETE to the provided URL, receiving a JAXB Object in the
	 * response. In order to unmarshal the response, an array of possible
	 * response classes must be provided in responseTypes
	 * 
	 * @param url
	 * @param responseTypes
	 * @return - {@link HttpClientResponse} with a JAXB content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<Object> deleteJaxb(String url, Class<?>[] responseTypes) throws HttpClientException;
	
	/**
	 * Issue an HTTP DELETE to the provided URL, receiving a {@link JSONObject}
	 * in the response.
	 * 
	 * @param url
	 * @return - {@link HttpClientResponse} with a {@link JSONObject} content
	 *         type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<JSONObject> deleteJson(String url) throws HttpClientException;
	
	/**
	 * Issue an HTTP DELETE to the provided URL, receiving a {@link String} in
	 * the response.
	 * 
	 * @param url
	 * @return - {@link HttpClientResponse} with a {@link String} content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<String> deleteText(String url) throws HttpClientException;
	
	/**
	 * Execute an {@link HttpClientRequest} returning a byte array available
	 * through the returned {@link HttpClientResponse}.
	 *
	 * @param request
	 * @return - {@link HttpClientResponse} with a byte array content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<byte[]> executeByteRequest(HttpClientRequest request) throws HttpClientException;
	
	/**
	 * Execute an {@link HttpClientRequest} returning a JAXB object available
	 * through the returned {@link HttpClientResponse}. In order to unmarshal
	 * the response, an array of possible response classes must be provided in
	 * responseTypes
	 * 
	 * @param request
	 * @param responseTypes
	 * @return - {@link HttpClientResponse} with a JAXB content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<Object> executeJaxbRequest(HttpClientRequest request, Class<?>[] responseTypes)
			throws HttpClientException;
	
	/**
	 * Execute an {@link HttpClientRequest} returning a {@link JSONObject}
	 * available through the returned {@link HttpClientResponse}.
	 * 
	 * @param request
	 * @return - {@link HttpClientResponse} with a {@link JSONObject} content
	 *         type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<JSONObject> executeJsonRequest(HttpClientRequest request) throws HttpClientException;

	/**
	 * Execute an {@link HttpClientRequest} returning a {@link String} available
	 * through the returned {@link HttpClientResponse}.
	 * 
	 * @param request
	 * @return - {@link HttpClientResponse} with a {@link String} content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<String> executeTextRequest(HttpClientRequest request) throws HttpClientException;
	
	/**
	 * Execute an {@link HttpClientRequest} returning a {@link Document}
	 * available through the returned {@link HttpClientResponse}.
	 * 
	 * @param request
	 * @return - {@link HttpClientResponse} with a {@link Document} content
	 *         type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<Document> executeXmlRequest(HttpClientRequest request) throws HttpClientException;

	/**
	 * Issue a GET request against the given path 
	 * @param path
	 * @return
	 * @throws HttpClientException
	 */
	String get(String path) throws HttpClientException;

	/**
	 * Issue a GET request against the given path, will retry if retry is set to true
	 * @param path
	 * @param retry
	 * @return
	 * @throws HttpClientException
	 */
	String get(String path, boolean retry) throws HttpClientException;

	/**
	 * Issue a GET request against the given path, will retry as required. If JAXB classes are expected to be returned they must also be passed.
	 * @param path
	 * @param retry
	 * @param jaxbClasses
	 * @return
	 * @throws HttpClientException
	 */
	Object get(String path, boolean retry, Class<?>[] jaxbClasses) throws HttpClientException;

	/**
	 * 
	 * @param path
	 * @param queryParams
	 * @param retry
	 * @param jaxbClasses
	 * @return
	 * @throws HttpClientException
	 */
	Object get(String path, Map<String, String> queryParams, boolean retry, Class<?>[] jaxbClasses)
			throws HttpClientException;

	Object get(String path, Map<String, String> queryParams, ContentType[] acceptTypes, Class<?>[] jaxbClasses,
			boolean retry) throws HttpClientException;

	/**
	 * Issue an HTTP GET to the provided URL, receiving a JAXB Object in the
	 * response. In order to unmarshal the response, an array of possible
	 * response classes must be provided in responseTypes
	 * 
	 * @param url
	 * @param responseTypes
	 * @return - {@link HttpClientResponse} with a JAXB content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<Object> getJaxb(String url, Class<?>[] responseTypes) throws HttpClientException;

	/**
	 * Issue an HTTP GET to the provided URL, receiving a {@link JSONObject} in
	 * the response.
	 * 
	 * @param url
	 * @return - {@link HttpClientResponse} with a {@link JSONObject} content
	 *         type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<JSONObject> getJson(String url) throws HttpClientException;

	/**
	 * Get the SSL context used by this client
	 * 
	 * @return the {@link SSLContext} or null if there is none
	 */
	public SSLContext getSSLContext();

	/**
	 * Issue an HTTP GET to the provided URL, receiving a {@link String} in the
	 * response.
	 * 
	 * @param url
	 * @return - {@link HttpClientResponse} with a {@link String} content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<String> getText(String url) throws HttpClientException;

	/**
	 * Get the username set for this client
	 * 
	 * @return the username
	 */
	public String getUsername();

	/**
	 * Get the username set for this client for a specific scope
	 * 
	 * @param scope
	 * @return the username
	 */
	public String getUsername(URI scope);

	/**
	 * Execute an {@link HttpClientRequest} returning a JAXB object available
	 * through the returned {@link HttpClientResponse}. In order to unmarshal
	 * the response, an array of possible response classes must be provided in
	 * responseTypes	 * 
	 * 
	 * @param url
	 * @return - {@link HttpClientResponse}
	 * @throws HttpClientException 
	 */
	HttpClientResponse<String> head(String url) throws HttpClientException;

	Object post(String path, Map<String, String> queryParams, ContentType contentType, Object data,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException;

	Object postForm(String path, Map<String, String> queryParams, HashMap<String, String> fields,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException;

	/**
	 * Issue an HTTP POST to the provided URL, sending the provided jaxbObject
	 * and receiving a JAXB Object in the response. In order to unmarshal the
	 * response, an array of possible response classes must be provided in
	 * responseTypes
	 * 
	 * @param url
	 * @param jaxbObject
	 * @param responseTypes
	 * @return - {@link HttpClientResponse} with a JAXB content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<Object> postJaxb(String url, Object jaxbObject, Class<?>[] responseTypes)
			throws HttpClientException;

	Object postJAXB(String path, Object data, boolean retry, Class<?>[] jaxbClasses) throws HttpClientException;

	Object postJAXB(String path, Object data, Map<String, String> queryParams, boolean retry, Class<?>[] jaxbClasses)
			throws HttpClientException;

	/**
	 * Issue an HTTP POST to the provided URL, sending the provided
	 * {@link JSONObject} and receiving a {@link JSONObject} in the response.
	 * 
	 * @param url
	 * @param json
	 * @return - {@link HttpClientResponse} with a {@link JSONObject} content
	 *         type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<JSONObject> postJson(String url, JSONObject json) throws HttpClientException;

	Object postJson(String path, String data, boolean retry) throws HttpClientException;

	/**
	 * Issue an HTTP POST to the provided URL, sending the provided
	 * {@link String} and receiving a {@link String} in the response.
	 * 
	 * @param url
	 * @param text
	 * @return - {@link HttpClientResponse} with a {@link String} content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<String> postText(String url, String text) throws HttpClientException;

	Object postText(String path, String data, boolean retry) throws HttpClientException;

	Object postTextAsXML(String path, String data, boolean retry) throws HttpClientException;

	Object postXml(String path, String data, boolean retry) throws HttpClientException;

	Object put(String path, Map<String, String> queryParams, ContentType contentType, Object data,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException;

	/**
	 * Issue an HTTP PUT to the provided URL, sending the provided jaxbObject
	 * and receiving a JAXB Object in the response. In order to unmarshal the
	 * response, an array of possible response classes must be provided in
	 * responseTypes
	 * 
	 * @param url
	 * @param jaxbObject
	 * @param responseTypes
	 * @return - {@link HttpClientResponse} with a JAXB content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<Object> putJaxb(String url, Object jaxbObject, Class<?>[] responseTypes)
			throws HttpClientException;

	/**
	 * Issue PUT of the passed data object request to the given path. If JAXB classes are used as input, or are expected to be returned they must also be passed.
	 * 
	 * @param path
	 * @param data
	 * @param retry
	 * @param jaxbClasses
	 * @return
	 * @throws HttpClientException
	 */
	Object putJAXB(String path, Object data, boolean retry, Class<?>[] jaxbClasses) throws HttpClientException;

	/**
	 * Issue PUT of the passed data object request to the given path with the given query parameters. If JAXB classes are used as input, or are expected to be returned they must also be passed.
	 * 
	 * @param path
	 * @param data
	 * @param queryParams
	 * @param retry
	 * @param jaxbClasses
	 * @return
	 * @throws HttpClientException
	 */
	Object putJAXB(String path, Object data, Map<String, String> queryParams, boolean retry, Class<?>[] jaxbClasses)
			throws HttpClientException;

	/**
	 * Issue an HTTP PUT to the provided URL, sending the provided
	 * {@link JSONObject} and receiving a {@link JSONObject} in the response.
	 * 
	 * @param url
	 * @param json
	 * @return - {@link HttpClientResponse} with a {@link JSONObject} content
	 *         type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<JSONObject> putJson(String url, JSONObject json) throws HttpClientException;

	Object putJson(String path, String data, boolean retry) throws HttpClientException;

	Object putMultipart(String path, List<RequestPart> parts, boolean retry) throws HttpClientException;

	Object putMultipart(String path, List<RequestPart> parts, Map<String, String> queryParams,
			ContentType[] acceptTypes, Class<?>[] jaxbClasses, boolean retry) throws HttpClientException;

	/**
	 * Issue an HTTP PUT to the provided URL, sending the provided
	 * {@link String} and receiving a {@link String} in the response.
	 * 
	 * @param url
	 * @param text
	 * @return - {@link HttpClientResponse} with a {@link String} content type
	 * @throws HttpClientException 
	 */
	HttpClientResponse<String> putText(String url, String text) throws HttpClientException;

	Object putText(String path, String data, boolean retry) throws HttpClientException;

	Object putText(String path, String data, Map<String, String> queryParams, boolean retry) throws HttpClientException;

	Object putXml(String path, String data, boolean retry) throws HttpClientException;

	/**
	 * Set the username and password for all scopes
	 * 
	 * @param username
	 * @param password
	 * @return the updated client
	 */
	public IHttpClient setAuthorisation(String username, String password);

	/**
	 * Set the username and password for a specific scope
	 * 
	 * @param username
	 * @param password
	 * @param scope
	 * @return the updated client
	 */
	public IHttpClient setAuthorisation(String username, String password, URI scope);

	/**
	 * Set the hostname verifier

	 * 
	 * @param hostnameVerifier
	 * @return the updated client
	 */
	public IHttpClient setHostnameVerifier(HostnameVerifier hostnameVerifier);

	/**
	 * Set the hostname verifier to a no-op verifier
	 * 
	 * @return the updated client
	 */
	public IHttpClient setNoopHostnameVerifier();

	/**
	 * Set the SSL Context
	 * 
	 * @param sslContext
	 * @return the updated client
	 */
	public IHttpClient setSSLContext(SSLContext sslContext);

	/**
	 * Set the SSL Context to a Trust All context
	 * 
	 * @return the updated client
	 * @throws HttpClientException 
	 */
	public IHttpClient setTrustingSSLContext() throws HttpClientException;

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
	public IHttpClient setupClientAuth(KeyStore clientKeyStore, KeyStore serverKeyStore, String alias, String password) throws HttpClientException;

	/**
	 * Set the URI endpoint for this client
	 * @param host
	 */
	public void setURI(URI host);

}


