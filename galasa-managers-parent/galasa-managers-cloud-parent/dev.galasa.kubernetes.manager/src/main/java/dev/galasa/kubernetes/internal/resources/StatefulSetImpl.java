package dev.galasa.kubernetes.internal.resources;

import dev.galasa.kubernetes.IStatefulSet;
import dev.galasa.kubernetes.internal.KubernetesNamespaceImpl;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.util.Yaml;

public class StatefulSetImpl implements IStatefulSet {
    
    private final V1StatefulSet statefulSet;

    public StatefulSetImpl(KubernetesNamespaceImpl namespace, V1StatefulSet deployment) {
        this.statefulSet = deployment;
    }

    @Override
    public String getName() {
        return statefulSet.getMetadata().getName();
    }

    @Override
    public TYPE getType() {
        return TYPE.StatefulSet;
    }

    @Override
    public String getYaml() {
        return Yaml.dump(this.statefulSet);
    }

}
