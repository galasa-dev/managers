/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.zosmf.manager.internal;

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

import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosfile.zosmf.manager.internal.properties.RestrictZosmfToImage;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosFileManagerImpl.class, RestrictZosmfToImage.class})
public class TestZosVSAMVSAMDatasetImpl {
    
    private ZosVSAMDatasetImpl zosVSAMDataset;
    
    private ZosVSAMDatasetImpl zosVSAMDatasetSpy;

    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private ZosmfManagerImpl zosmfManagerMock;
    
    @Mock
    private ZosFileHandlerImpl zosFileHandlerMock;
    
    @Mock
    private ZosDatasetImpl zosDatasetMock;
    
    @Mock
    private IZosmfRestApiProcessor zosmfApiProcessorMock;
    
    @Mock
    private IZosmfResponse zosmfResponseMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String VSAM_DATASET_NAME = "DATA.SET.NAME";
    
    private static final String IMAGE = "IMAGE";
    
    private static final String IDCAMS_COMMAND = "IDCAMS COMMAND";
    
    private static final String CONTENT = "content";
    
    private static final String EXCEPTION = "exception";
    
    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(ZosFileManagerImpl.class);
        Mockito.when(ZosFileManagerImpl.newZosFileHandler()).thenReturn(zosFileHandlerMock);
        Mockito.when(zosFileHandlerMock.newDataset(Mockito.any(), Mockito.any())).thenReturn(zosDatasetMock);
        
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        
        PowerMockito.mockStatic(RestrictZosmfToImage.class);
        Mockito.when(RestrictZosmfToImage.get(Mockito.any())).thenReturn(true);

        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(zosImageMock, RestrictZosmfToImage.get(zosImageMock.getImageID()))).thenReturn(zosmfApiProcessorMock);
        ZosFileManagerImpl.setZosmfManager(zosmfManagerMock);
        
        zosVSAMDataset = new ZosVSAMDatasetImpl(zosImageMock, VSAM_DATASET_NAME);
        zosVSAMDatasetSpy = Mockito.spy(zosVSAMDataset);
    }
    
    @Test
    public void testConstructorException() throws ZosmfManagerException, ZosFileManagerException {
        Mockito.when(zosFileHandlerMock.newDataset(Mockito.any(), Mockito.any())).thenThrow(new ZosDatasetException(EXCEPTION));
        exceptionRule.expect(ZosVSAMDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        new ZosVSAMDatasetImpl(zosImageMock, VSAM_DATASET_NAME);
    }
    
    @Test
    public void testCreate() throws ZosVSAMDatasetException, ZosmfException {
        PowerMockito.doReturn(false).doReturn(true).doReturn(false).doReturn(true).doReturn(false).doReturn(false).when(zosVSAMDatasetSpy).exists();
        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getDefineCommand();
        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());

        Assert.assertEquals("create() should return the IZosVSAMDataset instance", zosVSAMDatasetSpy, zosVSAMDatasetSpy.create());
        
        Assert.assertEquals("create() should return the IZosVSAMDataset instance", zosVSAMDatasetSpy, zosVSAMDatasetSpy.createRetain());
        
        exceptionRule.expect(ZosVSAMDatasetException.class);
        exceptionRule.expectMessage("VSAM data set \"" + VSAM_DATASET_NAME + "\" not created on image " + IMAGE);
        
        zosVSAMDatasetSpy.create();
    }
    
    @Test
    public void testCreateExists() throws ZosVSAMDatasetException {
        PowerMockito.doReturn(true).when(zosVSAMDatasetSpy).exists();

        exceptionRule.expect(ZosVSAMDatasetException.class);
        exceptionRule.expectMessage("VSAM data set \"" + VSAM_DATASET_NAME + "\" already exists on image " + IMAGE);
        
        zosVSAMDatasetSpy.create();
    }
    
    @Test
    public void testDelete() throws ZosVSAMDatasetException, ZosmfException {
        PowerMockito.doReturn(true).doReturn(false).doReturn(true).doReturn(true).doReturn(false).when(zosVSAMDatasetSpy).exists();
        PowerMockito.doReturn(IDCAMS_COMMAND).when(zosVSAMDatasetSpy).getDeleteCommand();
        PowerMockito.doNothing().when(zosVSAMDatasetSpy).idcamsRequest(Mockito.any());

        Assert.assertTrue("delete() should return true", zosVSAMDatasetSpy.delete());

        Assert.assertFalse("delete() should return false", zosVSAMDatasetSpy.delete());
        
        exceptionRule.expect(ZosVSAMDatasetException.class);
        exceptionRule.expectMessage("VSAM data set \"" + VSAM_DATASET_NAME + "\" does not exist on image " + IMAGE);
        
        zosVSAMDatasetSpy.delete();
    }
    
    @Test
    public void testExists() throws ZosDatasetException, ZosVSAMDatasetException {
        PowerMockito.doReturn(true).when(zosDatasetMock).exists();
        Assert.assertTrue("exists() should return true", zosVSAMDatasetSpy.exists());

        PowerMockito.doThrow(new ZosDatasetException(EXCEPTION)).when(zosDatasetMock).exists();        
        exceptionRule.expect(ZosVSAMDatasetException.class);
        exceptionRule.expectMessage(EXCEPTION);
        zosVSAMDatasetSpy.exists();
    }
}
