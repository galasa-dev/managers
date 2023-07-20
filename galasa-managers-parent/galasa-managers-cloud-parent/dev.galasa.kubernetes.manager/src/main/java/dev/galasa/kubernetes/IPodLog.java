/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
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
