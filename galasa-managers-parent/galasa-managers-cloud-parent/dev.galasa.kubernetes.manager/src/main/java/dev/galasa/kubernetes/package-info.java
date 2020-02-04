/**
 * Kubernetes Manager
 * 
 * @galasa.manager Kubernetes
 * 
 * @galasa.release.state ALPHA - This Manager has been written to provide a Kubernetes Namespace for the Galasa integrated test pipeline.  It has the bare 
 * minimum code necessary to do that.  It has not been extensively tested (other than on the pipeline).  The TPI is subject to change.  However, saying all that, 
 * it can be used within tests.
 * 
 * @galasa.description
 * 
 * This Manager provides a test with a Kubernetes Namespace to utilize.  The test will provide YAML representations
 * of the resources that the test requires.
 * <br><br>
 * As an absolute minimum, the CPS property <code>kubernetes.cluster.K8S.url</code> must be provided and a credential
 * <code>secure.credentials.K8S.token</code> for the API token.
 * <br><br>
 * The Kubernetes Manager supports Galasa Shared Environments.  Shared Environments provide 
 * the ability to create a test environment that can be shared across multiple test runs 
 * so you don't have to provision a test environment for each test.  
 * 
 * @galasa.limitations
 * 
 * The Manager only supports the following Kubernetes resources:-<br>
 * - Deployment
 * - StatefulSet
 * - Service
 * - Secret
 * - ConfigMap
 * - PersistentVolumeClaim
 * 
 * If additional resources are required, please raise an Issue.
 */
package dev.galasa.kubernetes;
