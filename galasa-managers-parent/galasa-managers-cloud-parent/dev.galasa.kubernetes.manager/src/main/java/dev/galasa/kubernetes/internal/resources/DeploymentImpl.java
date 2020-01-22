package dev.galasa.kubernetes.internal.resources;

import dev.galasa.kubernetes.IDeployment;
import dev.galasa.kubernetes.internal.KubernetesNamespaceImpl;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.util.Yaml;

public class DeploymentImpl implements IDeployment {
    
    private final V1Deployment deployment;

    public DeploymentImpl(KubernetesNamespaceImpl namespace, V1Deployment deployment) {
        this.deployment = deployment;
    }

    @Override
    public String getName() {
        return deployment.getMetadata().getName();
    }

    @Override
    public TYPE getType() {
        return TYPE.Deployment;
    }

    @Override
    public String getYaml() {
        return Yaml.dump(this.deployment);
    }

}
