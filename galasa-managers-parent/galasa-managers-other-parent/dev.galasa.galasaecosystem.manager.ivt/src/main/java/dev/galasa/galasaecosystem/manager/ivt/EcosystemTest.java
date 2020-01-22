package dev.galasa.galasaecosystem.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.galasaecosystem.IKubernetesEcosystem;
import dev.galasa.galasaecosystem.KubernetesEcosystem;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesNamespace;

@Test
public class EcosystemTest {
    
    @Logger
    public Log logger;
    
    @KubernetesEcosystem
    public IKubernetesEcosystem ecosystem;
    
    @KubernetesNamespace
    public IKubernetesNamespace namespace;
    
    
    @Test
    public void checkProvisioning() {
        assertThat(this.ecosystem).as("Check the ecosystem is provisioned").isNotNull();
    }
    

}
