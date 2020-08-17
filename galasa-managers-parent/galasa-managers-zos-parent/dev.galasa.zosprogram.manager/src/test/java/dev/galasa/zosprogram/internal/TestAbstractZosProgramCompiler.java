/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
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

import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramException;
import dev.galasa.zosprogram.ZosProgramManagerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosProgramManagerImpl.class})
public class TestAbstractZosProgramCompiler {
    
    private AbstractZosProgramCompiler abstractZosProgramCompiler;
    
    private AbstractZosProgramCompiler abstractZosProgramCompilerSpy;
    
    @Mock
    private IBundleResources bundleResourcesMock;
    
    @Mock
    private IZosBatchSpi zosBatchMock;

    @Mock
    private IZosBatchJob zosBatchJobMock;

    @Mock
    private ZosProgramImpl zosProgramMock;

    @Mock
    private IZosDataset loadlibMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String NAME = "NAME";

    private static final String SKEL = "SKEL";

    private static final String JCL = "JCL";

    private static final String EXCEPTION = "EXCEPTION";

    private static final String JOB_RETCODE = "CC 0000";

    private static final String JOBNAME = "JOBNAME";

    private static final String JOBID = "JOBID";

    private static final Language LANGUAGE = Language.COBOL;

    private static final String LOG_FOR_FIELD = " -++- ";

    @Before
    public void setup() throws Exception {
        Mockito.when(zosProgramMock.getLoadlib()).thenReturn(loadlibMock);
        abstractZosProgramCompiler = new AbstractZosProgramCompiler(zosProgramMock);
        abstractZosProgramCompilerSpy = Mockito.spy(abstractZosProgramCompiler);
    }
    
    @Test
    public void testConstructor() throws ZosProgramManagerException {
        Mockito.when(zosProgramMock.getLoadlib()).thenReturn(null);
        PowerMockito.mockStatic(ZosProgramManagerImpl.class);
        Mockito.when(ZosProgramManagerImpl.getRunLoadlib(Mockito.any())).thenReturn(loadlibMock);
        new AbstractZosProgramCompiler(zosProgramMock);
        
        exceptionRule.expect(ZosProgramManagerException.class);
        exceptionRule.expectMessage("EXCEPTION");
        Mockito.when(ZosProgramManagerImpl.getRunLoadlib(Mockito.any())).thenThrow(new ZosProgramManagerException(EXCEPTION));
        new AbstractZosProgramCompiler(zosProgramMock);
    }
    
    @Test
    public void testCompile() throws ZosProgramException {
        Mockito.doReturn("DUMMY").when(abstractZosProgramCompilerSpy).buildCompileJcl();
        Mockito.doNothing().when(abstractZosProgramCompilerSpy).submitCompileJob(Mockito.any());
        abstractZosProgramCompilerSpy.compile();

        Mockito.doThrow(new ZosProgramException(EXCEPTION)).when(abstractZosProgramCompilerSpy).submitCompileJob(Mockito.any());
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage(EXCEPTION);
        abstractZosProgramCompilerSpy.compile();
    }
    
    @Test
    public void testBuildCompileJcl() throws ZosProgramException, IOException, TestBundleResourceException {
        ZosProgramManagerImpl.setManagerBundleResources(bundleResourcesMock);
        InputStream inputStreamMock = Mockito.mock(InputStream.class);
        Mockito.when(bundleResourcesMock.retrieveSkeletonFile(Mockito.any(), Mockito.any())).thenReturn(inputStreamMock);
        Mockito.when(bundleResourcesMock.streamAsString(Mockito.any())).thenReturn(SKEL);
        
        Assert.assertEquals("Error in buildCompileJcl() method", SKEL, abstractZosProgramCompilerSpy.buildCompileJcl());

        Mockito.when(bundleResourcesMock.streamAsString(Mockito.any())).thenThrow(new IOException());
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage("Problem loading JCL skeleton");
        abstractZosProgramCompilerSpy.buildCompileJcl();
    }
    
    @Test
    public void testSubmitCompileJob() throws ZosBatchException, ZosProgramException {
        setupSubmitCompileJob(false);
        Mockito.doCallRealMethod().when(zosProgramMock).setCompileJob(Mockito.any());
        Mockito.doCallRealMethod().when(zosProgramMock).getCompileJob();
        abstractZosProgramCompilerSpy.submitCompileJob(JCL);
        Assert.assertEquals("Error in submitCompileJob() method", zosBatchJobMock, abstractZosProgramCompilerSpy.zosProgram.getCompileJob());
    }
    
