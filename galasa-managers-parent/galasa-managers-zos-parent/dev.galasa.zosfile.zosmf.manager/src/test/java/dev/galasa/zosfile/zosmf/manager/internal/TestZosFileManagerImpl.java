/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.ArrayList;
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
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosfile.zosmf.manager.internal.properties.ZosFileZosmfPropertiesSingleton;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
public class TestZosFileManagerImpl {
    
    private ZosFileManagerImpl zosFileManager;
    
    private ZosFileManagerImpl zosFileManagerSpy;
    
    private ZosFileZosmfPropertiesSingleton zosFileZosmfPropertiesSingleton;

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
        ZosFileManagerImpl.setZosManager(zosManagerMock);
        ZosFileManagerImpl.setZosmfManager(zosmfManagerMock);
        zosFileZosmfPropertiesSingleton = new ZosFileZosmfPropertiesSingleton();
        zosFileZosmfPropertiesSingleton.activate();
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        zosFileManager = new ZosFileManagerImpl();
        zosFileManagerSpy = Mockito.spy(zosFileManager);
        Mockito.when(zosFileManagerSpy.getFramework()).thenReturn(frameworkMock);
        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        allManagers.add(managerMock);
        zosFileManager.initialise(frameworkMock, allManagers, activeManagers, TestZosFileManagerImpl.class);
        Assert.assertEquals("Error in initialise() method", zosFileManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
        public void testInitialise1() throws ManagerException {
            Mockito.doNothing().when(zosFileManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
            zosFileManagerSpy.initialise(frameworkMock, allManagers, activeManagers, DummyTestClass.class);
            Assert.assertEquals("Error in initialise() method", zosFileManagerSpy.getFramework(), frameworkMock);
        }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        zosFileManagerSpy.initialise(frameworkMock, allManagers, activeManagers, DummyTestClass.class);
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(zosFileManagerSpy, "generateAnnotatedFields", Mockito.any());
        zosFileManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(zosFileManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }
    
    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(zosManagerMock);
        allManagers.add(zosmfManagerMock);
        zosFileManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosFileManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosFileManagerSpy);        
        zosFileManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosFileManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testYouAreRequiredException1() throws ManagerException {
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("The zOS Manager is not available");
        zosFileManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException2() throws ManagerException {
        allManagers.add(zosManagerMock);
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("The zOSMF Manager is not available");
        zosFileManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testAreYouProvisionalDependentOn() {
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosFileManager.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertTrue("Should be dependent on IZosmfManagerSpi" , zosFileManager.areYouProvisionalDependentOn(zosmfManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , zosFileManager.areYouProvisionalDependentOn(managerMock));
    }
    
    @Test
    public void testStartOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
        zosFileManager.startOfTestMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset"));
        Assert.assertEquals("currentTestMethod should contain the supplied value", "dummyTestMethodDataset", ZosFileManagerImpl.currentTestMethod);
    }
    
    @Test
    public void testEndOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
        ZosFileManagerImpl.setCurrentTestMethod("dummyTestMethodDataset");
        zosFileManagerSpy.endOfTestMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset"), "pass", null);
        Assert.assertNull("currentTestMethod should be null", ZosFileManagerImpl.currentTestMethod);
        
        List<ZosFileHandlerImpl> zosFileHandlers = new ArrayList<>();
        ZosFileHandlerImpl zosFileHandlerImpl = Mockito.mock(ZosFileHandlerImpl.class);
        Mockito.doNothing().when(zosFileHandlerImpl).cleanupEndOfTestMethod();
        zosFileHandlers.add(zosFileHandlerImpl);
        Whitebox.setInternalState(ZosFileManagerImpl.class, "zosFileHandlers", zosFileHandlers);
        
        zosFileManager.endOfTestMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset"), "pass", null);
        Assert.assertNull("currentTestMethod should be null", ZosFileManagerImpl.currentTestMethod);
    }
    
    @Test
    public void testEndOfTestClass() throws Exception {
        Whitebox.setInternalState(ZosFileManagerImpl.class, "zosFileHandlers", new ArrayList<>());
        zosFileManagerSpy.endOfTestClass("pass", null);
        Assert.assertEquals("currentTestMethod should be endOfTestCleanup", "endOfTestCleanup", ZosFileManagerImpl.currentTestMethod);
        
        List<ZosFileHandlerImpl> zosFileHandlers = new ArrayList<>();
        ZosFileHandlerImpl zosFileHandlerImpl = Mockito.mock(ZosFileHandlerImpl.class);
        Mockito.doNothing().when(zosFileHandlerImpl).cleanupEndOfClass();
        zosFileHandlers.add(zosFileHandlerImpl);
        Whitebox.setInternalState(ZosFileManagerImpl.class, "zosFileHandlers", zosFileHandlers);
        
        zosFileManagerSpy.endOfTestClass("pass", null);
        Mockito.verify(zosFileHandlerImpl, Mockito.times(1)).cleanupEndOfClass();
    }
    
    @Test
    public void testGenerateZosFileHandler() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosfile.ZosFileHandler.class);
        annotations.add(annotation);
        
        Object zosFileHandlerObject = zosFileManager.generateZosFileHandler(DummyTestClass.class.getDeclaredField("zosFileHandler"), annotations);
        Assert.assertTrue("Error in generateZosFileHandler() method", zosFileHandlerObject instanceof ZosFileHandlerImpl);
    }
    
