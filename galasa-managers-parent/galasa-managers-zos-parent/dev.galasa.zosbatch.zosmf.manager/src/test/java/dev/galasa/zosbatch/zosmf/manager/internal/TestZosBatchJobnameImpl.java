/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchJobnameImpl;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.JobnamePrefix;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JobnamePrefix.class})
public class TestZosBatchJobnameImpl {
    
    private static final String FIXED_JOBNAME = "GAL45678";
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setup() throws ZosBatchManagerException {
        // Mock JobnamePrefix.get() to return a fixed value
        PowerMockito.mockStatic(JobnamePrefix.class);
        Mockito.when(JobnamePrefix.get(Mockito.anyString())).thenReturn(FIXED_JOBNAME);
    }
    
    @Test
    public void testGetJobname() throws ZosBatchManagerException {        
        ZosBatchJobnameImpl zosJobname = new ZosBatchJobnameImpl("image");
        Assert.assertEquals("getName() should return the supplied value", FIXED_JOBNAME, zosJobname.getName());
        Assert.assertEquals("toString() should return the the job name", FIXED_JOBNAME, zosJobname.toString());
    }
    
    @Test
    public void testException() throws ZosBatchManagerException {
        Mockito.when(JobnamePrefix.get(Mockito.anyString())).thenThrow(new ZosBatchManagerException("exception"));
        
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Problem getting batch jobname prefix");
        new ZosBatchJobnameImpl("image");
    }
}
