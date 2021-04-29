package dev.galasa.galasaecosystem.internal;

import java.util.Properties;

import javax.validation.constraints.NotNull;

import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.ILocalEcosystem;

public class LocalEcosystemImpl implements ILocalEcosystem, IInternalEcosystem {

    @Override
    public String getCpsProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCpsProperty(@NotNull String property, String value) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getDssProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDssProperty(@NotNull String property, String value) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getCredsProperty(@NotNull String property) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCredsProperty(@NotNull String property, String value) throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void submitRun(String runType, String requestor, String groupName, @NotNull String bundleName,
            @NotNull String testName, String mavenRepository, String obr, String stream, Properties overrides)
            throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object getTag() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void build() throws GalasaEcosystemManagerException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void discard() {
        // TODO Auto-generated method stub
        
    }

}
