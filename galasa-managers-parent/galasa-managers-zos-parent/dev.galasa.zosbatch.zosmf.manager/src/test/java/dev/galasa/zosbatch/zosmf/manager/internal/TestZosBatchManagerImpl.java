/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchImpl;
import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchJobnameImpl;
import dev.galasa.zosbatch.zosmf.manager.internal.ZosBatchManagerImpl;
import dev.galasa.zosbatch.zosmf.manager.internal.properties.ZosBatchZosmfPropertiesSingleton;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
public class TestZosBatchManagerImpl {
    
    private ZosBatchManagerImpl zosBatchManager;
    
    private ZosBatchManagerImpl zosBatchManagerSpy;
    
    private ZosBatchZosmfPropertiesSingleton zosBatchZosmfPropertiesSingleton;

    private List<IManager> allManagers;
    
    private List<IManager> activeManagers;
    
    @Mock
    private IFramework frameworkMock;
    
    @Mock
    private IResultArchiveStore resultArchiveStoreMock;
    
    @Mock
    public IManager managerMock;
    
    @Mock
    private ZosManagerImpl zosManagerMock;

    @Mock
    private ZosmfManagerImpl zosmfManagerMock;

    @Mock
    private IZosImage zosImageMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() throws Exception {        
        ZosBatchManagerImpl.setZosManager(zosManagerMock);
        ZosBatchManagerImpl.setZosmfManager(zosmfManagerMock);
        ZosBatchManagerImpl.setCurrentTestMethod(this.getClass().getDeclaredMethod("setup"));
        zosBatchZosmfPropertiesSingleton = new ZosBatchZosmfPropertiesSingleton();
        zosBatchZosmfPropertiesSingleton.activate();
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        zosBatchManager = new ZosBatchManagerImpl();
        zosBatchManagerSpy = Mockito.spy(zosBatchManager);
        Mockito.when(zosBatchManagerSpy.getFramework()).thenReturn(frameworkMock);
        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        allManagers.add(managerMock);
        zosBatchManager.initialise(frameworkMock, allManagers, activeManagers, TestZosBatchManagerImpl.class);
        Assert.assertEquals("Error in initialise() method", zosBatchManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
        public void testInitialise1() throws ManagerException {
            Mockito.doNothing().when(zosBatchManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
            zosBatchManagerSpy.initialise(frameworkMock, allManagers, activeManagers, DummyTestClass.class);
            Assert.assertEquals("Error in initialise() method", zosBatchManagerSpy.getFramework(), frameworkMock);
        }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        zosBatchManagerSpy.initialise(frameworkMock, allManagers, activeManagers, DummyTestClass.class);
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(zosBatchManagerSpy, "generateAnnotatedFields", Mockito.any());
        zosBatchManagerSpy.provisionGenerate();
        Assert.assertTrue("Error in provisionGenerate() method", true);
    }
    
    @Test
    public void testYouAreRequired() throws ManagerException {
        allManagers.add(zosManagerMock);
        allManagers.add(zosmfManagerMock);
        zosBatchManagerSpy.youAreRequired(allManagers, activeManagers);
        
        zosBatchManagerSpy.youAreRequired(allManagers, activeManagers);
        
        Assert.assertTrue("Error in youAreRequired() method", true);
    }
    
    @Test
    public void testYouAreRequiredException1() throws ManagerException {
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("The zOS Manager is not available");
        zosBatchManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException2() throws ManagerException {
        allManagers.add(zosManagerMock);
        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage("The zOSMF Manager is not available");
        zosBatchManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testAreYouProvisionalDependentOn() {
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosBatchManager.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertTrue("Should be dependent on IZosmfManagerSpi" , zosBatchManager.areYouProvisionalDependentOn(zosmfManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , zosBatchManager.areYouProvisionalDependentOn(managerMock));
    }
    
    @Test
    public void testStartOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
        zosBatchManager.startOfTestMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethod"));
        
        Assert.assertTrue("Error in startOfTestMethod() method", true);
    }
    
    @Test
    public void testEndOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
        zosBatchManager.endOfTestMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethod"), "pass", null);
        
        HashMap<String, ZosBatchImpl> taggedZosBatches = new HashMap<>();
        ZosBatchImpl zosBatchImpl = Mockito.mock(ZosBatchImpl.class);
        Mockito.doNothing().when(zosBatchImpl).cleanup();
        taggedZosBatches.put("TAG", zosBatchImpl);
        Whitebox.setInternalState(zosBatchManagerSpy, "taggedZosBatches", taggedZosBatches);

        zosBatchManagerSpy.endOfTestMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethod"), "pass", null);
        
        Assert.assertTrue("Error in endOfTestMethod() method", true);
    }
    
    @Test
    public void testGenerateZosBatch() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosbatch.ZosBatch.class);
        annotations.add(annotation);
        
        zosBatchManager.generateZosBatch(DummyTestClass.class.getDeclaredField("zosBatch"), annotations);
        
        HashMap<String, ZosBatchImpl> taggedZosBatches = new HashMap<>();
        ZosBatchImpl zosBatchImpl = Mockito.mock(ZosBatchImpl.class);
        taggedZosBatches.put("tag", zosBatchImpl);
        Whitebox.setInternalState(zosBatchManagerSpy, "taggedZosBatches", taggedZosBatches);
        
        zosBatchManagerSpy.generateZosBatch(DummyTestClass.class.getDeclaredField("zosBatch"), annotations);
        
        Assert.assertTrue("Error in generateZosBatch() method", true);
    }
    
    @Test
    public void testGenerateZosBatchJobname() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosbatch.ZosBatchJobname.class);
        annotations.add(annotation);
        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
        ZosBatchJobnameImpl zosBatchJobnameMock = Mockito.mock(ZosBatchJobnameImpl.class);
        PowerMockito.doReturn(zosBatchJobnameMock).when(zosBatchManagerSpy).newZosBatchJobnameImpl(Mockito.anyString());
        
        Assert.assertEquals("generateZosBatchJobname() should return the mocked ZosBatchJobnameImpl", zosBatchManagerSpy.generateZosBatchJobname(DummyTestClass.class.getDeclaredField("zosBatchJobname"), annotations), zosBatchJobnameMock);
    }
    
    @Test
    public void testGenerateZosBatchJobnameException() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosbatch.ZosBatchJobname.class);
        annotations.add(annotation);
        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenThrow(new ZosBatchManagerException("exception"));

        exceptionRule.expect(ZosBatchManagerException.class);
        exceptionRule.expectMessage(StringStartsWith.startsWith("Unable to get image for tag"));
        
        zosBatchManagerSpy.generateZosBatchJobname(DummyTestClass.class.getDeclaredField("zosBatchJobname"), annotations);
    }
    
    class DummyTestClass {
        @dev.galasa.zosbatch.ZosBatch(imageTag="tag")
        public dev.galasa.zosbatch.IZosBatch zosBatch;
        @dev.galasa.zosbatch.ZosBatchJobname(imageTag="tag")
        public dev.galasa.zosbatch.IZosBatchJobname zosBatchJobname;
        @dev.galasa.Test
        public void dummyTestMethod() throws ZosBatchException {
            zosBatch.submitJob("JCL", null);
        }
    }
}
