/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.galasaecosystem;

/**
 * Types of Endpoints for the Galasa Ecosystem
 * 
 * @author Michael Baylis
 *
 */
public enum EcosystemEndpoint {
    
    /**
     * Configuration Property Store eg, etcd:http://etcd.server:2323
     */
    CPS,
    /**
     * Dynamic Status Store eg, etcd:http://etcd.server:2323
     */
    DSS,
    /**
     * Result Archive Store eg, etcd:http://etcd.server:2323
     */
    RAS,
    /**
     * Credentials Store eg, couchdb:http://etcd.server:2323
     */
    CREDS,
    /**
     * API eg, http://etcd.server:2323
     */
    API,
    /**
     * Prometheus endpoint eg, http://etcd.server:2323
     */
    PROMETHEUS,
    /**
     * Grafana endpoint eg, http://etcd.server:2323
     */
    GRAFANA,
    /**
     * The metrics server metrics port eg, http://etcd.server:2323
     */
    METRICS_METRICS,
    /**
     * The metrics server health port eg, http://etcd.server:2323
     */
    METRICS_HEALTH,
    /**
     * The resource management health port eg, http://etcd.server:2323
     */
    RESOURCE_MANAGEMENT_METRICS,
    /**
     * The resource management health port eg, http://etcd.server:2323
     */
    RESOURCE_MANAGEMENT_HEALTH,
    /**
     * The engine controller metrics port eg, http://etcd.server:2323
     */
    ENGINE_CONTROLLER_METRICS,
    /**
     * The engine controller health port eg, http://etcd.server:2323
     */
   ENGINE_CONTROLLER_HEALTH

}
