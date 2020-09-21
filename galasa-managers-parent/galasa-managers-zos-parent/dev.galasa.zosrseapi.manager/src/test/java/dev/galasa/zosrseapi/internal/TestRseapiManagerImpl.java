/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal;

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
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.properties.Https;
import dev.galasa.zosrseapi.internal.properties.RequestRetry;
import dev.galasa.zosrseapi.internal.properties.RseapiPropertiesSingleton;
import dev.galasa.zosrseapi.internal.properties.ServerHostname;
import dev.galasa.zosrseapi.internal.properties.ServerImages;
import dev.galasa.zosrseapi.internal.properties.ServerPort;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerImages.class, ServerHostname.class, ServerPort.class, Https.class, RequestRetry.class})
public class TestRseapiManagerImpl {
    
    private RseapiManagerImpl rseapiManager;
    
    private RseapiManagerImpl rseapiManagerSpy;
    
    private RseapiPropertiesSingleton rseapiRseapiPropertiesSingleton;

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
    private RseapiImpl rseapiMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String IMAGE = "image";

    private static final String IMAGE_TAG = "tag";

    private static final String CLUSTER = "cluster";

    private static final String HOSTNAME = "hostname";

    private static final String PORT = "999";

    @Before
    public void setup() throws Exception {
        RseapiManagerImpl.setZosManager(zosManagerMock);
        RseapiManagerImpl.setHttpManager(httpManagerMock);
        rseapiRseapiPropertiesSingleton = new RseapiPropertiesSingleton();
        rseapiRseapiPropertiesSingleton.activate();
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        rseapiManager = new RseapiManagerImpl();
        rseapiManagerSpy = Mockito.spy(rseapiManager);
        Mockito.when(rseapiManagerSpy.getFramework()).thenReturn(frameworkMock);
        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        allManagers.add(managerMock);
        rseapiManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestRseapiManagerImpl.class));
        Assert.assertEquals("Error in initialise() method", rseapiManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
    public void testInitialise1() throws ManagerException {
        Mockito.doNothing().when(rseapiManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        rseapiManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", rseapiManagerSpy.getFramework(), frameworkMock);
    }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(RseapiManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        rseapiManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(rseapiManagerSpy, "generateAnnotatedFields", Mockito.any());
        rseapiManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(rseapiManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }
    
    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(zosManagerMock);
        allManagers.add(httpManagerMock);
        rseapiManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(rseapiManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(rseapiManagerSpy);
        rseapiManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(rseapiManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testYouAreRequiredException1() throws ManagerException {
        exceptionRule.expect(RseapiManagerException.class);
        exceptionRule.expectMessage("The zOS Manager is not available");
        rseapiManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException2() throws ManagerException {
        allManagers.add(zosManagerMock);
        exceptionRule.expect(RseapiManagerException.class);
        exceptionRule.expectMessage("The HTTP Manager is not available");
        rseapiManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testAreYouProvisionalDependentOn() {
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , rseapiManager.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertTrue("Should be dependent on IHttpManagerSpi" , rseapiManager.areYouProvisionalDependentOn(httpManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , rseapiManager.areYouProvisionalDependentOn(managerMock));
    }
    
    @Test
    public void testGenerateRseapi() throws ZosManagerException, NoSuchFieldException, SecurityException {
        setupRseapiImplInitialize();
        
        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
        
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosrseapi.Rseapi.class);
        annotations.add(annotation);
        
        Object rseapiImplObject = rseapiManager.generateRseapi(DummyTestClass.class.getDeclaredField("rseapi"), annotations);
        Assert.assertTrue("Error in generateRseapi() method", rseapiImplObject instanceof RseapiImpl);
        
        HashMap<String, RseapiImpl> taggedRseapis = new HashMap<>();
        RseapiImpl rseapiImpl = Mockito.mock(RseapiImpl.class);
        taggedRseapis.put(IMAGE_TAG, rseapiImpl);
        Whitebox.setInternalState(rseapiManagerSpy, "taggedRseapis", taggedRseapis);
        
        rseapiImplObject = rseapiManagerSpy.generateRseapi(DummyTestClass.class.getDeclaredField("rseapi"), annotations);
        Assert.assertEquals("generateRseapi() should retrn the supplied instance of ZosBatchImpl", rseapiImpl, rseapiImplObject);
    }
    
    @Test
    public void testNewRseapi() throws RseapiManagerException {
        setupRseapiImplInitialize();
        
        IRseapi rseapi = rseapiManagerSpy.newRseapi(zosImageMock);
        Assert.assertNotNull("getRseapi() should not be null", rseapi);
        IRseapi rseapi2 = rseapiManagerSpy.newRseapi(zosImageMock);
        Assert.assertEquals("getRseapi() should return the existing Irseapi instance", rseapi, rseapi2);
    }
    
    @Test
    public void testGetRseapis() throws ZosManagerException {
        setupRseapiImplInitialize();
        
        Mockito.doReturn(rseapiMock).when(rseapiManagerSpy).newRseapi(Mockito.any());
        Mockito.when(zosManagerMock.getImage(Mockito.anyString())).thenReturn(zosImageMock);
        
        Assert.assertTrue("getRseapis() should return the mocked RseapiImpl", rseapiManagerSpy.getRseapis(CLUSTER).containsValue(rseapiMock));
        
        Assert.assertTrue("getRseapis() should return the mocked RseapiImpl", rseapiManagerSpy.getRseapis(CLUSTER).containsValue(rseapiMock));
    }
    
    @Test
    public void testGetRseapisException1() throws ZosManagerException {
        setupRseapiImplInitialize();

        Mockito.when(zosManagerMock.getImage(Mockito.anyString())).thenThrow(new ZosManagerException());
        Whitebox.setInternalState(rseapiManagerSpy, "rseapis", new HashMap<>());
        exceptionRule.expect(RseapiManagerException.class);
        exceptionRule.expectMessage("Unable to get RSE API servers for cluster \"" + CLUSTER + "\"");
        
        rseapiManagerSpy.getRseapis(CLUSTER);
    }
    
    @Test
    public void testGetRseapisException2() throws ZosManagerException {
        setupRseapiImplInitialize();
        
        PowerMockito.mockStatic(ServerImages.class);
        Mockito.when(ServerImages.get(Mockito.any())).thenReturn(Arrays.asList());

        Mockito.when(zosManagerMock.getImage(Mockito.anyString())).thenThrow(new ZosManagerException());
        Whitebox.setInternalState(rseapiManagerSpy, "rseapis", new HashMap<>());
        exceptionRule.expect(RseapiManagerException.class);
        exceptionRule.expectMessage("No RSE API servers defined for cluster \"" + CLUSTER + "\"");
        
        rseapiManagerSpy.getRseapis(CLUSTER);
    }
    
    @Test
    public void testNewRseapiRestApiProcessor() throws ZosManagerException {
        setupRseapiImplInitialize();
        
        HashMap<String, IRseapi> rseapis = new HashMap<>();
        rseapis.put(IMAGE, rseapiMock);
        Mockito.doReturn(rseapis).when(rseapiManagerSpy).getRseapis(CLUSTER);
        
        Assert.assertEquals("newRseapiRestApiProcessor() should return the mocked RseapiImpl", rseapiMock, ((RseapiRestApiProcessor) rseapiManagerSpy.newRseapiRestApiProcessor(zosImageMock, false)).getCurrentRseapiServer());
        
        Whitebox.setInternalState(rseapiManagerSpy, "rseapis", rseapis);
        Assert.assertEquals("newRseapiRestApiProcessor() should return the mocked RseapiImpl", rseapiMock, ((RseapiRestApiProcessor) rseapiManagerSpy.newRseapiRestApiProcessor(zosImageMock, true)).getCurrentRseapiServer());
        
        rseapis.clear();
        Whitebox.setInternalState(rseapiManagerSpy, "rseapis", rseapis);
        exceptionRule.expect(RseapiManagerException.class);
        exceptionRule.expectMessage("No RSE API sever configured on " + IMAGE);
        rseapiManagerSpy.newRseapiRestApiProcessor(zosImageMock, true);
    }

    private void setupRseapiImplInitialize() throws RseapiManagerException {        
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
        
        Whitebox.setInternalState(RseapiManagerImpl.class, "httpManager", httpManagerMock);
        Mockito.when(httpManagerMock.newHttpClient()).thenReturn(httpClientMock);
       
        PowerMockito.mockStatic(RequestRetry.class);
        Mockito.when(RequestRetry.get(Mockito.any())).thenReturn(5);
    }

    class DummyTestClass {
        @dev.galasa.zosrseapi.Rseapi(imageTag="tag")
        public dev.galasa.zosrseapi.IRseapi rseapi;
        @dev.galasa.Test
        public void dummyTestMethod() throws RseapiException {
            rseapi.delete("path", null);
        }
    }
}
