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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.ZosBatchManagerException;

@RunWith(PowerMockRunner.class)
public class TestZosBatchJobnameImpl {
    
    private static final String FIXED_JOBNAME = "GAL45678";

    @Mock
    private ZosManagerImpl zosManagerMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    @Before
    public void setup() throws ZosBatchManagerException {
    	Mockito.when(zosManagerMock.getZosBatchPropertyJobnamePrefix(Mockito.anyString())).thenReturn(FIXED_JOBNAME);
        ZosBatchManagerImpl.setZosManager(zosManagerMock);
    }
    
    @Test
    public void testGetJobname() throws ZosBatchManagerException {        
        ZosBatchJobnameImpl zosJobname = new ZosBatchJobnameImpl("image");
        Assert.assertEquals("getName() should return the supplied value", FIXED_JOBNAME, zosJobname.getName());
        Assert.assertEquals("toString() should return the job name", FIXED_JOBNAME, zosJobname.toString());
    }
    
    @Test
    public void testException() throws ZosBatchManagerException {
    	Mockito.when(zosManagerMock.getZosBatchPropertyJobnamePrefix(Mockito.anyString())).thenThrow(new ZosBatchManagerException("exception"));
        
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Problem getting batch jobname prefix");
        new ZosBatchJobnameImpl("image");
    }
}
