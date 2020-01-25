/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.ZosmfManagerException;

@RunWith(MockitoJUnitRunner.class)
public class ManagerTest {
    
    @Mock
    private IZosmf zosmf; 
    
    @Test
    public void testZosManagerException() throws ZosmfManagerException {
        
        Assert.assertEquals("dummy", null, zosmf.putText(null, null, null));
    }
    
    
}
