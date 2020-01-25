/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.kubernetes;


/**
 * Holder for a Pod Log
 * 
 * @author Michael Baylis
 *
 */
public interface IPodLog {

    /**
     * @return the log from the pod
     */
    String getLog();

}
