### Obtain a Kubernetes namespace

```java
@KubernetesEcosystem
public IKubernetesEcosystem ecosystem;
    
@KubernetesNamespace
public IKubernetesNamespace namespace;
```

This code will request the a Galasa Ecosystem be provisision in a Kubernetes Namespace.  The default Tag for both of them is 
PRIMARY.

### Retrieve the RAS Endpoint

```java
@KubernetesEcosystem
public IKubernetesEcosystem ecosystem;

URI ras = ecosystem.getEndpoint(EcosystemEndpoint.RAS);

```

This snippet demonstrates how to retrieve the Result Archive Store endpoint.   Be aware, that the URI is 
prefixed with the store type, eg couchdb:http://couchdb.server:5984.  This is the same for the CPS, DSS and CREDS.

### Set and retrieve a CPS property

```java
ecosystem.setCpsProperty("bob", "hello");

String value = ecosystem.getCpsProperty("bob")
```

Will set the CPS property `bob` with the value `hello`and retrieve it again.