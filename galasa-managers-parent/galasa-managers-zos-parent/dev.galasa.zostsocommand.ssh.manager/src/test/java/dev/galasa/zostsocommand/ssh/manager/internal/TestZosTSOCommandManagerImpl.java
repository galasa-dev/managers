package dev.galasa.zostsocommand.ssh.manager.internal;
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
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zostsocommand.ssh.manager.internal.properties.ZosTSOCommandSshPropertiesSingleton;
import dev.galasa.zosunixcommand.ZosUNIXCommandManagerException;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandImpl;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandManagerImpl;
import dev.galasa.zostsocommand.IZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommandException;
import dev.galasa.zostsocommand.ZosTSOCommandManagerException;

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
    private ZosUNIXCommandImpl zosUnixCommandMock;

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
        zosTSOCommandManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosTSOCommandManagerImpl.class));
        Assert.assertEquals("Error in initialise() method", zosTSOCommandManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
    public void testInitialise1() throws ManagerException {
        Mockito.doNothing().when(zosTSOCommandManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        zosTSOCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", zosTSOCommandManagerSpy.getFramework(), frameworkMock);
    }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosTSOCommandManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        zosTSOCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
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
        exceptionRule.expectMessage("The zOS UNIX Command Manager is not available");
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
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zostsocommand.ZosTSOCommand.class);
        annotations.add(annotation);
        Whitebox.setInternalState(ZosTSOCommandManagerImpl.class, "zosUnixCommandManager", zosUNIXCommandManagerMock);
        Mockito.when(zosUNIXCommandManagerMock.getZosUNIXCommand(Mockito.any())).thenReturn(zosUnixCommandMock);
        
        Object zosTSOImplObject = zosTSOCommandManager.generateZosTSOCommand(DummyTestClass.class.getDeclaredField("zosTSOCommand"), annotations);
        Assert.assertTrue("Error in generateZosTSO() method", zosTSOImplObject instanceof ZosTSOCommandImpl);
        
        HashMap<String, ZosTSOCommandImpl> taggedZosTSOCommands = new HashMap<>();
        ZosTSOCommandImpl zosTSOCommandImpl = Mockito.mock(ZosTSOCommandImpl.class);
        taggedZosTSOCommands.put("tag", zosTSOCommandImpl);
        Whitebox.setInternalState(zosTSOCommandManagerSpy, "taggedZosTSOCommands", taggedZosTSOCommands);
        
        zosTSOImplObject = zosTSOCommandManagerSpy.generateZosTSOCommand(DummyTestClass.class.getDeclaredField("zosTSOCommand"), annotations);
        Assert.assertEquals("generateZosTSO() should retrn the supplied instance of ZosTSOCommandImpl", zosTSOCommandImpl, zosTSOImplObject);
    }
    
    @Test
    public void testGetZosTSO() throws ZosTSOCommandManagerException, ZosUNIXCommandManagerException {
        Whitebox.setInternalState(ZosTSOCommandManagerImpl.class, "zosUnixCommandManager", zosUNIXCommandManagerMock);
        Mockito.when(zosUNIXCommandManagerMock.getZosUNIXCommand(Mockito.any())).thenReturn(zosUnixCommandMock);
        IZosTSOCommand zosTSOCommand = zosTSOCommandManagerSpy.getZosTSOCommand(zosImageMock);
        Assert.assertNotNull("getZosTSO() should not be null", zosTSOCommand);
        IZosTSOCommand zosTSOCommand2 = zosTSOCommandManagerSpy.getZosTSOCommand(zosImageMock);
        Assert.assertEquals("getZosTSO() should return the existing IZosTSO instance", zosTSOCommand, zosTSOCommand2);
    }
    
    class DummyTestClass {
        @dev.galasa.zostsocommand.ZosTSOCommand(imageTag="tag")
        public dev.galasa.zostsocommand.IZosTSOCommand zosTSOCommand;
        @dev.galasa.Test
        public void dummyTestMethod() throws ZosTSOCommandException {
            zosTSOCommand.issueCommand("command");
        }
    }
}