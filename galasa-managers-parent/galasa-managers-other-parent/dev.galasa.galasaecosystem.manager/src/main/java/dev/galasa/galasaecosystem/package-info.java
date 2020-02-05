/**
 * Galasa Ecosystem Manager
 * 
 * @galasa.manager Galasa Ecosystem
 * 
 * @galasa.release.state ALPHA - This Manager provides a Galasa Ecsosystem for the Galasa integrated test pipeline.  It has the bare 
 * minimum code necessary to do that.  It has not been extensively tested (other than on the pipeline) and is subject to change.  However, saying all that, 
 * it can be used within tests.
 * 
 * @galasa.description
 * 
 * This Manager provides the test with a fully provisioned Galasa Ecosystem to test on.  When the test runs
 * all the services that are required by the test are known to be up and working.
 * <br><br>
 * The Galasa Ecosystem Manager supports Galasa Shared Environments.  Shared Environments provide 
 * the ability to create a test environment that can be shared across multiple test runs 
 * so you don't have to provision a test environment for each test.  
 * 
 * @galasa.limitations
 * 
 * The Manager supports only the following platforms:-<br>
   <br>
 * - Kubernetes Namespace<br>
 * <br>
 * In the near future, this Manager will be able to provision Ecosystems in Docker and a plain Linux server.
 */
package dev.galasa.galasaecosystem;
