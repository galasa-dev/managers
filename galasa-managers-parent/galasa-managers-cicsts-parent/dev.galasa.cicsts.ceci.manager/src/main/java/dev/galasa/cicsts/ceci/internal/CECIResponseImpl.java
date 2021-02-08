/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci.internal;

import java.util.Map;

import dev.galasa.cicsts.CeciManagerException;
import dev.galasa.cicsts.ICeciResponse;
import dev.galasa.cicsts.ICeciResponseOutputValue;

public class CECIResponseImpl implements ICeciResponse {
    
    private String response;
    private int eibresp;
    private int eibresp2;
    private Map<String, ICeciResponseOutputValue> responseOutput;

    public CECIResponseImpl(String response, int eibresp, int eibresp2) {
        this.response = response;
        this.eibresp = eibresp;
        this.eibresp2 = eibresp2;
    }

    protected void setResponseOutput(Map<String, ICeciResponseOutputValue> responseOutput) {
        this.responseOutput = responseOutput;
    }

    @Override
    public boolean isNormal() {
        return response.equals("NORMAL");
    }

    @Override
    public void checkNormal() throws CeciManagerException {
        if (!isNormal()) {
            throw new CeciManagerException("CECI response is not 'NORMAL', actual response is '" + response + "'");
        }
        
        return;
    }

    @Override
    public void checkNotAbended() throws CeciManagerException {
        if (response.startsWith("ABEND ")) {
            throw new CeciManagerException("CECI response is an abend '" + response + "'");
        }

        return;
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
    public Map<String, ICeciResponseOutputValue> getResponseOutputValues() {
        return responseOutput;
    }
    
    @Override
    public String toString() {
        return String.format("RESPONSE: %s EIBRESP=%+010d EIBRESP2=%+010d", response, eibresp, eibresp2);
    }

}
