/**
 * ElasticLog Manager
 * 
 * @galasa.manager ElasticLog
 * 
 * @galasa.release.state ALPHA - This Manager has been written to provide a way of exporting test data to ElasticSearch.  It has the bare 
 * minimum code necessary to do that.  It has not been extensively tested. 
 * It is available for Galasa administrators to utilise to experiment with Elasic/Kabana dashboards
 * 
 * @galasa.description
 * 
 * This manager is used to export test results to an elastic search endpoint, where the data can be visualised on a Kabana dashboard. 
 * Other managers can contribute to the information being exported to Elastic.
 * <br><br>
 * As an absolute minimum, the CPS properties <code>elasticlog.endpoint.address</code> and <code>elasticlog.endpoint.index</code> 
 * must be provided. By default, this manager will only log automated tests, to enable logging from locally run tests, 
 * <code>elasticlog.local.run.log</code> must be set to true. The bundle must also be loaded by the framework but using 
 * <code>framework.extra.bundles=dev.galasa.elasticlog.manager</code> in bootstrap.properties.
 * <br><br>
 * This Manager will provide two ElasticSearch indexes. One of all test data, and one of the latest run for each test case and each 
 * testing environment.
 * 
 * @galasa.limitations
 * 
 * The Manager logs the following test information:-<br>
 * testCase<br>
 * runId<br>
 * startTimestamp<br>
 * endTimestamp<br>
 * requestor<br>
 * result<br>
 * testTooling<br>
 * testType<br>
 * testingEnvironment<br>
 * productRelease<br>
 * buildLevel<br>
 * customBuild<br>
 * testingAreas<br>
 * tags<br>
 * <br>
 * If additional testing information is required, please raise an Issue.
 */
package dev.galasa.elasticlog.internal;
