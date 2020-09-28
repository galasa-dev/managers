/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zos.internal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.utils.DssUtils;
import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.internal.IpNetworkManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl.ImageUsage;
import dev.galasa.zos.internal.properties.BatchExtraBundle;
import dev.galasa.zos.internal.properties.ClusterIdForTag;
import dev.galasa.zos.internal.properties.ClusterImages;
import dev.galasa.zos.internal.properties.ConsoleExtraBundle;
import dev.galasa.zos.internal.properties.DseClusterIdForTag;
import dev.galasa.zos.internal.properties.DseImageIdForTag;
import dev.galasa.zos.internal.properties.FileExtraBundle;
import dev.galasa.zos.internal.properties.ImageIdForTag;
import dev.galasa.zos.internal.properties.ImageMaxSlots;
import dev.galasa.zos.internal.properties.RunDatasetHLQ;
import dev.galasa.zos.internal.properties.TSOCommandExtraBundle;
import dev.galasa.zos.internal.properties.UNIXCommandExtraBundle;
import dev.galasa.zos.internal.properties.ZosPropertiesSingleton;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.internal.properties.JobWaitTimeout;
import dev.galasa.zosbatch.internal.properties.JobnamePrefix;
import dev.galasa.zosbatch.internal.properties.BatchRestrictToImage;
import dev.galasa.zosbatch.internal.properties.TruncateJCLRecords;
import dev.galasa.zosbatch.internal.properties.UseSysaff;
import dev.galasa.zosbatch.internal.properties.ZosBatchPropertiesSingleton;
import dev.galasa.zosfile.internal.properties.DirectoryListMaxItems;
import dev.galasa.zosfile.internal.properties.FileRestrictToImage;
import dev.galasa.zosfile.internal.properties.UnixFilePermissions;
import dev.galasa.zosfile.internal.properties.ZosFilePropertiesSingleton;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class, BatchExtraBundle.class, ConsoleExtraBundle.class, FileExtraBundle.class, TSOCommandExtraBundle.class, UNIXCommandExtraBundle.class, 
                 DseImageIdForTag.class, ImageIdForTag.class, DseClusterIdForTag.class, AbstractManager.class, ImageMaxSlots.class, DssUtils.class, 
                 ClusterIdForTag.class, ClusterImages.class, RunDatasetHLQ.class, BatchRestrictToImage.class, UseSysaff.class, JobWaitTimeout.class, TruncateJCLRecords.class, 
                 JobnamePrefix.class, DirectoryListMaxItems.class, FileRestrictToImage.class, UnixFilePermissions.class})
public class TestZosManagerImpl {

    private ZosManagerImpl zosManager;
    
    private ZosManagerImpl zosManagerSpy;

    private List<IManager> allManagers;
    
    private List<IManager> activeManagers;
    
    @Mock
    private IFramework frameworkMock;
    
    @Mock
    private IResultArchiveStore resultArchiveStoreMock;

    @Mock
    private IConfigurationPropertyStoreService cpsMock;
    
    @Mock
    private IDynamicStatusStoreService dssMock;

    @Mock
    private IDynamicResource dynamicResourceMock;
    
    @Mock
    public IManager managerMock;
    
    @Mock
    public IpNetworkManagerImpl ipNetworkManagerMock;

    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private Log logMock;

    private ZosPropertiesSingleton zosPropertiesSingleton;

    private ZosBatchPropertiesSingleton zosBatchPropertiesSingleton;

    private ZosFilePropertiesSingleton zosFilePropertiesSingleton;
    
    private static String logMessage;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String BATCH_BUNDLE = "batch.bundle";

    private static final String CONSOLE_BUNDLE = "console.bundle";

    private static final String FILE_BUNDLE = "file.bundle";

    private static final String TSO_COMMAND_BUNDLE = "tso.command.bundle";

    private static final String UNIX_COMMAND_BUNDLE = "unix.command.bundle";
    
    private static final String IMAGE_ID = "image";
    
    private static final String IMAGE_ID_1 = "image1";
    
    private static final String IMAGE_ID_2 = "image2";

    private static final String CLUSTER_ID = "cluster";

    private static final String PLEX_ID = "sysplex";

    private static final String DEFAULT_CREDENTIALS_ID = "credentials";

    private static final String IPV4_HOSTNAME = "ipv4.hostname";

    private static final String IPV6_HOSTNAME = "ipv6.hostname";

    private static final String TAG = "TAG";

    private static final String SLOT_NAME = "SLOT-NAME";

    private static final String RUN_HLQ = "RUNHLQ";

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
        logMessage = null;
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
        Mockito.doAnswer(answer).when(logMock).info(Mockito.any());
        
        zosPropertiesSingleton = new ZosPropertiesSingleton();
        zosPropertiesSingleton.activate();
        zosBatchPropertiesSingleton = new ZosBatchPropertiesSingleton();
        zosBatchPropertiesSingleton.activate();
        zosFilePropertiesSingleton = new ZosFilePropertiesSingleton();
        zosFilePropertiesSingleton.activate(); 
        
        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
        