    @Test
    public void testSubmitCompileJobException1() throws ZosBatchException, ZosProgramException {
        setupSubmitCompileJob(true);
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage("Problem submitting compile job for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD);
        abstractZosProgramCompilerSpy.submitCompileJob(JCL);
    }
    
    @Test
    public void testSubmitCompileJobException2() throws ZosProgramException, ZosBatchException {
        setupSubmitCompileJob(false);
        Mockito.doThrow(new ZosBatchException()).when(zosBatchJobMock).waitForJob();
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage("Problem waiting for compile job for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD + ". Jobname=" + JOBNAME + " Jobid=" + JOBID);
        
        abstractZosProgramCompilerSpy.submitCompileJob(JCL);
    }
    
    @Test
    public void testSubmitCompileJobException3() throws ZosProgramException, ZosBatchException {
        setupSubmitCompileJob(false);
        Mockito.doThrow(new ZosBatchException()).when(zosBatchJobMock).purge();
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage("Problem saving compile job output for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD + ". Jobname=" + JOBNAME + " Jobid=" + JOBID);
        abstractZosProgramCompilerSpy.submitCompileJob(JCL);
    }
    
    @Test
    public void testSubmitCompileJobException4() throws ZosProgramException, ZosBatchException {
        setupSubmitCompileJob(false);
        Mockito.when(zosBatchJobMock.waitForJob()).thenReturn(9);
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage("Compile job for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD + " failed: " + JOB_RETCODE + ". Jobname=" + JOBNAME + " Jobid=" + JOBID);
        abstractZosProgramCompilerSpy.submitCompileJob(JCL);
    }
    
    @Test
    public void testSubmitCompileJobException5() throws ZosProgramException, ZosBatchException {
        setupSubmitCompileJob(false);
        Mockito.when(zosBatchJobMock.waitForJob()).thenReturn(-1);
        exceptionRule.expect(ZosProgramException.class);
        exceptionRule.expectMessage("Compile job for " + LANGUAGE + " program " + NAME + LOG_FOR_FIELD + " failed: " + JOB_RETCODE + ". Jobname=" + JOBNAME + " Jobid=" + JOBID);
        abstractZosProgramCompilerSpy.submitCompileJob(JCL);
    }

    private void setupSubmitCompileJob(boolean exception) throws ZosBatchException {
        ZosProgramManagerImpl.setZosBatch(zosBatchMock);
        IZosBatch localZosBatch = Mockito.mock(IZosBatch.class);
        Mockito.when(zosBatchMock.getZosBatch(Mockito.any())).thenReturn(localZosBatch);
        if (exception) {
            Mockito.when(localZosBatch.submitJob(Mockito.any(), Mockito.any())).thenThrow(new ZosBatchException());
        } else {
            Mockito.when(localZosBatch.submitJob(Mockito.any(), Mockito.any())).thenReturn(zosBatchJobMock);
        }
        Mockito.when(zosBatchJobMock.waitForJob()).thenReturn(0);
        Mockito.when(zosBatchJobMock.getRetcode()).thenReturn(JOB_RETCODE);
        IZosBatchJobname zosJobnameMock = Mockito.mock(IZosBatchJobname.class);
        Mockito.when(zosJobnameMock.getName()).thenReturn(JOBNAME);
        Mockito.when(zosBatchJobMock.getJobname()).thenReturn(zosJobnameMock);
        Mockito.when(zosBatchJobMock.getJobId()).thenReturn(JOBID);
        Mockito.when(zosProgramMock.getLanguage()).thenReturn(Language.COBOL);
        Mockito.when(zosProgramMock.getName()).thenReturn(NAME);
        Mockito.when(zosProgramMock.logForField()).thenReturn(LOG_FOR_FIELD);
    }
    
    @Test
    public void testFormatDatasetConcatenation() {
        List<String> datasetList = new LinkedList<>();
        String concatenation = "DUMMY";
        Assert.assertEquals("Error in formatDatasetConcatenation() method", concatenation , abstractZosProgramCompilerSpy.formatDatasetConcatenation(datasetList));
        
        String disp = "DISP=SHR,DSN=";
        datasetList.add("DSN1");
        concatenation = disp + "DSN1";
        Assert.assertEquals("Error in formatDatasetConcatenation() method", concatenation , abstractZosProgramCompilerSpy.formatDatasetConcatenation(datasetList));
        
        datasetList.add("DSN2");
        concatenation = disp + "DSN1\n//         DD " + disp + "DSN2";
        Assert.assertEquals("Error in formatDatasetConcatenation() method", concatenation , abstractZosProgramCompilerSpy.formatDatasetConcatenation(datasetList));
        
        datasetList.clear();
        datasetList.add("ASIS-DSN");
        concatenation = "DSN";
        Assert.assertEquals("Error in formatDatasetConcatenation() method", concatenation , abstractZosProgramCompilerSpy.formatDatasetConcatenation(datasetList));
    }
}
