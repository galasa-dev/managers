package dev.galasa.galasaecosystem.internal;

import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.java.IJavaInstallation;

public abstract class AbstractEcosystemImpl implements IInternalEcosystem {
    
    private final GalasaEcosystemManagerImpl manager;
    private final String                     tag;
    private ICommandShell                    commandShell;
    private IJavaInstallation                javaInstallation;
    
    public AbstractEcosystemImpl(GalasaEcosystemManagerImpl manager, 
            String tag,
            IJavaInstallation javaInstallation, 
            ICommandShell commandShell) {
        this.manager          = manager;
        this.tag              = tag;
        this.commandShell     = commandShell;
        this.javaInstallation = javaInstallation;
        
    }
 
    @Override
    public String getTag() {
        return this.tag;
    }

    protected GalasaEcosystemManagerImpl getEcosystemManager() {
        return this.manager;
    }
    
    protected ICommandShell getCommandShell() {
        return this.commandShell;
    }
    
    protected IJavaInstallation getJavaInstallation() {
        return this.javaInstallation;
    }

}
