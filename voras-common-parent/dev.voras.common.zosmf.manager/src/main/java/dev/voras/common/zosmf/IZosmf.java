package dev.voras.common.zosmf;

public interface IZosmf {
	
	public IZosmfResponse putText(String uri, String text) throws ZosmfManagerException;

	public IZosmfResponse getText(String uri) throws ZosmfManagerException;

	public IZosmfResponse getJson(String uri) throws ZosmfManagerException;

	public IZosmfResponse getJsonArray(String uri) throws ZosmfManagerException;
}