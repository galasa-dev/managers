package dev.galasa.galasaecosystem.internal;

import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.IJavaInstallation;

public abstract class AbstractEcosystemImpl implements IInternalEcosystem {
    
    private final GalasaEcosystemManagerImpl manager;
    private final String                     tag;
    private IJavaInstallation                javaInstallation;
    
    public AbstractEcosystemImpl(GalasaEcosystemManagerImpl manager, 
            String tag,
            IJavaInstallation javaInstallation) {
        this.manager          = manager;
        this.tag              = tag;
        this.javaInstallation = javaInstallation;
        
    }
 
    @Override
    public String getTag() {
        return this.tag;
    }

    protected GalasaEcosystemManagerImpl getEcosystemManager() {
        return this.manager;
    }
    
    protected abstract ICommandShell getCommandShell() throws GalasaEcosystemManagerException;
    
    protected IJavaInstallation getJavaInstallation() {
        return this.javaInstallation;
    }

}
