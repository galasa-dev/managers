/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
/**
 * JMeter Manager
 * 
 * @galasa.manager JMeter
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * 
 * @galasa.description
 * 
 * This manager enables the tester to perform his own JMeter tests through the Galasa Framework. The manager will provide a container from the Docker Manager that is able to execute JMeter scripts. (JMX files)
 * The test does not need to worry how this container is provided and how it is kept up, the test has access to all the JMeter generated files inside the container.
 * <br><br>
 * The container for this execution is fully provided by the Docker Manager and is also properly shutdown once the tests come to an end.
 * By using a containerised environment for JMeter the test has access to a certain standard of scalability and uniformity.
 * <br><br> 
 * The logfiles and generated CSV files can be accessed after the JMeter tests are performed and the container becomes available for interaction.
 * The manager allows as many JMeter sessions as you have available Docker container slots on your machine.
 * 
 * @galasa.limitations
 * 
 * The JMeter manager is not able for the JMeter tests to be run remotely away from the Galasa framework on a targethost.
 */
package dev.galasa.jmeter;
