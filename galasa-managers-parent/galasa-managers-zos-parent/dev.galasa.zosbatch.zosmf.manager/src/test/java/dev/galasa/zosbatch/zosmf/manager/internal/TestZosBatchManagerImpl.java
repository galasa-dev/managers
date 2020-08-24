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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.core.StringStartsWith;
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
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class})
public class TestZosBatchManagerImpl {

	private ZosBatchManagerImpl zosBatchManager; 
    
    private ZosBatchManagerImpl zosBatchManagerSpy;

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
    private ZosmfManagerImpl zosmfManagerMock;

    @Mock
    private IZosImage zosImageMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String JOBNAME_PREFIX = "PFX";    

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
        
        PowerMockito.doReturn(JOBNAME_PREFIX).when(zosManagerMock).getZosBatchPropertyJobnamePrefix(Mockito.any());
        ZosBatchManagerImpl.setZosManager(zosManagerMock);
        ZosBatchManagerImpl.setZosmfManager(zosmfManagerMock);
        
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
        zosBatchManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosBatchManagerImpl.class));
        Assert.assertEquals("Error in initialise() method", zosBatchManagerSpy.getFramework(), frameworkMock);
    }
    
    @Test
    public void testInitialise1() throws ManagerException {
        Mockito.doNothing().when(zosBatchManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        zosBatchManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", zosBatchManagerSpy.getFramework(), frameworkMock);
    }

    @Test
    public void testInitialiseException() throws ManagerException {
        PowerMockito.doThrow(new ManagerException("exception")).when(zosBatchManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        exceptionRule.expect(ManagerException.class);
        exceptionRule.expectMessage("exception");
        zosBatchManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(zosBatchManagerSpy, "generateAnnotatedFields", Mockito.any());
        zosBatchManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(zosBatchManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }
    
    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(zosManagerMock);
        allManagers.add(zosmfManagerMock);
        zosBatchManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosBatchManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosBatchManagerSpy);
        zosBatchManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosBatchManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
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
        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosBatchManagerSpy.areYouProvisionalDependentOn(zosManagerMock));
        Assert.assertTrue("Should be dependent on IZosmfManagerSpi" , zosBatchManagerSpy.areYouProvisionalDependentOn(zosmfManagerMock));
        Assert.assertFalse("Should not be dependent on IManager" , zosBatchManagerSpy.areYouProvisionalDependentOn(managerMock));
    }

    @Test
    public void testProvisionStart() throws Exception {
        Whitebox.setInternalState(zosBatchManagerSpy, "artifactsRoot", new File("/").toPath());
        zosBatchManagerSpy.provisionStart();
        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", "preTest", ZosBatchManagerImpl.currentTestMethodArchiveFolderName);
    }

    @Test
    public void testStartOfTestMethod() throws Exception {
        Whitebox.setInternalState(zosBatchManagerSpy, "artifactsRoot", new File("/").toPath());
        zosBatchManagerSpy.startOfTestMethod(new GalasaMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethod"), null));
        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName(), ZosBatchManagerImpl.currentTestMethodArchiveFolderName);
        zosBatchManagerSpy.startOfTestMethod(new GalasaMethod(DummyTestClass.class.getDeclaredMethod("dummyBeforeMethod"), DummyTestClass.class.getDeclaredMethod("dummyTestMethod")));
        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName() + "." + DummyTestClass.class.getDeclaredMethod("dummyBeforeMethod").getName(), ZosBatchManagerImpl.currentTestMethodArchiveFolderName);
    }
    
    @Test
    public void testEndOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
        Mockito.doNothing().when(zosBatchManagerSpy).cleanup();
        ZosBatchManagerImpl.setCurrentTestMethodArchiveFolderName(DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName());
        zosBatchManagerSpy.endOfTestMethod(new GalasaMethod(null, DummyTestClass.class.getDeclaredMethod("dummyTestMethod")), "pass", null);
        Assert.assertEquals("currentTestMethodArchiveFolderName should be expected value", DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName(), ZosBatchManagerImpl.currentTestMethodArchiveFolderName);
    }
    
    @Test
    public void testEndOfTestClass() throws NoSuchMethodException, SecurityException, ManagerException {
        Whitebox.setInternalState(zosBatchManagerSpy, "artifactsRoot", new File("/").toPath());
        Mockito.doNothing().when(zosBatchManagerSpy).cleanup();
        ZosBatchManagerImpl.setCurrentTestMethodArchiveFolderName(DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName());
        zosBatchManagerSpy.endOfTestClass(null, null);
        Assert.assertEquals("currentTestMethodArchiveFolderName should be expeacted value", "postTest", ZosBatchManagerImpl.currentTestMethodArchiveFolderName);
    }
    
    @Test
    public void testEndOfTestRun() throws NoSuchMethodException, SecurityException, ManagerException {
        Whitebox.setInternalState(zosBatchManagerSpy, "artifactsRoot", new File("/").toPath());
        Mockito.doNothing().when(zosBatchManagerSpy).cleanup();
        ZosBatchManagerImpl.setCurrentTestMethodArchiveFolderName(DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName());
        zosBatchManagerSpy.endOfTestRun();
        Assert.assertEquals("currentTestMethodArchiveFolderName should be expeacted value", "dummyTestMethod", ZosBatchManagerImpl.currentTestMethodArchiveFolderName);

        Mockito.doThrow(new ZosBatchException()).when(zosBatchManagerSpy).cleanup();
        zosBatchManagerSpy.endOfTestRun();
        Assert.assertEquals("testEndOfTestRun() should log expected message", "Problem in endOfTestRun()", logMessage);
    }
    
    @Test
    public void testCleanup() throws Exception {
        zosBatchManagerSpy.cleanup();
        
        HashMap<String, ZosBatchImpl> taggedZosBatches = new HashMap<>();
        ZosBatchImpl zosBatchImpl = Mockito.mock(ZosBatchImpl.class);
        Mockito.doNothing().when(zosBatchImpl).cleanup();
        taggedZosBatches.put("TAG", zosBatchImpl);
        Whitebox.setInternalState(zosBatchManagerSpy, "taggedZosBatches", taggedZosBatches);
        Whitebox.setInternalState(zosBatchManagerSpy, "zosBatches", taggedZosBatches);
        zosBatchManagerSpy.cleanup();
        PowerMockito.verifyPrivate(zosBatchImpl, Mockito.times(2)).invoke("cleanup");        
    }
    
    @Test
    public void testGenerateZosBatch() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
        List<Annotation> annotations = new ArrayList<>();
        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosbatch.ZosBatch.class);
        annotations.add(annotation);
        
        Object zosBatchImplObject = zosBatchManagerSpy.generateZosBatch(DummyTestClass.class.getDeclaredField("zosBatch"), annotations);
        Assert.assertTrue("Error in generateZosBatch() method", zosBatchImplObject instanceof ZosBatchImpl);
        
        HashMap<String, ZosBatchImpl> taggedZosBatches = new HashMap<>();
        ZosBatchImpl zosBatchImpl = Mockito.mock(ZosBatchImpl.class);
        taggedZosBatches.put("tag", zosBatchImpl);
        Whitebox.setInternalState(zosBatchManagerSpy, "taggedZosBatches", taggedZosBatches);
        
        zosBatchImplObject = zosBatchManagerSpy.generateZosBatch(DummyTestClass.class.getDeclaredField("zosBatch"), annotations);
        Assert.assertEquals("generateZosBatch() should retrn the supplied instance of ZosBatchImpl", zosBatchImpl, zosBatchImplObject);
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
    
    @Test
    public void testNewZosBatchJobnameImpl() throws Exception {
        IZosBatchJobname zosBatchJobname = zosBatchManagerSpy.newZosBatchJobnameImpl("image");
        Assert.assertThat("IZosBatchJobname getName() should start with the supplied value", zosBatchJobname.getName(), StringStartsWith.startsWith(JOBNAME_PREFIX));
    }
    
    @Test
    public void testGetZosBatch() {
        IZosBatch zosBatch = zosBatchManagerSpy.getZosBatch(zosImageMock);
        Assert.assertNotNull("getZosBatch() should not be null", zosBatch);
        IZosBatch zosBatch2 = zosBatchManagerSpy.getZosBatch(zosImageMock);
        Assert.assertEquals("getZosBatch() should return the existing IZosBatch instance", zosBatch, zosBatch2);
    }
    
    class DummyTestClass {
        @dev.galasa.zosbatch.ZosBatch(imageTag="tag")
        public dev.galasa.zosbatch.IZosBatch zosBatch;
        @dev.galasa.zosbatch.ZosBatchJobname(imageTag="tag")
        public dev.galasa.zosbatch.IZosBatchJobname zosBatchJobname;
        @dev.galasa.Before
        public void dummyBeforeMethod() throws ZosBatchException {
            zosBatch.submitJob("JCL", null);
        }
        @dev.galasa.Test
        public void dummyTestMethod() throws ZosBatchException {
            zosBatch.submitJob("JCL", null);
        }
    }
}
