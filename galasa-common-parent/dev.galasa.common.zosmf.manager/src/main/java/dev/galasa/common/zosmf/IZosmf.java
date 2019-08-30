package dev.galasa.common.zosmf;

import com.google.gson.JsonObject;

import dev.galasa.common.zos.IZosImage;

/**
 * 
 * Represents a zOSMF server
 */
public interface IZosmf {
	
	/**
	 * Set an HTTP Header for the pending zOSMF request
	 * 
	 * @param name header name
	 * @param value header value 
	 */
	public void setHeader(String name, String value);

	/**
	 * Issue an HTTP PUT request to the zOSMF server with a request body of content type of {@code text/plain}
	 * 
	 * @param path identifies the zOSMF REST API
	 * @param body the request body
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public IZosmfResponse putText(String path, String body) throws ZosmfException;

	/**
	 * Issue an HTTP PUT request to the zOSMF server with a request body of content type of {@code application/json}
	 * 
	 * @param path identifies the zOSMF REST API
	 * @param body the request body
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public IZosmfResponse putJson(String path, JsonObject body) throws ZosmfException;

	/**
	 * Issue an HTTP PUT request to the zOSMF server with no request body
	 * 
	 * @param path identifies the zOSMF REST API
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public IZosmfResponse get(String path) throws ZosmfException;

	/**
	 * Issue an HTTP DELETE request to the zOSMF server with no request body
	 * 
	 * @param path identifies the zOSMF REST API
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public IZosmfResponse delete(String path) throws ZosmfException;
	
	/**
	 * Get the zOS image associated with the zOSMF server
	 * @return the zOS image
	 */
	public IZosImage getImage();
}