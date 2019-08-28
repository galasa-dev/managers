package dev.galasa.common.zosmf.internal;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.common.http.HttpClientResponse;
import dev.galasa.common.zosmf.IZosmfResponse;
import dev.galasa.common.zosmf.ZosmfException;

public class ZosmfResponseImpl implements IZosmfResponse {

	private HttpClientResponse<?> httpClientresponse;
	private URL requestUrl;

	public ZosmfResponseImpl(String url, String path) throws MalformedURLException {
		this.requestUrl = new URL(url + path); 
	}

	@Override
	public JsonObject getJsonContent() throws ZosmfException {
		Object content = this.httpClientresponse.getContent();
		if (!(content instanceof String) && !(content instanceof JsonObject)) {
			throw new ZosmfException("Content not a JsonObject or String Object - " + content.getClass().getName());
		}
		return content instanceof String ? new JsonParser().parse((String) content).getAsJsonObject() : (JsonObject) content;
	}

	@Override
	public JsonArray getJsonArrayContent() throws ZosmfException {
		Object content = this.httpClientresponse.getContent();
		if (!(content instanceof String) && !(content instanceof JsonArray)) {
			throw new ZosmfException("Content not a JsonArray or String Object - " + content.getClass().getName());
		}
		
		return content instanceof String ? new JsonParser().parse((String) content).getAsJsonArray() : (JsonArray) content;
	}

	@Override
	public String getTextContent() throws ZosmfException {
		Object content = this.httpClientresponse.getContent();
		if (!(content instanceof String)) {
			throw new ZosmfException("Content not a String Object - " + content.getClass().getName());
		}
		return (String) content;
	}

	@Override
	public int getStatusCode() {
		return this.httpClientresponse.getStatusCode();
	}

	@Override
	public String getStatusLine() {
		return this.httpClientresponse.getStatusLine();
	}

	@Override
	public URL getRequestUrl() {
		return this.requestUrl;
	}

	public void setHttpClientresponse(HttpClientResponse<?> httpClientResponse) {
		this.httpClientresponse = httpClientResponse;
	}

}
