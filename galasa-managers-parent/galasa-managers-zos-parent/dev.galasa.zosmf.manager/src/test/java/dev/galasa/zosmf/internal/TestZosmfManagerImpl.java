/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosmf.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.internal.HttpManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.properties.Https;
import dev.galasa.zosmf.internal.properties.RequestRetry;
import dev.galasa.zosmf.internal.properties.ServerHostname;
import dev.galasa.zosmf.internal.properties.ServerImages;
import dev.galasa.zosmf.internal.properties.ServerPort;
import dev.galasa.zosmf.internal.properties.ZosmfPropertiesSingleton;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerImages.class, ServerHostname.class, ServerPort.class, Https.class, RequestRetry.class})
public class TestZosmfManagerImpl {
    
    private ZosmfManagerImpl zosmfManager;
    
    private ZosmfManagerImpl zosmfManagerSpy;
    
    private ZosmfPropertiesSingleton zosmfZosmfPropertiesSingleton;

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
    private HttpManagerImpl httpManagerMock;
    
    @Mock
    private IHttpClient httpClientMock;

    @Mock
    private IZosImage zosImageMock;

    @Mock
    private ZosmfImpl zosmfMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String IMAGE = "image";

    private static final String IMAGE_TAG = "tag";

    private static final String CLUSTER = "cluster";

    private static final String HOSTNAME = "hostname";

    private static final String PORT = "999";

