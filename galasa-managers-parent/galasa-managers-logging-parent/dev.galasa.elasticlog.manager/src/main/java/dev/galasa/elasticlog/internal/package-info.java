/**
 * ElasticLog Manager
 * 
 * @galasa.manager ElasticLog
 * 
 * @galasa.release.state ALPHA - This Manager provides a way to export test data to ElasticSearch.  It contains the bare 
 * minimum code necessary to do that.  It has not been extensively tested. 
 * It is available for Galasa administrators to experiment with Elasic/Kabana dashboards
 * 
 * @galasa.description
 * 
 * This Manager is used to export test results to an elastic search endpoint, where the data can be visualised on a Kabana dashboard. 
 * Other Managers can contribute to the information being exported to Elastic.
 * <br><br>
 * As an absolute minimum, the CPS properties <code>elasticlog.endpoint.address</code> and <code>elasticlog.endpoint.index</code> 
 * must be provided. By default, this Manager only logs automated tests. To enable logging from locally run tests, 
 * <code>elasticlog.local.run.log</code> must be set to <code>true</code>.
 * <br><br>
 * This Manager provides two ElasticSearch indexes. One of all test data, and one of the latest run for each test case and each testing environment.
 * 
 * @galasa.limitations
 * 
 * The Manager logs the following test information:-<br>
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
 * If additional testing information is required, please raise an Issue.
 */
package dev.galasa.elasticlog.internal;
