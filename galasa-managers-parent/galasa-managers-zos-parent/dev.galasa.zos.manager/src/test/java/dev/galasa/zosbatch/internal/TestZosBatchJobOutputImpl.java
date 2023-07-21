/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;

@RunWith(MockitoJUnitRunner.class)
public class TestZosBatchJobOutputImpl {
    
    @Mock
    private IZosBatchJob zosBatchJobMock;
    
    private ZosBatchJobOutputImpl zosBatchJobOutput; 

    private static final String JOBNAME = "jobname";

    private static final String JOBID = "jobid";
    
    private static final String STEPNAME = "stepname";
    
    private static final String PROCSTEP = "procstep";
    
    private static final String DDNAME = "ddname";
    
    private static final String ID = "id";

    private static final String RECORDS = "records";
    
    @Before
    public void setup() throws ZosBatchManagerException {
        zosBatchJobOutput = new ZosBatchJobOutputImpl(zosBatchJobMock, JOBNAME, JOBID);
    }
    
    @Test
    public void testAddJcl() throws ZosBatchException {
        zosBatchJobOutput.addJcl(RECORDS);
        Assert.assertEquals("getJobname() should return the supplied value", JOBNAME, zosBatchJobOutput.getJobname());
        Assert.assertEquals("getJobid() should return the supplied value", JOBID, zosBatchJobOutput.getJobid());
        Assert.assertEquals("toString() should return the supplied values of JOBNAME_JOBID", JOBNAME + "_" + JOBID, zosBatchJobOutput.toString());
    }

    @Test
    public void testAdd() throws ZosBatchException {
        zosBatchJobOutput.addSpoolFile(STEPNAME, PROCSTEP, DDNAME, ID, RECORDS);
        Assert.assertEquals("getJobname() should return the supplied value", JOBNAME, zosBatchJobOutput.getJobname());
        Assert.assertEquals("getJobid() should return the supplied value", JOBID, zosBatchJobOutput.getJobid());
        Assert.assertEquals("toString() should return the supplied values of JOBNAME_JOBID", JOBNAME + "_" + JOBID, zosBatchJobOutput.toString());
    }
    @Test
    public void testGetSpoolFiles() throws ZosBatchException {
        zosBatchJobOutput.addJcl("JCL");
        Assert.assertNotNull("getSpoolFiles() should not return null", zosBatchJobOutput.getSpoolFiles());
    }
    
    @Test
    public void testToList() throws ZosBatchException {
        zosBatchJobOutput.addJcl("JCL");
        Assert.assertNotNull("toList() should return a value", zosBatchJobOutput.toList());
    }
    
    @Test
    public void testIterator() throws ZosBatchException {
        zosBatchJobOutput.addJcl("JCL");
        Iterator<IZosBatchJobOutputSpoolFile> iterator = zosBatchJobOutput.iterator();
        
        Assert.assertTrue("hasNext() should return true", iterator.hasNext());
        
        Assert.assertNotNull("next() should return a value", iterator.next());
        
        String expectedMessage = "Object can not be updated";
        UnsupportedOperationException expectedException = Assert.assertThrows("expected exception should be thrown", UnsupportedOperationException.class, ()->{
        	iterator.remove();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testSize() throws ZosBatchException {
        zosBatchJobOutput.addJcl("JCL");
        Assert.assertEquals("size() should return a 1", 1, zosBatchJobOutput.size());
    }
    
    @Test
    public void testIsEmpty() throws ZosBatchException {
        Assert.assertTrue("isEmpty() should return a true", zosBatchJobOutput.isEmpty());
    }
}
