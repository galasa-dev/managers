package dev.galasa.kubernetes.manager.ivt;

import dev.galasa.Test;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesNamespace;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class KubernetesManagerIVT {
    
    @KubernetesNamespace
    public IKubernetesNamespace namespace;
    
    @Test
    public void ensureSetupOk() {
        assertThat(this.namespace).as("Kubernetes Namespace").isNotNull();
    }

}
