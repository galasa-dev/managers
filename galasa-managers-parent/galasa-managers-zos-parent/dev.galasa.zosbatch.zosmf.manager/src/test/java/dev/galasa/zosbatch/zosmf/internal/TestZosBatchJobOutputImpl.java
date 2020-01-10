/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchJobOutputImpl {
	
	private ZosBatchJobOutputImpl zosBatchJobOutput; 

	private static final String JOBID = "jobid";

	private static final String JOBNAME = "jobname";
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	
	@Before
	public void setup() throws ZosBatchManagerException {
		zosBatchJobOutput = new ZosBatchJobOutputImpl(JOBNAME, JOBID);
	}
	
	@Test
	public void testAddJcl() throws ZosBatchException {
		zosBatchJobOutput.addJcl("JCL");
		assertEquals(JOBNAME, zosBatchJobOutput.getJobname());
		assertEquals(JOBID, zosBatchJobOutput.getJobid());
		assertEquals(JOBNAME + "_" + JOBID, zosBatchJobOutput.toString());
	}
	
	@Test
	public void testGetSpoolFiles() throws ZosBatchException {
		zosBatchJobOutput.addJcl("JCL");
		assertNotNull(zosBatchJobOutput.getSpoolFiles());
	}
	
	@Test
	public void testToList() throws ZosBatchException {
		zosBatchJobOutput.addJcl("JCL");
		assertNotNull(zosBatchJobOutput.toList());
	}
	
	@Test
	public void testIterator() throws ZosBatchException {
		zosBatchJobOutput.addJcl("JCL");
		Iterator<IZosBatchJobOutputSpoolFile> iterator = zosBatchJobOutput.iterator();
		
		assertTrue(iterator.hasNext());
		
		assertNotNull(iterator.next());
		
		exceptionRule.expect(UnsupportedOperationException.class);
	    exceptionRule.expectMessage("Object can not be updated");
	    iterator.remove();
	}
}
