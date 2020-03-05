/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.zosmf.manager.internal.properties.ZosConsoleZosmfPropertiesSingleton;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
public class TestZosConsoleManagerImpl {
    
    private ZosConsoleManagerImpl zosConsoleManager;
    
    private ZosConsoleManagerImpl zosConsoleManagerSpy;
    
    private ZosConsoleZosmfPropertiesSingleton zosConsoleZosmfPropertiesSingleton;

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
        ZosConsoleManagerImpl.setZosManager(zosManagerMock);
        ZosConsoleManagerImpl.setZosmfManager(zosmfManagerMock);
        zosConsoleZosmfPropertiesSingleton = new ZosConsoleZosmfPropertiesSingleton();
        zosConsoleZosmfPropertiesSingleton.activate();
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        zosConsoleManager = new ZosConsoleManagerImpl();
        zosConsoleManagerSpy = Mockito.spy(zosConsoleManager);
        Mockito.when(zosConsoleManagerSpy.getFramework()).thenReturn(frameworkMock);
        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        allManagers.add(managerMock);
        zosConsoleManager.initialise(frameworkMock, allManagers, activeManagers, TestZosConsoleManagerImpl.class);
        Assert.assertEquals("Error in initialise() method", zosConsoleManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
    public void testInitialise1() throws ManagerException {
        Mockito.doNothing().when(zosConsoleManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        zosConsoleManagerSpy.initialise(frameworkMock, allManagers, activeManagers, DummyTestClass.class);
        Assert.assertEquals("Error in initialise() method", zosConsoleManagerSpy.getFramework(), frameworkMock);
    }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosConsoleManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        zosConsoleManagerSpy.initialise(frameworkMock, allManagers, activeManagers, DummyTestClass.class);
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(zosConsoleManagerSpy, "generateAnnotatedFields", Mockito.any());
        zosConsoleManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(zosConsoleManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }
    
    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(zosManagerMock);
        allManagers.add(zosmfManagerMock);
        zosConsoleManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosConsoleManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosConsoleManagerSpy);
        zosConsoleManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosConsoleManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testYouAreRequiredException1() throws ManagerException {
        exceptionRule.expect(ZosConsoleManagerException.class);
        exceptionRule.expectMessage("The zOS Manager is not available");
        zosConsoleManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException2() throws ManagerException {
        allManagers.add(zosManagerMock);
        exceptionRule.expect(ZosConsoleManagerException.class);
        exceptionRule.expectMessage("The zOSMF Manager is not available");
        zosConsoleManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testAreYouProvisionalDependentOn() {
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosConsoleManager.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertTrue("Should be dependent on IZosmfManagerSpi" , zosConsoleManager.areYouProvisionalDependentOn(zosmfManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , zosConsoleManager.areYouProvisionalDependentOn(managerMock));
    }
    
    @Test
    public void testGenerateZosConsole() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosconsole.ZosConsole.class);
        annotations.add(annotation);
        
        Object zosConsoleImplObject = zosConsoleManager.generateZosConsole(DummyTestClass.class.getDeclaredField("zosConsole"), annotations);
        Assert.assertTrue("Error in generateZosConsole() method", zosConsoleImplObject instanceof ZosConsoleImpl);
        
        HashMap<String, ZosConsoleImpl> taggedZosConsoles = new HashMap<>();
        ZosConsoleImpl zosConsoleImpl = Mockito.mock(ZosConsoleImpl.class);
        taggedZosConsoles.put("tag", zosConsoleImpl);
        Whitebox.setInternalState(zosConsoleManagerSpy, "taggedZosConsoles", taggedZosConsoles);
        
        zosConsoleImplObject = zosConsoleManagerSpy.generateZosConsole(DummyTestClass.class.getDeclaredField("zosConsole"), annotations);
        Assert.assertEquals("generateZosConsole() should retrn the supplied instance of ZosBatchImpl", zosConsoleImpl, zosConsoleImplObject);
    }
    
    class DummyTestClass {
        @dev.galasa.zosconsole.ZosConsole(imageTag="tag")
        public dev.galasa.zosconsole.IZosConsole zosConsole;
        @dev.galasa.Test
        public void dummyTestMethod() throws ZosConsoleException {
            zosConsole.issueCommand("command");
        }
    }
}
