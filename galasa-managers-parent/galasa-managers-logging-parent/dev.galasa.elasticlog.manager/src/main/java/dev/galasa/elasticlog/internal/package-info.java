/**
 * ElasticLog Manager
 * 
 * @galasa.manager ElasticLog
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * It is available for Galasa administrators to utilise to experiment with Elastic/Kibana dashboards
 * 
 * @galasa.description
 * 
 * This Manager exports test results to an elastic search endpoint, where the data can be visualized on a Kibana dashboard. 
 * Other Managers can contribute to the information that is exported to Elastic.
 * <br><br>
 * As an absolute minimum, the CPS properties <code>elasticlog.endpoint.address</code> and <code>elasticlog.endpoint.index</code> 
 * must be provided. By default, this manager will only log automated tests, to enable logging from locally run tests, 
 * <code>elasticlog.local.run.log</code> must be set to true. The bundle must also be loaded by the framework by using 
 * <code>framework.extra.bundles=dev.galasa.elasticlog.manager</code> in bootstrap.properties.
 * <br><br>
 * This Manager provides two ElasticSearch indexes; one of all test data, and one of the latest run for each test case and each 
 * test environment.
 * 
 * @galasa.limitations
 * 
 * The Manager logs the following test information:<br>
 * <br>
 * - testCase<br>
 * - runId<br>
 * - startTimestamp<br>
 * - endTimestamp<br>
 * - requestor<br>
 * - result<br>
 * - testTooling<br>
 * - testType<br>
 * - testingEnvironment<br>
 * - productRelease<br>
 * - buildLevel<br>
 * - customBuild<br>
 * - testingAreas<br>
 * - tags<br>
 * <br>
 * <br>
 * If additional testing information is required, please raise a GitHub issue.
 */
package dev.galasa.elasticlog.internal;
