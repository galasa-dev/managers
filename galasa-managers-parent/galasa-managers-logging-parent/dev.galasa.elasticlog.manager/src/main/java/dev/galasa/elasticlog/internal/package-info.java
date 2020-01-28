/**
 * ElasticLog Manager
 * 
 * @galasa.manager ElasticLog
 * 
 * @galasa.release.state ALPHA - This Manager has been written to provide a way of exporting test data to ElasticSearch.  It has the bare 
 * minimum code necessary to do that.  It has not been extensively tested.  The TPI of the LoggingManager interface is subject to change. 
 * It can be used within tests.
 * 
 * @galasa.description
 * 
 * This Manager provides an interface to allow test data to be exported to an ElasticSearch endpoint. This allows for test data to be 
 * visualised and logged. 
 * <br><br>
 * As an absolute minimum, the CPS properties <code>elasticlog.endpoint.address</code> and <code>elasticlog.endpoint.index</code> 
 * must be provided with <code>elasticlog.local.run.log</code> set to true to active the manager locally.
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
