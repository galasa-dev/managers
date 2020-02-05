/**
 * ElasticLog Manager
 * 
 * @galasa.manager ElasticLog
 * 
 * @galasa.release.state ALPHA - This Manager has been written to provide a way of exporting test data to ElasticSearch.  It has the bare 
 * minimum code necessary to do that.  It has not been extensively tested and is subject to change. It is available for Galasa administrators to utilise to experiment with Elastic/Kabana dashboards
 * 
 * @galasa.description
 * 
 * This Manager exports test results to an elastic search endpoint, where the data can be visualized on a Kabana dashboard. 
 * Other Managers can contribute to the information that is exported to Elastic.
 * <br><br>
 * As an absolute minimum, the CPS properties <code>elasticlog.endpoint.address</code> and <code>elasticlog.endpoint.index</code> 
 * must be provided. By default, this Manager logs only automated tests. To enable logging from locally run tests, set the
 * <code>elasticlog.local.run.log</code> to true.
 * <br><br>
 * This Manager provides two ElasticSearch indexes; one of all test data, and one of the latest run for each test case and each 
 * test environment.
 * 
 * @galasa.limitations
 * 
 * The Manager logs the following test information:-<br>
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
