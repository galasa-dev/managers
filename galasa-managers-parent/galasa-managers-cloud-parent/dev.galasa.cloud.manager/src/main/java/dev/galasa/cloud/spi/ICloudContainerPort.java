/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.cloud.spi;

public interface ICloudContainerPort {

    /**
     * A name given to the port,  not used except for getContainerExposedPort()
     */
    public String getName();
    
    /**
     * The port number
     */
    public int    getPort();
    
    /**
     * The type of port, http, tcp, udp
     */
    public String getType();
}
