/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosrseapi.internal.RseapiManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class})
public class TestRseapiZosFileManagerImpl {

	private RseapiZosFileManagerImpl zosFileManager;
    
    private RseapiZosFileManagerImpl zosFileManagerSpy;

    private List<IManager> allManagers;
    
    private List<IManager> activeManagers;
    
    @Mock
    private Log logMock;
    
    private static String logMessage;
    
    @Mock
    private IFramework frameworkMock;
    
    @Mock
    private IResultArchiveStore resultArchiveStoreMock;
    
    @Mock
    public IManager managerMock;
    
    @Mock
    private ZosManagerImpl zosManagerMock;

    @Mock
    private RseapiManagerImpl rseapiManagerMock;

    @Mock
    private IZosImage zosImageMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final Path ARTIFACT_ROOT = new File(".").toPath();

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
        Answer<String> answer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                logMessage = invocation.getArgument(0);
                System.err.println("Captured Log Message:\n" + logMessage);
                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
                    ((Throwable) invocation.getArgument(1)).printStackTrace();
                }
                return null;
            }
        };
        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
        
        RseapiZosFileManagerImpl.setZosManager(zosManagerMock);
        RseapiZosFileManagerImpl.setRseapiManager(rseapiManagerMock);
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        zosFileManager = new RseapiZosFileManagerImpl();
        zosFileManagerSpy = Mockito.spy(zosFileManager);
        Mockito.when(zosFileManagerSpy.getFramework()).thenReturn(frameworkMock);
        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(ARTIFACT_ROOT);
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        allManagers.add(managerMock);
        zosFileManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestRseapiZosFileManagerImpl.class));
        Assert.assertEquals("Error in initialise() method", zosFileManagerSpy.getFramework(), frameworkMock);
        
        RseapiZosFileManagerImpl.setCurrentTestMethodArchiveFolderName("");
        Assert.assertEquals("Error in getDatasetCurrentTestMethodArchiveFolder() should return the expected value", ARTIFACT_ROOT.resolve("zOS_Datasets"), RseapiZosFileManagerImpl.getDatasetCurrentTestMethodArchiveFolder());
        Assert.assertEquals("Error in getVsamDatasetCurrentTestMethodArchiveFolder() should return the expected value", ARTIFACT_ROOT.resolve("zOS_VSAM_Datasets"), RseapiZosFileManagerImpl.getVsamDatasetCurrentTestMethodArchiveFolder());
        Assert.assertEquals("Error in getUnixPathCurrentTestMethodArchiveFolder() should return the expected value", ARTIFACT_ROOT.resolve("zOS_Unix_Paths"), RseapiZosFileManagerImpl.getUnixPathCurrentTestMethodArchiveFolder());
    }
    
    @Test
    public void testInitialise1() throws ManagerException {
        Mockito.doNothing().when(zosFileManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        zosFileManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", zosFileManagerSpy.getFramework(), frameworkMock);
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
        allManagers.add(rseapiManagerMock);
        zosFileManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosFileManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosFileManagerSpy);        
        zosFileManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosFileManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testYouAreRequiredException1() throws ManagerException {
        allManagers.add(rseapiManagerMock);
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("The zOS Manager is not available");
        zosFileManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testYouAreRequiredException2() throws ManagerException {
        allManagers.add(zosManagerMock);
        allManagers.add(zosManagerMock);
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("The RSE API Manager is not available");
        zosFileManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testAreYouProvisionalDependentOn() {
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosFileManagerSpy.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertTrue("Should be dependent on IRseapiManagerSpi" , zosFileManagerSpy.areYouProvisionalDependentOn(rseapiManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , zosFileManagerSpy.areYouProvisionalDependentOn(managerMock));
    }

    @Test
    public void testProvisionBuild() throws Exception {
        Whitebox.setInternalState(zosFileManagerSpy, "artifactsRoot", new File(".").toPath());
        zosFileManagerSpy.provisionBuild();
        Assert.assertEquals("datasetArtifactRoot should contain the supplied value", new File(".").toPath().resolve("provisioning").resolve("zOS_Datasets"), RseapiZosFileManagerImpl.datasetArtifactRoot);
        Assert.assertEquals("vsamDatasetArtifactRoot should contain the supplied value", new File(".").toPath().resolve("provisioning").resolve("zOS_VSAM_Datasets"), RseapiZosFileManagerImpl.vsamDatasetArtifactRoot);
        Assert.assertEquals("unixPathArtifactRoot should contain the supplied value", new File(".").toPath().resolve("provisioning").resolve("zOS_Unix_Paths"), RseapiZosFileManagerImpl.unixPathArtifactRoot);
        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", "preTest", RseapiZosFileManagerImpl.currentTestMethodArchiveFolderName);
    }
    
    @Test
    public void testStartOfTestClass() throws ManagerException {
        Mockito.doNothing().when(zosFileManagerSpy).cleanup(Mockito.anyBoolean());
        zosFileManagerSpy.startOfTestClass();
        Mockito.verify(zosFileManagerSpy, Mockito.times(1)).cleanup(Mockito.anyBoolean());
    }
    
    @Test
    public void testStartOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
        Mockito.doNothing().when(zosFileManagerSpy).cleanup(Mockito.anyBoolean());
        Whitebox.setInternalState(zosFileManagerSpy, "artifactsRoot", new File(".").toPath());
        zosFileManagerSpy.startOfTestMethod(new GalasaMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset"), null));
        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", "dummyTestMethodDataset", RseapiZosFileManagerImpl.currentTestMethodArchiveFolderName);
        
        zosFileManagerSpy.startOfTestMethod(new GalasaMethod(DummyTestClass.class.getDeclaredMethod("dummyBeforeMethod"), DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset")));
        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset").getName() + "." + DummyTestClass.class.getDeclaredMethod("dummyBeforeMethod").getName(), RseapiZosFileManagerImpl.currentTestMethodArchiveFolderName);
    }
    
    @Test
    public void testEndOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
        Mockito.doNothing().when(zosFileManagerSpy).cleanup(Mockito.anyBoolean());
        RseapiZosFileManagerImpl.setCurrentTestMethodArchiveFolderName("dummyTestMethodDataset");
        zosFileManagerSpy.endOfTestMethod(new GalasaMethod(null, DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset")), "pass", null);
        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset").getName(), RseapiZosFileManagerImpl.currentTestMethodArchiveFolderName);
    }
    
    @Test
    public void testEndOfTestClass() throws Exception {
        Whitebox.setInternalState(zosFileManagerSpy, "artifactsRoot", new File(".").toPath());
        Whitebox.setInternalState(RseapiZosFileManagerImpl.class, "zosFileHandlers", new HashMap<>());
        zosFileManagerSpy.endOfTestClass("pass", null);
        Assert.assertEquals("currentTestMethodArchiveFolderName should be should be set to the expected value", "postTest", RseapiZosFileManagerImpl.currentTestMethodArchiveFolderName);
    }
    
    @Test
    public void testCleanup() throws Exception {
        zosFileManagerSpy.cleanup(false);
        zosFileManagerSpy.cleanup(true);

        Map<String, RseapiZosFileHandlerImpl> zosFileHandlers = new HashMap<>();
        RseapiZosFileHandlerImpl rseapiZosFileHandlerImpl = Mockito.mock(RseapiZosFileHandlerImpl.class);
        Mockito.doNothing().when(rseapiZosFileHandlerImpl).cleanup(Mockito.anyBoolean());
        zosFileHandlers.put(rseapiZosFileHandlerImpl.toString(), rseapiZosFileHandlerImpl);
        Whitebox.setInternalState(RseapiZosFileManagerImpl.class, "zosFileHandlers", zosFileHandlers);
        
        zosFileManagerSpy.cleanup(false);
        Mockito.verify(rseapiZosFileHandlerImpl, Mockito.times(1)).cleanup(Mockito.anyBoolean());
        
        Mockito.clearInvocations(zosFileManagerSpy);
        zosFileHandlers = new HashMap<>();
        Mockito.doNothing().when(zosFileManagerSpy).cleanup(Mockito.anyBoolean());
        zosFileHandlers.put(rseapiZosFileHandlerImpl.toString(), rseapiZosFileHandlerImpl);
        Whitebox.setInternalState(RseapiZosFileManagerImpl.class, "zosFileHandlers", zosFileHandlers);
        
        zosFileManagerSpy.cleanup(true);
        Mockito.verify(rseapiZosFileHandlerImpl, Mockito.times(1)).cleanup(Mockito.anyBoolean());
    }
    
    @Test
    public void testProvisionDiscard() throws ZosFileManagerException {
        Mockito.doNothing().when(zosFileManagerSpy).cleanup(Mockito.anyBoolean());
        zosFileManagerSpy.provisionDiscard();

        Mockito.doThrow(new ZosFileManagerException()).when(zosFileManagerSpy).cleanup(Mockito.anyBoolean());  
        zosFileManagerSpy.provisionDiscard();
        Assert.assertEquals("provisionDiscard() should log expected message", "Problem in provisionDiscard()", logMessage);
    }
    
    @Test
    public void testEndOfTestRun() throws NoSuchMethodException, SecurityException, ManagerException {
        Whitebox.setInternalState(zosFileManagerSpy, "artifactsRoot", new File(".").toPath());
        Mockito.doNothing().when(zosFileManagerSpy).cleanup(Mockito.anyBoolean());
        RseapiZosFileManagerImpl.setCurrentTestMethodArchiveFolderName(DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset").getName());
        zosFileManagerSpy.endOfTestRun();
        Assert.assertEquals("currentTestMethodArchiveFolderName should be expeacted value", DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset").getName(), RseapiZosFileManagerImpl.currentTestMethodArchiveFolderName);

        Mockito.doThrow(new ZosFileManagerException()).when(zosFileManagerSpy).cleanup(Mockito.anyBoolean());
        zosFileManagerSpy.endOfTestRun();
        Assert.assertEquals("testEndOfTestRun() should log expected message", "Problem in endOfTestRun()", logMessage);
    }
    
    @Test
    public void testGenerateZosFileHandler() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosfile.ZosFileHandler.class);
        annotations.add(annotation);
        
        Object zosFileHandlerObject = zosFileManagerSpy.generateZosFileHandler(DummyTestClass.class.getDeclaredField("zosFileHandler"), annotations);
        Assert.assertTrue("Error in generateZosFileHandler() method", zosFileHandlerObject instanceof RseapiZosFileHandlerImpl);
    }
    
    @Test
    public void testRunid() {
        RseapiZosFileManagerImpl.setRunId("RUNID");
        Assert.assertEquals("getRunid() should return the supplied value", "RUNID", RseapiZosFileManagerImpl.getRunId());
    }
    
    @Test
    public void testDatasetArtifactRoot() {
        Path path = new File("datasetArtifactRoot").toPath();
        RseapiZosFileManagerImpl.setDatasetArtifactRoot(path);
        Assert.assertEquals("getDatasetArtifactRoot() should return the supplied value", path, RseapiZosFileManagerImpl.getDatasetArtifactRoot());
    }
    
    @Test
    public void testVsamDatasetArtifactRoot() {
        Path path = new File("vsamDatasetArtifactRoot").toPath();
        RseapiZosFileManagerImpl.setVsamDatasetArtifactRoot(path);
        Assert.assertEquals("getVsamDatasetArtifactRoot() should return the supplied value", path, RseapiZosFileManagerImpl.getVsamDatasetArtifactRoot());
    }
    
    @Test
    public void testUnixPathArtifactRoot() {
        Path path = new File("unixPathArtifactRoot").toPath();
        RseapiZosFileManagerImpl.setUnixPathArtifactRoot(path);
        Assert.assertEquals("getUnixPathArtifactRoot() should return the supplied value", path, RseapiZosFileManagerImpl.getUnixPathArtifactRoot());
    }
    
    @Test
    public void testNewZosFileHandler() {
        Assert.assertNotNull("newZosFileHandler() should return a new RseapiZosFileHandlerImpl", RseapiZosFileManagerImpl.newZosFileHandler());
    }
    
    @Test
    public void testGetRunDatasetHLQ() throws ZosManagerException {
        Mockito.when(RseapiZosFileManagerImpl.zosManager.getRunDatasetHLQ(Mockito.any())).thenReturn("HLQ");
        
        Assert.assertEquals("getRunDatasetHLQ() should return the supplied value", "HLQ", RseapiZosFileManagerImpl.getRunDatasetHLQ(zosImageMock));
    }
    
    @Test
    public void testGetRunDatasetHLQException() throws ZosManagerException {
        Mockito.when(RseapiZosFileManagerImpl.zosManager.getRunDatasetHLQ(Mockito.any())).thenThrow(new ZosManagerException("exception"));
        
        exceptionRule.expect(ZosFileManagerException.class);
        exceptionRule.expectMessage("exception");
        
        RseapiZosFileManagerImpl.getRunDatasetHLQ(zosImageMock);
    }
    
    @Test
    public void testGetZosFileHandler() throws ZosFileManagerException {
        Assert.assertEquals("getZosFileHandler() should return the same IZosFileHandler", zosFileManagerSpy.getZosFileHandler(), zosFileManagerSpy.getZosFileHandler());
    }
    
    class DummyTestClass {
        @dev.galasa.zos.ZosImage
        public dev.galasa.zos.IZosImage zosImage;
        @dev.galasa.zosfile.ZosFileHandler
        public dev.galasa.zosfile.IZosFileHandler zosFileHandler;
        @dev.galasa.Before
        public void dummyBeforeMethod() throws ZosDatasetException {
            zosFileHandler.newDataset("dsname", zosImage);
        }
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
