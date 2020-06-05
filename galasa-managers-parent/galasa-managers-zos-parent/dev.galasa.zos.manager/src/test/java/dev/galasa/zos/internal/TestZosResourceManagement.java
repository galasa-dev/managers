/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos.internal;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.ResourceManagerException;

@RunWith(PowerMockRunner.class)
public class TestZosResourceManagement {

    private ZosResourceManagement zosResourceManagement;
    
    private ZosResourceManagement zosResourceManagementSpy;
    
    @Mock
    private ScheduledExecutorService scheduledExecutorServiceMock;
    
    @Mock
    private Random randomMock;
    
    @Mock
    private IFramework frameworkMock;
    
    @Mock
    private IResourceManagement resourceManagementMock;

    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String RUN_NAME = "RUN-NAME";
    
    @Test
    public void testZosResourceManagement() throws ResourceManagerException, DynamicStatusStoreException {
        zosResourceManagement = new ZosResourceManagement();
        zosResourceManagementSpy = PowerMockito.spy(zosResourceManagement);
        
        Assert.assertTrue("initialise() should return true", zosResourceManagementSpy.initialise(frameworkMock, resourceManagementMock));

        Mockito.doReturn(scheduledExecutorServiceMock).when(resourceManagementMock).getScheduledExecutorService();
        Mockito.when(frameworkMock.getRandom()).thenReturn(randomMock);
        zosResourceManagementSpy.start();
        
        zosResourceManagementSpy.shutdown();
        
        zosResourceManagementSpy.runFinishedOrDeleted(RUN_NAME);
        
        PowerMockito.when(frameworkMock.getDynamicStatusStoreService(Mockito.anyString())).thenThrow(new DynamicStatusStoreException());
        exceptionRule.expect(ResourceManagerException.class);
        exceptionRule.expectMessage("Unable to initialise zOS resource monitor");
        zosResourceManagementSpy.initialise(frameworkMock, resourceManagementMock);
    }
    

}
