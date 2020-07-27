/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
/**
 * zOS Program Manager
 * 
 * @galasa.manager zOS Program
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * 
 * @galasa.description
 * 
 * This Manager allows Galasa tests to compile and link zOS Programs.<br><br>
 * 
 * Use the z/OS Program Manager in your Galasa test to log onto an application in a test environment and update it.<br><br>
 * The <code>@ZosProgram</code> annotation defines the program to the Galasa test. For example, to check that update worked, write a COBOL test program that can be submitted, for example, by a batch job. The test program is used only by this Galasa test, so its source is included in this test project. The source for the program is stored in the src/main/resources folder of the test project in Eclipse. When Maven builds the project, it creates a test bundle which includes everything contained in the src/main/resources folder. When the  test runs, the Galasa launcher is passed the name of the test bundle and the test class. For each @ZosProgram annotation in the test, the z/OS program Manager finds the program source in the test bundle, creates compile and link JCL based on the @ZosProgram parameters and then submits the JCL to create the Load Module. You can then write a test method within the test to build the batch job to run the program and check the application update worked.
 * Start the z/OS program by using the <code>EXEC PGM</code> command, for example, <code>EXEC PGM=test_program</code> which can be included in the JCL contained within a batch job.
 * When the <code>EXEC PGM</code> command runs, z/OS loads the load module “test_program” from the library to memory and runs it.  <br><br>
 * In the Simbank tutorial <a href="/docs/running-simbank-tests/batch-accounts-open-test">BatchAccountsOpenTest</a>BatchAccountsOpenTest uses JCL contained within the batch job to runs the z/OS program SIMBANK via the <code>EXEC PGM=SIMBANK</code> command. <br><br>
 * <br><br>
 * You can view the <a href="https://javadoc.galasa.dev/" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>. <br><br>
 * 
 */
package dev.galasa.zosprogram;