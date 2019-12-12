/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosmf;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;

/**
 * 
 * Represents a zOSMF server
 */
public interface IZosmf {
	
	/**
	 * Enumeration of zOSMF request types:
	 * <li>{@link #POST}</li>
	 * <li>{@link #POST_JSON}</li>
	 * <li>{@link #GET}</li>
	 * <li>{@link #PUT}</li>
	 * <li>{@link #PUT_TEXT}</li>
	 * <li>{@link #PUT_JSON}</li>
	 * <li>{@link #DELETE}</li>
	 */
	public enum ZosmfRequestType {
		/**
		 * POST method with no request body
		 */
		POST,
		/**
		 * POST method with JSON request body
		 */
		POST_JSON,
		/**
		 * GET method with no request body
		 */
		GET,
		/**
		 * PUT method with no request body
		 */
		PUT,
		/**
		 * PUT method with TEXT request body
		 */
		PUT_TEXT,
		/**
		 * PUT method with JSON request body
		 */
		PUT_JSON,
		/**
		 * DELETE method with no request body
		 */
		DELETE;
	}
	
	/**
	 * Enumeration of zOSMF Custom HTTP headers:
	 * <li>{@link #X_IBM_ATTRIBUTES}</li>
	 * <li>{@link #X_IBM_DATA_TYPE}</li>
	 * <li>{@link #X_IBM_JOB_MODIFY_VERSION}</li>
	 * <li>{@link #X_IBM_LSTAT}</li>
	 * <li>{@link #X_IBM_MAX_ITEMS}</li>
	 * <li>{@link #X_IBM_OPTION}</li>
	 * <li>{@link #X_IBM_REQUESTED_METHOD}</li>
	 *
	 */
	public enum ZosmfCustomHeaders {
		/**
		 * {@code X-IBM-Attributes}
		 */
		X_IBM_ATTRIBUTES("X-IBM-Attributes"),
		/**
		 * {@code X-IBM-Data-Type}
		 */
		X_IBM_DATA_TYPE("X-IBM-Data-Type"),
		/**
		 * {@code X-IBM-Job-Modify-Version}
		 */
		X_IBM_JOB_MODIFY_VERSION("X-IBM-Job-Modify-Version"),
		/**
		 * {@code X-IBM-Lstat}
		 */
		X_IBM_LSTAT("X-IBM-Lstat"),
		/**
		 * {@code X-IBM-Max-Items}
		 */
		X_IBM_MAX_ITEMS("X-IBM-Max-Items"),
		/**
		 * {@code X-IBM-Option}
		 */
		X_IBM_OPTION("X-IBM-Option"),
		/**
		 * {@code X-IBM-Requested-Method}
		 */
		X_IBM_REQUESTED_METHOD("X-IBM-Requested-Method");
		
		private String header;
		
		ZosmfCustomHeaders(String header) {
			this.header = header;
		}
		
		@Override
		public String toString() {
			return header;
		}
	}
	
	/**
	 * Set an HTTP Header for the pending zOSMF request
	 * 
	 * @param name header name
	 * @param value header value 
	 */
	public void setHeader(String name, String value);
	
	/**
	 * Clear the existing HTTP Headers ready for the next zOSMF request
	 *  
	 */
	void clearHeaders();

	/**
	 * Issue an HTTP PUT request to the zOSMF server with no request body
	 * 
	 * @param path identifies the zOSMF REST API
	 * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus.HttpStatus.SC_OK} when null
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public @NotNull IZosmfResponse get(String path, List<Integer> validStatusCodes) throws ZosmfException;

	/**
	 * Issue an HTTP POST request to the zOSMF server with a request body of content type of {@code application/json}
	 * 
	 * @param path identifies the zOSMF REST API
	 * @param requestBody the request body
	 * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus.HttpStatus.SC_OK} when null
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public @NotNull IZosmfResponse postJson(String path, JsonObject requestBody, List<Integer> validStatusCodes) throws ZosmfException;

	/**
	 * Issue an HTTP PUT request to the zOSMF server with a request body of content type of {@code text/plain}
	 * 
	 * @param path identifies the zOSMF REST API
	 * @param requestBody the request body
	 * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus.SC_OK} when null
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public @NotNull IZosmfResponse putText(String path, String requestBody, List<Integer> validStatusCodes) throws ZosmfException;

	/**
	 * Issue an HTTP PUT request to the zOSMF server with a request body of content type of {@code application/json}
	 * 
	 * @param path identifies the zOSMF REST API
	 * @param body the request body
	 * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus.HttpStatus.SC_OK} when null
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public @NotNull IZosmfResponse putJson(String path, JsonObject body, List<Integer> validStatusCodes) throws ZosmfException;

	/**
	 * Issue an HTTP DELETE request to the zOSMF server with no request body
	 * 
	 * @param path identifies the zOSMF REST API
	 * @param validStatusCodes a {@link List} of acceptable HTTP Status codes. Default to {@link HttpStatus.HttpStatus.SC_OK} when null
	 * @return the zOSMF server response
	 * @throws ZosmfException
	 */
	public @NotNull IZosmfResponse delete(String path, List<Integer> validStatusCodes) throws ZosmfException;
	
	/**
	 * Get the zOS image associated with the zOSMF server
	 * @return the zOS image
	 */
	public IZosImage getImage();
}
