/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosmf.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;

public class ZosmfRestApiProcessor implements IZosmfRestApiProcessor {
	
	private int retryRequest;
	private IZosmf currentZosmf;
	private String currentZosmfImageId;
	
	private final HashMap<String, IZosmf> zosmfs = new LinkedHashMap<>();
	
	private static final Log logger = LogFactory.getLog(ZosmfRestApiProcessor.class);
	
	public ZosmfRestApiProcessor(Map<String, IZosmf> zosmfs) {
		this.zosmfs.putAll(zosmfs);
		this.currentZosmfImageId = this.zosmfs.entrySet().iterator().next().getKey();
		this.currentZosmf = this.zosmfs.get(this.currentZosmfImageId);
	}
	
	
	/**
	 * Send zOSMF request
	 * @param requestType
	 * @param path
	 * @param body
	 * @param headers
	 * @param validStatusCodes
	 * @return
	 * @throws ZosBatchException
	 */
	public @NotNull IZosmfResponse sendRequest(ZosmfRequestType requestType, String path, Map<String, String> headers, Object body, List<Integer> validStatusCodes) throws ZosmfException {
		if (validStatusCodes == null) {
			validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
		}
		IZosmfResponse response = null;
		for (int i = 0; i <= this.retryRequest; i++) {
			try {
				IZosmf zosmfServer = getCurrentZosmfServer();
				if (headers != null) {
					for (Entry<String, String> entry : headers.entrySet()) {
						zosmfServer.setHeader(entry.getKey(), entry.getValue());
					}
				}
				switch (requestType) {
				case GET:
					response = zosmfServer.get(path, validStatusCodes);
					break;
				case POST_JSON:
					response = zosmfServer.postJson(path, (JsonObject) body, validStatusCodes);
					break;
				case PUT_TEXT:
					response = zosmfServer.putText(path, (String) body, validStatusCodes);
					break;
				case PUT_JSON:
					response = zosmfServer.putJson(path, (JsonObject) body, validStatusCodes);
					break;
				case DELETE:
					response = zosmfServer.delete(path, validStatusCodes);
					break;
				default:
					throw new ZosmfException("Invalid request type");
				}
	
				if (validStatusCodes.contains(response.getStatusCode())) {
			    	return response;
				} else {
					logger.error("Expected HTTP status codes: " + validStatusCodes);
			    	getNextZosmf();
				}
			} catch (ZosmfManagerException e) {
		    	logger.error(e);
		    	getNextZosmf();
			}
		}
		throw new ZosmfException("Unable to get valid response fron zOS/MF server");
	}
	
	private IZosmf getCurrentZosmfServer() {
		logger.info("Using zOSMF on " + this.currentZosmf);
		this.currentZosmf.clearHeaders();
		return this.currentZosmf;
	}

	private void getNextZosmf() {
		if (this.zosmfs.size() == 1) {
			logger.debug("Only one zOSMF server available");
			return;
		}
		Iterator<Entry<String, IZosmf>> zosmfsIterator = this.zosmfs.entrySet().iterator();
		while (zosmfsIterator.hasNext()) {
			if (zosmfsIterator.next().getKey().equals(this.currentZosmfImageId)) {
				Entry<String, IZosmf> entry;
				if (zosmfsIterator.hasNext()) {
					entry = zosmfsIterator.next();
				} else {
					entry = this.zosmfs.entrySet().iterator().next();
				}
				this.currentZosmfImageId = entry.getKey();
				this.currentZosmf = this.zosmfs.get(this.currentZosmfImageId);
				return;
			}
		}
		logger.debug("No alternate zOSMF server available");
	}
}
