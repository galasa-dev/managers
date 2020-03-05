/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci.internal;

import java.util.Map;

import dev.galasa.cicsts.ceci.ICECIResponse;
import dev.galasa.cicsts.ceci.IResponseOutputValue;

public class CECIResponseImpl implements ICECIResponse {
    
    private String response;
    private int eibresp;
    private int eibresp2;
    private Map<String, IResponseOutputValue> responseOutput;

    public CECIResponseImpl(String response, int eibresp, int eibresp2) {
        this.response = response;
        this.eibresp = eibresp;
        this.eibresp2 = eibresp2;
    }

    protected void setResponseOutput(Map<String, IResponseOutputValue> responseOutput) {
        this.responseOutput = responseOutput;
    }

    @Override
    public boolean isNormal() {
        return response.equals("NORMAL");
    }

    @Override
    public String getResponse() {
        return response;
    }

    @Override
    public int getEIBRESP() {
        return eibresp;
    }

    @Override
    public int getEIBRESP2() {
        return eibresp2;
    }

    @Override
    public Map<String, IResponseOutputValue> getResponseOutputValues() {
        return responseOutput;
    }
    
    @Override
    public String toString() {
        return String.format("RESPONSE: %s EIBRESP=%+010d EIBRESP2=%+010d", response, eibresp, eibresp2);
    }
}
