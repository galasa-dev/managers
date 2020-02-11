/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.elasticlog.manager.ivt;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;

@Test
public class ElasticLogManagerIVT {

    @Logger
    public Log logger;

    @Test
    public void test() {
    }
}