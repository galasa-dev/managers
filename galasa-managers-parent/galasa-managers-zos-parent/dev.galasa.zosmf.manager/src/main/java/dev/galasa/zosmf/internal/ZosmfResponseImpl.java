/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosmf.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.methods.CloseableHttpResponse;

import dev.galasa.http.HttpClientResponse;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.ZosmfException;

public class ZosmfResponseImpl implements IZosmfResponse {

    private URL requestUrl;
    private Object content;
    private int statusCode;
    private String statusLine;

    public ZosmfResponseImpl(String url, String path) throws MalformedURLException {
        this.requestUrl = new URL(url + path); 
    }

    @Override
    public JsonObject getJsonContent() throws ZosmfException {
        if (!(this.content instanceof String) && !(content instanceof JsonObject)) {
            throw new ZosmfException("Content not a JsonObject or String Object - " + content.getClass().getName());
        }
        return content instanceof String ? new JsonParser().parse((String) content).getAsJsonObject() : (JsonObject) content;
    }

    @Override
    public JsonArray getJsonArrayContent() throws ZosmfException {;
        if (!(this.content instanceof String) && !(content instanceof JsonArray)) {
            throw new ZosmfException("Content not a JsonArray or String Object - " + content.getClass().getName());
        }
        
        return content instanceof String ? new JsonParser().parse((String) content).getAsJsonArray() : (JsonArray) content;
    }

    @Override
    public String getTextContent() throws ZosmfException {
        if (!(this.content instanceof String)) {
            throw new ZosmfException("Content not a String Object - " + content.getClass().getName());
        }
        return (String) content;
    }

    @Override
    public Object getContent() throws ZosmfException {
        return this.content;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getStatusLine() {
        return this.statusLine;
    }

    @Override
    public URL getRequestUrl() {
        return this.requestUrl;
    }

    public void setHttpClientresponse(HttpClientResponse<?> httpClientResponse) {
        this.content = httpClientResponse.getContent();
        this.statusCode = httpClientResponse.getStatusCode();
        this.statusLine = httpClientResponse.getStatusLine();
    }

    public void setHttpClientresponse(CloseableHttpResponse httpClientResponse) throws ZosmfException{
        try{
            this.content = httpClientResponse.getEntity().getContent();
            this.statusCode = httpClientResponse.getStatusLine().getStatusCode();
            this.statusLine = httpClientResponse.getStatusLine().getReasonPhrase();
        } catch (IOException e) {
            throw new ZosmfException("Could not retrieve response", e);
        }
    }

}
