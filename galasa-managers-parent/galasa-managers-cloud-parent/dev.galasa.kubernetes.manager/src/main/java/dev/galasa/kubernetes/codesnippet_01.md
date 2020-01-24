### Create Kubernetes namespaces for the Kubernetes Manager to use

Note: Isolated namespaces must be provided for the Kubernetes Manager to use.  The Manager will delete any resource that 
exists on the namespace once a Test has finished.

The following are example scripts and YAML files necessary to create namspaces:-
1. [Namespace creation script](https://github.com/galasa-dev/managers/blob/master/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager/examples/namespaces.yaml)
1. [Create Service Account for the Manager to use (including api token)](https://github.com/galasa-dev/managers/blob/master/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager/examples/account.sh)
1. [The RBAC rules to be applied to each namespace](https://github.com/galasa-dev/managers/blob/master/galasa-managers-parent/galasa-managers-cloud-parent/dev.galasa.kubernetes.manager/examples/rbac.yaml)
The following snippet shows the minimum code that is required to request a Docker container in a Galasa test:

### Obtain a Kubernetes namespace

```java
@KubernetesNamespace()
public IKubernetesNamespace namespace;
```

This code will request the Kubernetes Manager allocate a namespace for the test to use.

There is no limit in Galasa on how many Kuberentes namespaces can be used within a single test. The only limit is the number of Kuberentes namespaces that can be started in the Galasa Ecosystem. This limit is set by the Galasa Administrator and is typically set to the maximum number of namespaces defined in the Kubernetes cluster.  If there are not enough "slots" available for an automated run, the run is put back on the queue in "waiting" state to retry.  Local test runs fail if there are not enough container "slots" available.

### Create a resource on the namespace

```java
@ArtifactManager
public IArtifactManager artifactManager

@KubernetesNamespace()
public IKubernetesNamespace namespace;

@Test
public void test() {
	IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
	
	String yaml = bundleResource.streamAsString(bundleResources.retrieveFile("/example.yaml"));
	
	IResource resource = namespace.createResource(yaml);
}

```

In this snippet, the test will retrieve the contents of the `/example.yaml` resource file as a String.  The YAML file is passed the the namespace for creation.  The YAML must contain only one Kubernetes resource.

The resource is created but is not checked to see if the resource has been started or allocated.

### Retrieve a pod log

```java
IStatefulSet statefulSet = (IStatefulSet)namespace.createResource(yaml);

List<IPodLog> podLogs = statefulSet.getPodLogs("containername");

```

As Deployments and StatefulSets can have multiple pods and therefore containers with the same name,  a List is returned containing all the current logs for all the named containers.