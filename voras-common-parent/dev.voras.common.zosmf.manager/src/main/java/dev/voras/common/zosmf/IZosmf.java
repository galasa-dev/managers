package dev.voras.common.zosmf;

import org.apache.http.HttpStatus;

/**
 * 
 * Represents a zOS/MF server
 */
public interface IZosmf {
	
	/**
	 * Set an HTTP Header for the pending zOS/MF request
	 * 
	 * @param name header name
	 * @param value header value 
	 */
	public void setHeader(String name, String value);

	/**
	 * Issue an HTTP PUT request to the zOS/MF server with a request body of content type of {@code text/plain}
	 * <p>Expected status code {@link HttpStatus.SC_CREATED} (201)
	 * 
	 * @param path identifies the zOS/MF REST interface
	 * @param text the request body
	 * @return the zOS/MF server response
	 * @throws ZosmfException
	 */
	public IZosmfResponse putText(String path, String text) throws ZosmfException;

	/**
	 * Issue an HTTP PUT request to the zOS/MF server with no request body
	 * <p>Expected status code {@link HttpStatus.SC_CREATED} (201)
	 * 
	 * @param path identifies the zOS/MF REST interface
	 * @return the zOS/MF server response
	 * @throws ZosmfException
	 */
	public IZosmfResponse get(String path) throws ZosmfException;

	/**
	 * Issue an HTTP PUT request to the zOS/MF server with no request body
	 * <p>Expected status code {@link HttpStatus.SC_CREATED} (201)
	 * 
	 * @param path identifies the zOS/MF REST interface
	 * @return the zOS/MF server response
	 * @throws ZosmfException
	 */
	public IZosmfResponse getJson(String path) throws ZosmfException;

	/**
	 * Issue an HTTP DELETE request to the zOS/MF server with no request body
	 * <p>Expected status code {@link HttpStatus.SC_OK} (200)
	 * 
	 * @param path identifies the zOS/MF REST interface
	 * @return the zOS/MF server response
	 * @throws ZosmfException
	 */
	public IZosmfResponse deleteJson(String path) throws ZosmfException;
}