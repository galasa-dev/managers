package dev.voras.common.zosmf;

import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface IZosmfResponse {

	String getTextContent() throws ZosmfException;

	JsonObject getJsonContent() throws ZosmfException;

	JsonArray getJsonArrayContent() throws ZosmfException;

	int getStatusCode();

	String getStatusLine();

	URL getRequestUrl();
}