    @Test
    public void testRunid() {
        ZosFileManagerImpl.setRunId("RUNID");
        Assert.assertEquals("getRunid() should return the supplied value", "RUNID", ZosFileManagerImpl.getRunId());
    }
    
    @Test
    public void testDatasetArtifactRoot() {
        Path path = new File("datasetArtifactRoot").toPath();
        ZosFileManagerImpl.setDatasetArtifactRoot(path);
        Assert.assertEquals("getDatasetArtifactRoot() should return the supplied value", path, ZosFileManagerImpl.getDatasetArtifactRoot());
    }
    
    @Test
    public void testVsamDatasetArtifactRoot() {
        Path path = new File("vsamDatasetArtifactRoot").toPath();
        ZosFileManagerImpl.setVsamDatasetArtifactRoot(path);
        Assert.assertEquals("getVsamDatasetArtifactRoot() should return the supplied value", path, ZosFileManagerImpl.getVsamDatasetArtifactRoot());
    }
    
    @Test
    public void testUnixPathArtifactRoot() {
        Path path = new File("unixPathArtifactRoot").toPath();
        ZosFileManagerImpl.setUnixPathArtifactRoot(path);
        Assert.assertEquals("getUnixPathArtifactRoot() should return the supplied value", path, ZosFileManagerImpl.getUnixPathArtifactRoot());
    }
    
    @Test
    public void testNewZosFileHandler() {
        Assert.assertNotNull("newZosFileHandler() should return a new ZosFileHandlerImpl", ZosFileManagerImpl.newZosFileHandler());
    }
    
    @Test
    public void testGetRunDatasetHLQ() throws ZosManagerException {
        Mockito.when(ZosFileManagerImpl.zosManager.getRunDatasetHLQ(Mockito.any())).thenReturn("HLQ");
        
        Assert.assertEquals("getRunDatasetHLQ() should return the supplied value", "HLQ", ZosFileManagerImpl.getRunDatasetHLQ(zosImageMock));
    }
    
    @Test
    public void testGetRunDatasetHLQException() throws ZosManagerException {
        Mockito.when(ZosFileManagerImpl.zosManager.getRunDatasetHLQ(Mockito.any())).thenThrow(new ZosManagerException("exception"));
        
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("exception");
        
        ZosFileManagerImpl.getRunDatasetHLQ(zosImageMock);
    }
    
    class DummyTestClass {
        @dev.galasa.zos.ZosImage
        public dev.galasa.zos.IZosImage zosImage;
        @dev.galasa.zosfile.ZosFileHandler
        public dev.galasa.zosfile.IZosFileHandler zosFileHandler;
        @dev.galasa.Test
        public void dummyTestMethodDataset() throws ZosDatasetException {
            zosFileHandler.newDataset("dsname", zosImage);
        }
        @dev.galasa.Test
        public void dummyTestMethodUNIXFile() throws ZosUNIXFileException {
            zosFileHandler.newUNIXFile("filePath", zosImage);
        }
        @dev.galasa.Test
        public void dummyTestMethodVSAMDataset() throws ZosVSAMDatasetException {
            zosFileHandler.newVSAMDataset("dsname", zosImage);
        }
    }
}
