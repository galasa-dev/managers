/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosmf;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosmf.IZosmf.ZosmfCustomHeaders;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;

public interface IZosmfRestApiProcessor {    
    
    /**
     * Send zOSMF request
     * @param requestType as defined by {@link ZosmfRequestType}
     * @param path the zOSMF API path
     * @param headers the required HTTP headers (e.g. See {@link ZosmfCustomHeaders} or null
     * @param body the request body or null
     * @param validStatusCodes list HTTP status codes expected from this request. default of HTTP 200 when null 
     * @return the response {@link IZosmfResponse}
     * @throws ZosBatchException
     */
    public @NotNull IZosmfResponse sendRequest(ZosmfRequestType requestType, String path, Map<String, String> headers, Object body, List<Integer> validStatusCodes) throws ZosmfException;
}