    @Before
    public void setup() throws Exception {
        ZosmfManagerImpl.setZosManager(zosManagerMock);
        ZosmfManagerImpl.setHttpManager(httpManagerMock);
        zosmfZosmfPropertiesSingleton = new ZosmfPropertiesSingleton();
        zosmfZosmfPropertiesSingleton.activate();
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        zosmfManager = new ZosmfManagerImpl();
        zosmfManagerSpy = Mockito.spy(zosmfManager);
        Mockito.when(zosmfManagerSpy.getFramework()).thenReturn(frameworkMock);
        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        allManagers.add(managerMock);
        zosmfManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosmfManagerImpl.class));
        Assert.assertEquals("Error in initialise() method", zosmfManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
    public void testInitialise1() throws ManagerException {
        Mockito.doNothing().when(zosmfManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        zosmfManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", zosmfManagerSpy.getFramework(), frameworkMock);
    }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        zosmfManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(zosmfManagerSpy, "generateAnnotatedFields", Mockito.any());
        zosmfManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(zosmfManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }
    
    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(zosManagerMock);
        allManagers.add(httpManagerMock);
        zosmfManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosmfManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosmfManagerSpy);
        zosmfManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosmfManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testYouAreRequiredException1() throws ManagerException {
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("The zOS Manager is not available");
        zosmfManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException2() throws ManagerException {
        allManagers.add(zosManagerMock);
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("The HTTP Manager is not available");
        zosmfManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testAreYouProvisionalDependentOn() {
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosmfManager.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertTrue("Should be dependent on IHttpManagerSpi" , zosmfManager.areYouProvisionalDependentOn(httpManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , zosmfManager.areYouProvisionalDependentOn(managerMock));
    }
    
    @Test
    public void testGenerateZosmf() throws ZosManagerException, NoSuchFieldException, SecurityException {
        setupZosmfImplInitialize();
        
        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
        
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosmf.Zosmf.class);
        annotations.add(annotation);
        
        Object zosmfImplObject = zosmfManager.generateZosmf(DummyTestClass.class.getDeclaredField("zosmf"), annotations);
        Assert.assertTrue("Error in generateZosmf() method", zosmfImplObject instanceof ZosmfImpl);
        
        HashMap<String, ZosmfImpl> taggedZosmfs = new HashMap<>();
        ZosmfImpl zosmfImpl = Mockito.mock(ZosmfImpl.class);
        taggedZosmfs.put(IMAGE_TAG, zosmfImpl);
        Whitebox.setInternalState(zosmfManagerSpy, "taggedZosmfs", taggedZosmfs);
        
        zosmfImplObject = zosmfManagerSpy.generateZosmf(DummyTestClass.class.getDeclaredField("zosmf"), annotations);
        Assert.assertEquals("generateZosmf() should retrn the supplied instance of ZosBatchImpl", zosmfImpl, zosmfImplObject);
    }
    
    @Test
    public void testNewZosmf() throws ZosmfManagerException {
        setupZosmfImplInitialize();
        
        IZosmf zosmf = zosmfManagerSpy.newZosmf(zosImageMock);
        Assert.assertNotNull("getZosmf() should not be null", zosmf);
        IZosmf zosmf2 = zosmfManagerSpy.newZosmf(zosImageMock);
        Assert.assertEquals("getZosmf() should return the existing IZosmf instance", zosmf, zosmf2);
    }
    
    @Test
    public void testGetZosmfs() throws ZosManagerException {
        setupZosmfImplInitialize();
        
        Mockito.doReturn(zosmfMock).when(zosmfManagerSpy).newZosmf(Mockito.any());
        Mockito.when(zosManagerMock.getImage(Mockito.anyString())).thenReturn(zosImageMock);
        
        Assert.assertTrue("getZosmfs() should return the mocked ZosmfImpl", zosmfManagerSpy.getZosmfs(CLUSTER).containsValue(zosmfMock));
        
        Assert.assertTrue("getZosmfs() should return the mocked ZosmfImpl", zosmfManagerSpy.getZosmfs(CLUSTER).containsValue(zosmfMock));

        Mockito.when(zosManagerMock.getImage(Mockito.anyString())).thenThrow(new ZosManagerException());
        Whitebox.setInternalState(zosmfManagerSpy, "zosmfs", new HashMap<>());
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("Unable to get zOSMF servers for cluster " + CLUSTER);
        
        zosmfManagerSpy.getZosmfs(CLUSTER);
    }
    
    @Test
    public void testNewZosmfRestApiProcessor() throws ZosManagerException {
        setupZosmfImplInitialize();
        
        HashMap<String, IZosmf> zosmfs = new HashMap<>();
        zosmfs.put(IMAGE, zosmfMock);
        Mockito.doReturn(zosmfs).when(zosmfManagerSpy).getZosmfs(CLUSTER);
        
        Assert.assertEquals("newZosmfRestApiProcessor() should return the mocked ZosmfImpl", zosmfMock, ((ZosmfRestApiProcessor) zosmfManagerSpy.newZosmfRestApiProcessor(zosImageMock, false)).getCurrentZosmfServer());
        
        Whitebox.setInternalState(zosmfManagerSpy, "zosmfs", zosmfs);
        Assert.assertEquals("newZosmfRestApiProcessor() should return the mocked ZosmfImpl", zosmfMock, ((ZosmfRestApiProcessor) zosmfManagerSpy.newZosmfRestApiProcessor(zosImageMock, true)).getCurrentZosmfServer());
        
        zosmfs.clear();
        Whitebox.setInternalState(zosmfManagerSpy, "zosmfs", zosmfs);
        exceptionRule.expect(ZosmfManagerException.class);
        exceptionRule.expectMessage("No zOSMF sever configured on " + IMAGE);
        zosmfManagerSpy.newZosmfRestApiProcessor(zosImageMock, true);
    }

    private void setupZosmfImplInitialize() throws ZosmfManagerException {        
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        Mockito.when(zosImageMock.getClusterID()).thenReturn(CLUSTER);
        
        PowerMockito.mockStatic(ServerImages.class);
        Mockito.when(ServerImages.get(Mockito.any())).thenReturn(Arrays.asList(IMAGE));
        
        PowerMockito.mockStatic(ServerHostname.class);
        Mockito.when(ServerHostname.get(Mockito.any())).thenReturn(HOSTNAME);
        
        PowerMockito.mockStatic(ServerPort.class);
        Mockito.when(ServerPort.get(Mockito.any())).thenReturn(PORT);
        
        PowerMockito.mockStatic(Https.class);
        Mockito.when(Https.get(Mockito.any())).thenReturn(true);
        
        Whitebox.setInternalState(ZosmfManagerImpl.class, "httpManager", httpManagerMock);
        Mockito.when(httpManagerMock.newHttpClient()).thenReturn(httpClientMock);
       
        PowerMockito.mockStatic(RequestRetry.class);
        Mockito.when(RequestRetry.get(Mockito.any())).thenReturn(5);
    }

    class DummyTestClass {
        @dev.galasa.zosmf.Zosmf(imageTag="tag")
        public dev.galasa.zosmf.IZosmf zosmf;
        @dev.galasa.Test
        public void dummyTestMethod() throws ZosmfException {
            zosmf.delete("path", null);
        }
    }
}
