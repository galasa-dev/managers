package dev.galasa.galasaecosystem.internal;

import java.util.Properties;

import javax.validation.constraints.NotNull;

import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.windows.IWindowsImage;
import dev.galasa.windows.WindowsManagerException;

public class LocalWindowsEcosystemImpl extends LocalEcosystemImpl {

    public LocalWindowsEcosystemImpl(GalasaEcosystemManagerImpl manager, 
            String tag,
            IWindowsImage windowsImage, 
            IJavaInstallation javaInstallation) throws WindowsManagerException {
        super(manager, tag, javaInstallation, windowsImage.getCommandShell());
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

    @Override
    public String submitRun(String runType, String requestor, String groupName, @NotNull String bundleName,
            @NotNull String testName, String mavenRepository, String obr, String stream, Properties overrides)
                    throws GalasaEcosystemManagerException {
        return null;

    }



}
