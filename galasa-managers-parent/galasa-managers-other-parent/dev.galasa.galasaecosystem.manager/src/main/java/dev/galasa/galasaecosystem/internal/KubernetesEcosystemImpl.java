package dev.galasa.galasaecosystem.internal;

import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IKubernetesEcosystem;
import dev.galasa.kubernetes.IKubernetesNamespace;

public class KubernetesEcosystemImpl implements IKubernetesEcosystem {
    
    private final IKubernetesNamespace namespace;
    private final String               tag;

    public KubernetesEcosystemImpl(String tag, IKubernetesNamespace namespace) {
        this.tag       = tag;
        this.namespace = namespace;
    }
    
    protected void loadYamlResources() throws GalasaEcosystemManagerException {
        
    }

    public String getTag() {
        return this.tag;
    }

    public void stop() {
        // TODO Auto-generated method stub
        
    }

    public void discard() {
        // TODO Auto-generated method stub
        
    }

}