        zosManager = new ZosManagerImpl();
        zosManagerSpy = Mockito.spy(zosManager);
        Mockito.when(zosManagerSpy.getFramework()).thenReturn(frameworkMock);
        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
        
        allManagers = new ArrayList<>();
        activeManagers = new ArrayList<>();
    }
    
    @Test
    public void testExtraBundles() throws Exception {
        PowerMockito.mockStatic(BatchExtraBundle.class);
        PowerMockito.doReturn(BATCH_BUNDLE).when(BatchExtraBundle.class, "get");
        PowerMockito.mockStatic(ConsoleExtraBundle.class);
        PowerMockito.doReturn(CONSOLE_BUNDLE).when(ConsoleExtraBundle.class, "get");
        PowerMockito.mockStatic(FileExtraBundle.class);
        PowerMockito.doReturn(FILE_BUNDLE).when(FileExtraBundle.class, "get");
        PowerMockito.mockStatic(TSOCommandExtraBundle.class);
        PowerMockito.doReturn(TSO_COMMAND_BUNDLE).when(TSOCommandExtraBundle.class, "get");
        PowerMockito.mockStatic(UNIXCommandExtraBundle.class);
        PowerMockito.doReturn(UNIX_COMMAND_BUNDLE).when(UNIXCommandExtraBundle.class, "get");
        
        ArrayList<String> bundles = new ArrayList<>();
        bundles.add(BATCH_BUNDLE);
        bundles.add(CONSOLE_BUNDLE);
        bundles.add(FILE_BUNDLE);
        bundles.add(TSO_COMMAND_BUNDLE);
        bundles.add(UNIX_COMMAND_BUNDLE);
        
        Assert.assertTrue("extraBundles() should return the expected value", bundles.equals(zosManagerSpy.extraBundles(frameworkMock)));
    }
    
    @Test
    public void testExtraBundlesExceptions() throws Exception {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        
        zosManagerSpy.extraBundles(frameworkMock);
    }
    
    @Test
    public void testInitialise() throws ManagerException {
        Mockito.doNothing().when(zosManagerSpy).youAreRequired(Mockito.any(), Mockito.any());
        zosManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
        Assert.assertEquals("Error in initialise() method", frameworkMock, zosManagerSpy.getFramework());
        
        GalasaTest galasaTestMock = Mockito.mock(GalasaTest.class);
        Mockito.when(galasaTestMock.isJava()).thenReturn(false);
        zosManager.initialise(frameworkMock, allManagers, activeManagers, galasaTestMock);
        Assert.assertEquals("Error in initialise() method", frameworkMock, zosManagerSpy.getFramework());
    }

    @Test
    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to request framework services");
        zosManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosManagerImpl.class));
    }
    
    @Test
    public void testYouAreRequired() throws Exception {
        allManagers.add(ipNetworkManagerMock);
        Mockito.clearInvocations(zosManagerSpy);        
        zosManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosManagerSpy, Mockito.times(1)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
        
        Mockito.clearInvocations(zosManagerSpy);
        zosManagerSpy.youAreRequired(allManagers, activeManagers);
        PowerMockito.verifyPrivate(zosManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any());
    }
    
    @Test
    public void testYouAreRequiredException() throws ManagerException {
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("The IP Network Manager is not available");
        zosManagerSpy.youAreRequired(allManagers, activeManagers);
    }
    
    @Test
    public void testProvisionGenerate() throws Exception {
        PowerMockito.doNothing().when(zosManagerSpy, "generateAnnotatedFields", Mockito.any());
        PowerMockito.doReturn(zosImageMock).when(zosManagerSpy).generateZosImage((Field)Mockito.any());
        PowerMockito.doReturn(zosImageMock).when(zosManagerSpy).generateZosImage((String)Mockito.any());
        PowerMockito.doNothing().when(zosManagerSpy, "registerAnnotatedField", Mockito.any(), Mockito.any());
        PowerMockito.when(zosManagerSpy, "findProvisionDependentAnnotatedFieldTags", Mockito.any(), Mockito.any()).thenReturn(new  HashSet<String>());
        Whitebox.setInternalState(zosManagerSpy, "testClass", (Object) DummyTestClass.class);

        HashMap<Field, Object> annotatedFields = new HashMap<>();
        annotatedFields.put(DummyTestClass.class.getField("zosImage1"), zosImageMock);
        Whitebox.setInternalState(zosManagerSpy, "annotatedFields", annotatedFields);        

        zosManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(zosManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());

        Mockito.clearInvocations(zosManagerSpy);
        Whitebox.setInternalState(zosManagerSpy, "testClass", (Object) DummyTestClass1.class);
        zosManagerSpy.provisionGenerate();
        PowerMockito.verifyPrivate(zosManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
    }
    
    @Test
    public void testProvisionDiscard() throws Exception {
        HashMap<String, ZosBaseImageImpl> images = new HashMap<>();
        ZosDseImageImpl zosDseImage = Mockito.mock(ZosDseImageImpl.class);
        ZosBaseImageImpl zosProvisionedImageMock = Mockito.mock(ZosProvisionedImageImpl.class);
        images.put(IMAGE_ID_1, (ZosBaseImageImpl) zosDseImage);
        images.put(IMAGE_ID_2, (ZosBaseImageImpl) zosProvisionedImageMock);
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        zosManagerSpy.provisionDiscard();
        PowerMockito.verifyPrivate(zosProvisionedImageMock, Mockito.times(1)).invoke("freeImage");
    }
    
    @Test
    public void testGenerateZosImage() throws Exception {
        PowerMockito.mockStatic(DseImageIdForTag.class);
        PowerMockito.doReturn(null).when(DseImageIdForTag.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(ImageIdForTag.class);
        PowerMockito.doReturn(null).when(ImageIdForTag.class, "get", Mockito.anyString());
        ZosProvisionedImageImpl zosProvisionedImageMock = Mockito.mock(ZosProvisionedImageImpl.class);
        PowerMockito.doReturn(zosProvisionedImageMock).when(zosManagerSpy).selectNewImage(Mockito.any());        
        Field field = DummyTestClass.class.getField("zosImage");
        
        Assert.assertEquals("generateZosImage() should return the expected value", zosProvisionedImageMock, zosManagerSpy.generateZosImage(field));

        setupZosImage();
        PowerMockito.mockStatic(DseClusterIdForTag.class);
        PowerMockito.doReturn(CLUSTER_ID).when(DseClusterIdForTag.class, "get", Mockito.anyString());        
        PowerMockito.doReturn(IMAGE_ID).when(DseImageIdForTag.class, "get", Mockito.anyString());
        
        IZosImage returnedImage = zosManagerSpy.generateZosImage(field);
        Assert.assertTrue("generateZosImage() should return the expected value", returnedImage instanceof ZosDseImageImpl);
        
        Assert.assertEquals("generateZosImage() should return the expected value", returnedImage, zosManagerSpy.generateZosImage(field));
        
        HashMap<String, ZosBaseImageImpl> images = new HashMap<>();
        images.put(IMAGE_ID, (ZosBaseImageImpl) returnedImage);
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        HashMap<String, ZosBaseImageImpl> taggedImages = new HashMap<>();
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
        Assert.assertEquals("generateZosImage() should return the expected value", returnedImage, zosManagerSpy.generateZosImage(field));
        
        images.clear();
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        taggedImages.clear();
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
        PowerMockito.doReturn(null).when(DseImageIdForTag.class, "get", Mockito.anyString());
        PowerMockito.doReturn(IMAGE_ID).when(ImageIdForTag.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(ImageMaxSlots.class);
        PowerMockito.doReturn(99).when(ImageMaxSlots.class, "get", Mockito.anyString());
        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains(".current.slots"), Mockito.any(), Mockito.anyString())).thenReturn(true);
        PowerMockito.when(dssMock.putSwap(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        returnedImage = zosManagerSpy.generateZosImage(field);
        Assert.assertTrue("generateZosImage() should return the expected value", returnedImage instanceof ZosProvisionedImageImpl);
        
        taggedImages.clear();
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
        Assert.assertEquals("generateZosImage() should return the expected value", returnedImage, zosManagerSpy.generateZosImage(field));
        
        images.clear();
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        taggedImages.clear();
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
        PowerMockito.doReturn(0).when(ImageMaxSlots.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(DssUtils.class);
        
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to provision zOS Image tagged " + TAG + " on " + IMAGE_ID + " as there is insufficient capacity");
        zosManagerSpy.generateZosImage(field);
    }
    
    private void setupZosImage() throws Exception {
        PowerMockito.doReturn(cpsMock).when(zosManagerSpy).getCPS();
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(PLEX_ID))).thenReturn(PLEX_ID);
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(DEFAULT_CREDENTIALS_ID), Mockito.anyString())).thenReturn(DEFAULT_CREDENTIALS_ID);
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV4_HOSTNAME))).thenReturn(IPV4_HOSTNAME);
        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV6_HOSTNAME))).thenReturn(IPV6_HOSTNAME);
        PowerMockito.doReturn(dssMock).when(zosManagerSpy).getDSS();
        PowerMockito.when(dssMock.getDynamicResource(Mockito.any())).thenReturn(dynamicResourceMock);
        
        PowerMockito.mockStatic(AbstractManager.class);
        PowerMockito.doReturn(PLEX_ID).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(PLEX_ID));
        PowerMockito.doReturn(DEFAULT_CREDENTIALS_ID).when(AbstractManager.class, "defaultString", ArgumentMatchers.contains(DEFAULT_CREDENTIALS_ID), Mockito.anyString());
        PowerMockito.doReturn(IPV4_HOSTNAME).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(IPV4_HOSTNAME));
        PowerMockito.doReturn(IPV6_HOSTNAME).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(IPV6_HOSTNAME));
        PowerMockito.doReturn(TAG).when(AbstractManager.class, "defaultString", ArgumentMatchers.contains(TAG), Mockito.anyString());
    }

    @Test
    public void testGenerateIpHost() throws NoSuchFieldException, SecurityException, ZosManagerException {
        Field field = DummyTestClass.class.getField("zosHost");
        List<Annotation> annotations = Arrays.asList(DummyTestClass.class.getAnnotations());
        ZosBaseImageImpl zosBaseImageMock = Mockito.mock(ZosBaseImageImpl.class);
        ZosIpHostImpl zosIpHostMock = Mockito.mock(ZosIpHostImpl.class);
        PowerMockito.when(zosBaseImageMock.getIpHost()).thenReturn(zosIpHostMock);
        HashMap<String, ZosBaseImageImpl> taggedImages = new HashMap<>();
        taggedImages.put(TAG, zosBaseImageMock);
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
        Assert.assertEquals("generateIpHost() should return the expected value", zosIpHostMock, zosManagerSpy.generateIpHost(field, annotations));

        taggedImages.clear();
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);

        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to provision an IP Host for field " + field.getName() + " as no @ZosImage for the tag '" + TAG + "' was present");
        zosManagerSpy.generateIpHost(field, annotations);
    }
    
    @Test
    public void testGenerateIpPort() throws NoSuchFieldException, SecurityException, ZosManagerException, IpNetworkManagerException {
        Field field = DummyTestClass.class.getField("zosPort");
        List<Annotation> annotations = Arrays.asList(DummyTestClass.class.getAnnotations());
        ZosBaseImageImpl zosBaseImageMock = Mockito.mock(ZosBaseImageImpl.class);
        PowerMockito.when(zosBaseImageMock.getImageID()).thenReturn(IMAGE_ID);
        ZosIpHostImpl zosIpHostMock = Mockito.mock(ZosIpHostImpl.class);
        PowerMockito.when(zosBaseImageMock.getIpHost()).thenReturn(zosIpHostMock);
        IIpPort ipPortMock = Mockito.mock(IIpPort.class);
        PowerMockito.when(zosIpHostMock.provisionPort(Mockito.anyString())).thenReturn(ipPortMock);
        HashMap<String, ZosBaseImageImpl> taggedImages = new HashMap<>();
        taggedImages.put(TAG, zosBaseImageMock);
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
        Assert.assertEquals("generateIpPort() should return the expected value", ipPortMock, zosManagerSpy.generateIpPort(field, annotations));
        
        PowerMockito.when(zosIpHostMock.provisionPort(Mockito.anyString())).thenThrow(new IpNetworkManagerException());
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to provision a port for zOS Image " + IMAGE_ID + ", type=standard");
        zosManagerSpy.generateIpPort(field, annotations);
    }
    
    @Test
    public void testGenerateIpPortException() throws NoSuchFieldException, SecurityException, ZosManagerException {
        Field field = DummyTestClass.class.getField("zosPort");
        List<Annotation> annotations = Arrays.asList(DummyTestClass.class.getAnnotations());
        
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to provision an IP Host for field " + field.getName() + " as no @ZosImage for the tag '" + TAG + "' was present");
        zosManagerSpy.generateIpPort(field, annotations);
    }
    
    @Test
    public void testSelectNewImage() throws Exception {
        setupZosImage();    
        PowerMockito.mockStatic(ClusterIdForTag.class);
        PowerMockito.doReturn(null).when(ClusterIdForTag.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(ClusterImages.class);
        List<String> clusters = Arrays.asList(new String[] {IMAGE_ID});
        PowerMockito.doReturn(clusters).when(ClusterImages.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(ImageMaxSlots.class);
        PowerMockito.doReturn(-1).when(ImageMaxSlots.class, "get", Mockito.anyString());   
        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn("-2");  
        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains(".current.slots"), Mockito.any(), Mockito.anyString())).thenReturn(true);
        PowerMockito.when(dssMock.putSwap(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        
        ZosProvisionedImageImpl returnedImage = zosManagerSpy.selectNewImage(TAG);
        Assert.assertTrue("selectNewImage() should return the expected value", returnedImage instanceof ZosProvisionedImageImpl);        
        Assert.assertEquals("selectNewImage() should return the expected value", IMAGE_ID, returnedImage.getImageID());

        clusters = Arrays.asList(new String[] {IMAGE_ID_1});
        PowerMockito.doReturn(clusters).when(ClusterImages.class, "get", Mockito.anyString());
        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn("0");
        PowerMockito.mockStatic(DssUtils.class);
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Insufficent capacity for images in cluster DEFAULT");
        zosManagerSpy.selectNewImage(TAG);
    }
    
    @Test
    public void testSelectNewImageException1() throws Exception {        
        PowerMockito.mockStatic(ClusterIdForTag.class);
        PowerMockito.doReturn(CLUSTER_ID).when(ClusterIdForTag.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(ClusterImages.class);
        List<String> clusters = Arrays.asList(new String[] {});
        PowerMockito.doReturn(clusters).when(ClusterImages.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(DssUtils.class);
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Insufficent capacity for images in cluster " + CLUSTER_ID.toUpperCase());
        zosManagerSpy.selectNewImage(TAG);
    }
    
    @Test
    public void testSelectNewImageException2() throws Exception {
        setupZosImage();    
        PowerMockito.mockStatic(ClusterIdForTag.class);
        PowerMockito.doReturn(CLUSTER_ID).when(ClusterIdForTag.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(ClusterImages.class);
        List<String> clusters = Arrays.asList(new String[] {IMAGE_ID});
        PowerMockito.doReturn(clusters).when(ClusterImages.class, "get", Mockito.anyString());
        PowerMockito.mockStatic(ImageMaxSlots.class);
        PowerMockito.doReturn(-1).when(ImageMaxSlots.class, "get", Mockito.anyString());         
        PowerMockito.mockStatic(DssUtils.class);
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Insufficent capacity for images in cluster " + CLUSTER_ID.toUpperCase());
        zosManagerSpy.selectNewImage(TAG);
    }
    
    @Test
    public void testGetDSS() {
        Whitebox.setInternalState(zosManagerSpy, "dss", dssMock);
        Assert.assertEquals("getDSS() should return the expected value", dssMock, zosManagerSpy.getDSS());        
    }
    
    @Test
    public void testGetCPS() {
        Whitebox.setInternalState(zosManagerSpy, "cps", cpsMock);
        Assert.assertEquals("getCPS() should return the expected value", cpsMock, zosManagerSpy.getCPS());        
    }
    
    @Test
    public void testImageUsage() throws Exception {
        ZosProvisionedImageImpl zosProvisionedImageMock = Mockito.mock(ZosProvisionedImageImpl.class);
        Mockito.when(zosProvisionedImageMock.getImageID()).thenReturn(IMAGE_ID);
        ZosManagerImpl.ImageUsage imageUsage = new ZosManagerImpl.ImageUsage(zosProvisionedImageMock);
        ZosManagerImpl.ImageUsage imageUsage1 = new ZosManagerImpl.ImageUsage(zosProvisionedImageMock);
        
        Assert.assertEquals("ImageUsage.compareTo() should return the expected value", 0, imageUsage.compareTo(imageUsage1));
        
        Assert.assertTrue("ImageUsage.equals() should return true", imageUsage.equals(imageUsage1));
        
        Assert.assertTrue("ImageUsage.equals() should return true", imageUsage.equals(imageUsage1));
        
        Assert.assertFalse("ImageUsage.equals() should false", imageUsage.equals(null));
        
        Assert.assertFalse("ImageUsage.equals() should false", imageUsage.equals((Object) new String()));

        Mockito.doReturn(99.9f).when(zosProvisionedImageMock).getCurrentUsage();
        Assert.assertFalse("ImageUsage.equals() should false", imageUsage.equals(new ZosManagerImpl.ImageUsage(zosProvisionedImageMock)));

        Assert.assertEquals("ImageUsage.hashCode() should return the correct value", imageUsage.hashCode(), imageUsage1.hashCode());

        Assert.assertEquals("ImageUsage.toString() should return the correct value", IMAGE_ID, imageUsage.toString());
    }
    
    @Test
    public void testGetIpManager() {
        Whitebox.setInternalState(zosManagerSpy, "ipManager", ipNetworkManagerMock);
        Assert.assertEquals("getIpManager() should return the expected value", ipNetworkManagerMock, zosManagerSpy.getIpManager());        
    }
    
    @Test
    public void testProvisionImageForTag() throws ZosManagerException {
    	PowerMockito.doReturn(zosImageMock).when(zosManagerSpy).generateZosImage(Mockito.any(String.class));
    	Assert.assertEquals("provisionImageForTag() should return the expected value", zosImageMock, zosManagerSpy.provisionImageForTag(TAG));

    	HashMap<String, ZosBaseImageImpl> taggedImages = new HashMap<>();
        ZosBaseImageImpl zosBaseImageMock = Mockito.mock(ZosBaseImageImpl.class);
        taggedImages.put(TAG, zosBaseImageMock);
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
    	Assert.assertEquals("provisionImageForTag() should return the expected value", zosBaseImageMock, zosManagerSpy.provisionImageForTag(TAG));
    }
    
    @Test
    public void testGetImageForTag() throws ZosManagerException {
        HashMap<String, ZosBaseImageImpl> taggedImages = new HashMap<>();
        ZosBaseImageImpl zosBaseImageMock = Mockito.mock(ZosBaseImageImpl.class);
        taggedImages.put(TAG, zosBaseImageMock);
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
        Assert.assertEquals("getImageForTag() should return the expected value", zosBaseImageMock, zosManagerSpy.getImageForTag(TAG));
        
        taggedImages.clear();
        Whitebox.setInternalState(zosManagerSpy, "taggedImages", taggedImages);
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("Unable to locate zOS Image for tag " + TAG + "1");
        zosManagerSpy.getImageForTag(TAG + "1");
    }
    
    @Test
    public void testGetImage() throws ZosManagerException {
        HashMap<String, ZosBaseImageImpl> images = new HashMap<>();
        ZosBaseImageImpl zosBaseImageMock = Mockito.mock(ZosBaseImageImpl.class);
        Mockito.when(zosBaseImageMock.getImageID()).thenReturn(IMAGE_ID);
        images.put(IMAGE_ID, zosBaseImageMock);
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        Assert.assertEquals("getImage() should return the expected value", zosBaseImageMock, zosManagerSpy.getImage(IMAGE_ID));
        Assert.assertEquals("getImage() should log specified message", "zOS Image " + IMAGE_ID + " selected", logMessage);
        
        ZosProvisionedImageImpl zosProvisionedImageMock = Mockito.mock(ZosProvisionedImageImpl.class);
        Mockito.when(zosProvisionedImageMock.getImageID()).thenReturn(IMAGE_ID);
        Mockito.when(zosProvisionedImageMock.getSlotName()).thenReturn(SLOT_NAME);
        Mockito.doReturn(99.9f).when(zosProvisionedImageMock).getCurrentUsage();
        Mockito.when(zosProvisionedImageMock.allocateImage()).thenReturn(false);
        images.clear();
        images.put(IMAGE_ID, zosProvisionedImageMock);
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        Assert.assertEquals("getImage() should return the expected value", zosProvisionedImageMock, zosManagerSpy.getImage(IMAGE_ID));
        Assert.assertEquals("getImage() should log specified message", "zOS Image " + IMAGE_ID + " selected with slot name "+ SLOT_NAME, logMessage);

        images.clear();
        images.put(IMAGE_ID, zosProvisionedImageMock);
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        ZosProvisionedImageImpl zosProvisionedImageMock1 = Mockito.mock(ZosProvisionedImageImpl.class);
        Mockito.when(zosProvisionedImageMock1.getImageID()).thenReturn(IMAGE_ID_1);
        Mockito.when(zosProvisionedImageMock1.getSlotName()).thenReturn(SLOT_NAME);
        Mockito.doReturn(99.9f).when(zosProvisionedImageMock1).getCurrentUsage();
        Mockito.when(zosProvisionedImageMock1.allocateImage()).thenReturn(true);
        ZosProvisionedImageImpl zosProvisionedImageMock2 = Mockito.mock(ZosProvisionedImageImpl.class);
        Mockito.when(zosProvisionedImageMock2.getImageID()).thenReturn(IMAGE_ID_2);
        Mockito.when(zosProvisionedImageMock2.getSlotName()).thenReturn(SLOT_NAME);
        Mockito.doReturn(99.9f).when(zosProvisionedImageMock2).getCurrentUsage();
        Mockito.when(zosProvisionedImageMock2.allocateImage()).thenReturn(true);
        ArrayList<ImageUsage> definedImages = new ArrayList<>();
        definedImages.add(new ImageUsage(zosProvisionedImageMock));
        definedImages.add(new ImageUsage(zosProvisionedImageMock1));
        definedImages.add(new ImageUsage(zosProvisionedImageMock2));
        Whitebox.setInternalState(zosManagerSpy, "definedImages", definedImages);
        Assert.assertEquals("getImage() should return the expected value", zosProvisionedImageMock1, zosManagerSpy.getImage(IMAGE_ID_1));
        Assert.assertEquals("getImage() should log specified message", "zOS Image " + IMAGE_ID_1 + " selected with slot name "+ SLOT_NAME, logMessage);
        
        images.clear();
        images.put(IMAGE_ID, zosProvisionedImageMock);
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        definedImages.clear();
        definedImages.add(new ImageUsage(zosProvisionedImageMock2));
        definedImages.add(new ImageUsage(zosProvisionedImageMock1));
        Whitebox.setInternalState(zosManagerSpy, "definedImages", definedImages);
        Assert.assertEquals("getImage() should return the expected value", zosProvisionedImageMock1, zosManagerSpy.getImage(IMAGE_ID_1));
        Assert.assertEquals("getImage() should log specified message", "zOS Image " + IMAGE_ID_1 + " selected with slot name "+ SLOT_NAME, logMessage);
    }
    
    @Test
    public void testGetImageException1() throws ZosManagerException {
        HashMap<String, ZosBaseImageImpl> images = new HashMap<>();
        ArrayList<ImageUsage> definedImages = new ArrayList<>();
        ZosProvisionedImageImpl zosProvisionedImageMock = Mockito.mock(ZosProvisionedImageImpl.class);
        images.put(IMAGE_ID_1, zosProvisionedImageMock);
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        Whitebox.setInternalState(zosManagerSpy, "definedImages", definedImages);
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("zOS image \"" + IMAGE_ID + "\" not defined");
        zosManagerSpy.getImage(IMAGE_ID);
    }
    
    @Test
    public void testGetImageException2() throws ZosManagerException {
        HashMap<String, ZosBaseImageImpl> images = new HashMap<>();
        ArrayList<ImageUsage> definedImages = new ArrayList<>();
        ZosProvisionedImageImpl zosProvisionedImageMock = Mockito.mock(ZosProvisionedImageImpl.class);
        Mockito.when(zosProvisionedImageMock.getImageID()).thenReturn(IMAGE_ID);
        Mockito.when(zosProvisionedImageMock.getSlotName()).thenReturn(SLOT_NAME);
        Mockito.doReturn(99.9f).when(zosProvisionedImageMock).getCurrentUsage();
        Mockito.when(zosProvisionedImageMock.allocateImage()).thenReturn(false);
        Mockito.when(zosProvisionedImageMock.allocateImage()).thenReturn(false);
        images.put(IMAGE_ID_1, zosProvisionedImageMock);
        definedImages.add(new ImageUsage(zosProvisionedImageMock));
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        Whitebox.setInternalState(zosManagerSpy, "definedImages", definedImages);
        exceptionRule.expect(ZosManagerException.class);
        exceptionRule.expectMessage("zOS image \"" + IMAGE_ID + "\" not defined");
        zosManagerSpy.getImage(IMAGE_ID);
    }
    
    @Test
    public void testGetUnmanagedImage() throws Exception {
        HashMap<String, ZosBaseImageImpl> images = new HashMap<>();
        ZosBaseImageImpl zosBaseImageMock = Mockito.mock(ZosBaseImageImpl.class);
        Mockito.when(zosBaseImageMock.getImageID()).thenReturn(IMAGE_ID);
        images.put(IMAGE_ID, zosBaseImageMock);
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        Assert.assertEquals("getUnmanagedImage() should return the expected value", zosBaseImageMock, zosManagerSpy.getUnmanagedImage(IMAGE_ID));
        
        images.clear();
        Whitebox.setInternalState(zosManagerSpy, "images", images);
        setupZosImage();
        PowerMockito.mockStatic(DseClusterIdForTag.class);
        PowerMockito.doReturn(CLUSTER_ID).when(DseClusterIdForTag.class, "get", Mockito.anyString());        
        Assert.assertTrue("getUnmanagedImage() should return the expected value",  zosManagerSpy.getUnmanagedImage(IMAGE_ID) instanceof ZosDseImageImpl);
    }
    
    @Test
    public void testGetRunDatasetHLQ() throws Exception {
        PowerMockito.mockStatic(RunDatasetHLQ.class);
        PowerMockito.doReturn(RUN_HLQ).when(RunDatasetHLQ.class, "get", Mockito.any());
        Assert.assertEquals("RunDatasetHLQ() should return the expected value", RUN_HLQ, zosManagerSpy.getRunDatasetHLQ(zosImageMock));        
    }
    
    @Test
    public void testGetZosBatchPropertyBatchRestrictToImage() throws Exception {
        PowerMockito.mockStatic(BatchRestrictToImage.class);
        PowerMockito.doReturn(true).when(BatchRestrictToImage.class, "get", Mockito.any());
        Assert.assertTrue("BatchRestrictToImage() should return the expected value", zosManagerSpy.getZosBatchPropertyBatchRestrictToImage(IMAGE_ID));        
    }
    
    @Test
    public void testGetZosBatchPropertyUseSysaff() throws Exception {
        PowerMockito.mockStatic(UseSysaff.class);
        PowerMockito.doReturn(true).when(UseSysaff.class, "get", Mockito.any());
        Assert.assertTrue("getZosBatchPropertyUseSysaff() should return the expected value", zosManagerSpy.getZosBatchPropertyUseSysaff(IMAGE_ID));        
    }
    
    @Test
    public void testGetZosBatchPropertyJobWaitTimeout() throws Exception {
        PowerMockito.mockStatic(JobWaitTimeout.class);
        PowerMockito.doReturn(99).when(JobWaitTimeout.class, "get", Mockito.any());
        Assert.assertEquals("getZosBatchPropertyJobWaitTimeout() should return the expected value", 99, zosManagerSpy.getZosBatchPropertyJobWaitTimeout(IMAGE_ID));        
    }
    
    @Test
    public void testGetZosBatchPropertyTruncateJCLRecords() throws Exception {
        PowerMockito.mockStatic(TruncateJCLRecords.class);
        PowerMockito.doReturn(true).when(TruncateJCLRecords.class, "get", Mockito.any());
        Assert.assertEquals("getZosBatchPropertyTruncateJCLRecords() should return the expected value", true, zosManagerSpy.getZosBatchPropertyTruncateJCLRecords(IMAGE_ID));        
    }
    
    @Test
    public void testNewZosBatchJobnameImage() throws Exception {
    	String prefix = "PFX";
        PowerMockito.mockStatic(JobnamePrefix.class);
        PowerMockito.doReturn(prefix).when(JobnamePrefix.class, "get", Mockito.any());
    	Assert.assertTrue("newZosBatchJobname() should return the expected value", zosManagerSpy.newZosBatchJobname(zosImageMock).getName().startsWith(prefix));
    }
    
    @Test
    public void testNewZosBatchJobnameString() throws ZosBatchException {
    	String jobname = "jobname";
		Assert.assertEquals("newZosBatchJobname() should return the expected value", jobname.toUpperCase(), zosManagerSpy.newZosBatchJobname(jobname).getName());
    }
    
    @Test
    public void testNewZosBatchJobOutput() throws ZosBatchException {
    	String jobname = "JOBNAME";
    	Assert.assertEquals("newZosBatchJobOutput() should return the expected value", jobname, zosManagerSpy.newZosBatchJobOutput(jobname, "jobid").getJobname());
    }

    @Test
    public void testGetZosFilePropertyDirectoryListMaxItems() throws Exception {
        PowerMockito.mockStatic(DirectoryListMaxItems.class);
        PowerMockito.doReturn(99).when(DirectoryListMaxItems.class, "get", Mockito.any());
        Assert.assertEquals("DirectoryListMaxItems() should return the expected value", 99, zosManagerSpy.getZosFilePropertyDirectoryListMaxItems(IMAGE_ID));        
    }

    @Test
    public void testGetZosFilePropertyFileRestrictToImage() throws Exception {
        PowerMockito.mockStatic(FileRestrictToImage.class);
        PowerMockito.doReturn(true).when(FileRestrictToImage.class, "get", Mockito.any());
        Assert.assertTrue("FileRestrictToImage() should return the expected value", zosManagerSpy.getZosFilePropertyFileRestrictToImage(IMAGE_ID));        
    }

    @Test
    public void testGetZosFilePropertyUnixFilePermissions() throws Exception {
        PowerMockito.mockStatic(UnixFilePermissions.class);
        PowerMockito.doReturn("---------").when(UnixFilePermissions.class, "get", Mockito.any());
        Assert.assertEquals("UnixFilePermissions() should return the expected value", "---------", zosManagerSpy.getZosFilePropertyUnixFilePermissions(IMAGE_ID));        
    }
    
    @Test 
    public void testStoreArtifact() throws ZosManagerException, IOException {
    	Path archivePathMock = newMockedPath(true);
    	zosManagerSpy.storeArtifact(archivePathMock, "content", ResultArchiveStoreContentType.TEXT);
    	Mockito.verify(archivePathMock, Mockito.times(2)).getFileSystem();
    }
    
    @Test
    public void testBuildUniquePathName() throws IOException {
    	Path archivePathMock = newMockedPath(false);
    	Answer<Path> resolveAnswer = new Answer<Path>() {
    		@Override
    		public Path answer(InvocationOnMock invocation) throws Throwable {
    			String path = invocation.getArgument(0);
    			if ("XX_1".equals(path) ||
            		"XX_2".equals(path) ||
            		"YY".equals(path)) {
    				return newMockedPath(true);
    			}
    			return newMockedPath(false);
    		}
    	};
    	Mockito.when(archivePathMock.resolve(Mockito.anyString())).thenAnswer(resolveAnswer);
    	Assert.assertEquals("newZosBatchJobname() should return the expected value", "XX", zosManagerSpy.buildUniquePathName(archivePathMock, "XX"));
    	Assert.assertEquals("newZosBatchJobname() should return the expected value", "XX_3", zosManagerSpy.buildUniquePathName(archivePathMock, "XX_2"));
    	Assert.assertEquals("newZosBatchJobname() should return the expected value", "YY_1", zosManagerSpy.buildUniquePathName(archivePathMock, "YY"));
    }
    
    private Path newMockedPath(boolean fileExists) throws IOException {
        Path pathMock = Mockito.mock(Path.class);
        FileSystem fileSystemMock = Mockito.mock(FileSystem.class);
        FileSystemProvider fileSystemProviderMock = Mockito.mock(FileSystemProvider.class);
        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);
        Mockito.when(pathMock.resolve(Mockito.anyString())).thenReturn(pathMock);        
        Mockito.when(pathMock.getFileSystem()).thenReturn(fileSystemMock);
        Mockito.when(fileSystemMock.provider()).thenReturn(fileSystemProviderMock);
        SeekableByteChannel seekableByteChannelMock = Mockito.mock(SeekableByteChannel.class);
        Mockito.when(fileSystemProviderMock.newByteChannel(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(seekableByteChannelMock);
        Mockito.when(fileSystemProviderMock.newOutputStream(Mockito.any(Path.class), Mockito.any())).thenReturn(outputStreamMock);
        if (!fileExists) {
            Mockito.doThrow(new IOException()).when(fileSystemProviderMock).checkAccess(Mockito.any(), Mockito.any());
        }
        return pathMock;
    }
    class DummyTestClass {
        @dev.galasa.zos.ZosImage(imageTag="TAG")
        public dev.galasa.zos.IZosImage zosImage;
        @dev.galasa.zos.ZosImage(imageTag="TAG")
        public dev.galasa.zos.IZosImage zosImage1;
        @dev.galasa.zos.ZosIpHost(imageTag="TAG")
        public dev.galasa.ipnetwork.IIpHost zosHost;
        @dev.galasa.zos.ZosIpPort(imageTag="TAG")
        public dev.galasa.ipnetwork.IIpPort zosPort;
        @dev.galasa.Test
        public void dummyTestMethod() {
            zosImage.getImageID();
        }
    }
    
    class DummyTestClass1 {
        @dev.galasa.zos.ZosImage
        public dev.galasa.zos.IZosImage zosImage;
        @dev.galasa.Test
        public void dummyTestMethod() {
            zosImage.getImageID();
        }
    }
}
