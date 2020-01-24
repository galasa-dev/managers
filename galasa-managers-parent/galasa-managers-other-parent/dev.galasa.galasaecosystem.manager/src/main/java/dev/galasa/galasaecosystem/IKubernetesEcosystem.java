package dev.galasa.galasaecosystem;

import java.net.URI;

import javax.validation.constraints.NotNull;

/**
 * Kubernetes Ecosystem
 *  
 * @author Michael Baylis
 *
 */
public interface IKubernetesEcosystem {
    
    public @NotNull URI getEndpoint(@NotNull EcosystemEndpoint endpoint) throws GalasaEcosystemManagerException;
    
    public String getCpsProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    public void setCpsProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
    public String getDssProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    public void setDssProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
    public String getCredsProperty(@NotNull String property) throws GalasaEcosystemManagerException;
    
    public void setCredsProperty(@NotNull String property, String value)  throws GalasaEcosystemManagerException;
    
}