/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts;

import java.util.Map;

/**
 * Represents the response from a CECI command
 */
public interface ICeciResponse {
    
    /**
     * Returns true if the CECI response is "NORMAL", 
     * @return
     */
    public boolean isNormal();
    
    /**
     * Throws an exception if the CECI response is not "NORMAL" 
     */
    public void checkNormal() throws CeciManagerException;
    
    /**
     * Throws an exception if the CECI response is an abend 
     */
    public void checkNotAbended() throws CeciManagerException;
    
    /**
     * Returns the text value of the CECI issues CICS API command, e.g. "NORMAL", 
     * @return the response
     */
    public String getResponse();
    
    /**
     * Returns the value of the Exec Interface Block field EIBRESP
     * @return EIBRESP
     */
    public int getEIBRESP();
    
    /**
     * Returns the value of the Exec Interface Block field EIBRESP2
     * @return EIBRESP2
     */
    public int getEIBRESP2();
    
    /**
     * Returns a map containing the output of the CECI CICS API command response fields and their values
     * @return 
     */
    public Map<String, ICeciResponseOutputValue> getResponseOutputValues();
}
