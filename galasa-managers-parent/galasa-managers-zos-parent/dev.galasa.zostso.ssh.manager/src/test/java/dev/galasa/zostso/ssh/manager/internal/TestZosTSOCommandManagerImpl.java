package dev.galasa.zostso.ssh.manager.internal;
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */


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
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zostso.IZosTSO;
import dev.galasa.zostso.ZosTSOCommandException;
import dev.galasa.zostso.ZosTSOCommandManagerException;
import dev.galasa.zostso.ssh.manager.internal.properties.ZosTSOCommandSshPropertiesSingleton;
import dev.galasa.zosunix.ssh.manager.internal.ZosUNIXCommandManagerImpl;

@RunWith(PowerMockRunner.class)
public class TestZosTSOCommandManagerImpl {
    
    private ZosTSOCommandManagerImpl zosTSOCommandManager;
    
    private ZosTSOCommandManagerImpl zosTSOCommandManagerSpy;
    
    private ZosTSOCommandSshPropertiesSingleton zosTSOCommandSshPropertiesSingleton;

    private List<IManager> allManagers;
    
    private List<IManager> activeManagers;
    
    @Mock
    private IFramework frameworkMock;
    
    @Mock
    public IManager managerMock;
    
    @Mock
    private ZosManagerImpl zosManagerMock;
    
    @Mock
    private ZosUNIXCommandManagerImpl zosUNIXCommandManagerMock;

    @Mock
    private IZosImage zosImageMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setup() throws Exception {        
        ZosTSOCommandManagerImpl.setZosManager(zosManagerMock);
        zosTSOCommandSshPropertiesSingleton = new ZosTSOCommandSshPropertiesSingleton();
        zosTSOCommandSshPropertiesSingleton.activate();
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        zosTSOCommandManager = new ZosTSOCommandManagerImpl();
        zosTSOCommandManagerSpy = Mockito.spy(zosTSOCommandManager);
        Mockito.when(zosTSOCommandManagerSpy.getFramework()).thenReturn(frameworkMock);
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        allManagers.add(managerMock);
        zosTSOCommandManager.initialise(frameworkMock, allManagers, activeManagers, TestZosTSOCommandManagerImpl.class);
        Assert.assertEquals("Error in initialise() method", zosTSOCommandManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
    public void testInitialise1() throws ManagerException {
        Mockito.doNothing().when(zosTSOCommandManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        zosTSOCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, DummyTestClass.class);
        Assert.assertEquals("Error in initialise() method", zosTSOCommandManagerSpy.getFramework(), frameworkMock);
    }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        zosTSOCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, DummyTestClass.class);
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(zosTSOCommandManagerSpy, "generateAnnotatedFields", Mockito.any());
        zosTSOCommandManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(zosTSOCommandManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }
    
    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(zosManagerMock);
        allManagers.add(zosUNIXCommandManagerMock);
        zosTSOCommandManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosTSOCommandManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosTSOCommandManagerSpy);
        zosTSOCommandManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosTSOCommandManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testYouAreRequiredException1() throws ManagerException {
        exceptionRule.expect(ManagerException.class);
        exceptionRule.expectMessage("The zOS Manager is not available");
        zosTSOCommandManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException2() throws ManagerException {
        allManagers.add(zosManagerMock);
        exceptionRule.expect(ManagerException.class);
        exceptionRule.expectMessage("The zOS UNIX Manager is not available");
        zosTSOCommandManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testAreYouProvisionalDependentOn() {
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosTSOCommandManager.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertTrue("Should be dependent on IZosUNIXManagerSpi" , zosTSOCommandManager.areYouProvisionalDependentOn(zosUNIXCommandManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , zosTSOCommandManager.areYouProvisionalDependentOn(managerMock));
    }
    
    @Test
    public void testGenerateZosTSO() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zostso.ZosTSO.class);
        annotations.add(annotation);
        
        Object zosTSOImplObject = zosTSOCommandManager.generateZosTSO(DummyTestClass.class.getDeclaredField("zosTSO"), annotations);
        Assert.assertTrue("Error in generateZosTSO() method", zosTSOImplObject instanceof ZosTSOImpl);
        
        HashMap<String, ZosTSOImpl> taggedZosTSOs = new HashMap<>();
        ZosTSOImpl zosTSOImpl = Mockito.mock(ZosTSOImpl.class);
        taggedZosTSOs.put("tag", zosTSOImpl);
        Whitebox.setInternalState(zosTSOCommandManagerSpy, "taggedZosTSOs", taggedZosTSOs);
        
        zosTSOImplObject = zosTSOCommandManagerSpy.generateZosTSO(DummyTestClass.class.getDeclaredField("zosTSO"), annotations);
        Assert.assertEquals("generateZosTSO() should retrn the supplied instance of ZosTSOImpl", zosTSOImpl, zosTSOImplObject);
    }
    
    @Test
    public void testGetZosTSO() throws ZosTSOCommandManagerException {
        IZosTSO zosTSO = zosTSOCommandManagerSpy.getZosTSO(zosImageMock);
        Assert.assertNotNull("getZosTSO() should not be null", zosTSO);
        IZosTSO zosTSO2 = zosTSOCommandManagerSpy.getZosTSO(zosImageMock);
        Assert.assertEquals("getZosTSO() should return the existing IZosTSO instance", zosTSO, zosTSO2);
    }
    
    class DummyTestClass {
        @dev.galasa.zostso.ZosTSO(imageTag="tag")
        public dev.galasa.zostso.IZosTSO zosTSO;
        @dev.galasa.Test
        public void dummyTestMethod() throws ZosTSOCommandException {
            zosTSO.issueCommand("command");
        }
    }
}