package dev.galasa.kubernetes.internal.resources;

import dev.galasa.kubernetes.IService;
import dev.galasa.kubernetes.internal.KubernetesNamespaceImpl;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;

public class ServiceImpl implements IService {
    
    private final V1Service service;

    public ServiceImpl(KubernetesNamespaceImpl namespace, V1Service service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return service.getMetadata().getName();
    }

    @Override
    public TYPE getType() {
        return TYPE.Service;
    }

    @Override
    public String getYaml() {
        return Yaml.dump(this.service);
    }

}
