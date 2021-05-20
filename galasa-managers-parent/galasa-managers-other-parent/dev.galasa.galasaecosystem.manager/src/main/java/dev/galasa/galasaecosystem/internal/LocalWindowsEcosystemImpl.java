package dev.galasa.galasaecosystem.internal;

import java.util.Properties;

import javax.validation.constraints.NotNull;

import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IsolationInstallation;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.windows.IWindowsImage;
import dev.galasa.windows.WindowsManagerException;

public class LocalWindowsEcosystemImpl extends LocalEcosystemImpl {
    
    private final IWindowsImage windowsImage;

    public LocalWindowsEcosystemImpl(GalasaEcosystemManagerImpl manager, 
            String tag,
            IWindowsImage windowsImage, 
            IJavaInstallation javaInstallation, 
            IsolationInstallation isolationInstallation,
            boolean startSimPlatform) throws WindowsManagerException {
        super(manager, tag, javaInstallation, isolationInstallation, startSimPlatform);
        this.windowsImage = windowsImage;
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

    @Override
    protected ICommandShell getCommandShell() throws GalasaEcosystemManagerException {
        try {
            return this.windowsImage.getCommandShell();
        } catch (WindowsManagerException e) {
            throw new GalasaEcosystemManagerException("Problem obtaining command shell", e);
        }
    }

    @Override
    public void startSimPlatform() throws GalasaEcosystemManagerException {
        throw new GalasaEcosystemManagerException("This code needs writing");
    }

    @Override
    public void stopSimPlatform() throws GalasaEcosystemManagerException {
        throw new GalasaEcosystemManagerException("This code needs writing");
    }


}
