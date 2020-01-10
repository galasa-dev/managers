/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.internal.properties.JobnamePrefix;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JobnamePrefix.class})
public class TestZosBatchImpl {
	
	private ZosBatchImpl zosBatch;
	
	private ZosBatchImpl zosBatchSpy;

	@Mock
	private IZosImage zosImageMock;

	@Mock
	private ZosBatchJobnameImpl zosJobnameMock;
	
	private static final String FIXED_JOBNAME = "GAL45678";
	
	@Mock
	private ZosBatchJobImpl zosBatchJobMock;
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	
	@Before
	public void setup() throws ZosBatchManagerException {
		when(zosImageMock.getImageID()).thenReturn("image");
		
		zosBatch = new ZosBatchImpl(zosImageMock);
		zosBatchSpy = spy(zosBatch);
		doReturn(zosBatchJobMock).when(zosBatchSpy).newZosBatchJob(anyString(), any());
		
		when(zosBatchJobMock.submitJob()).thenReturn(zosBatchJobMock);
		
		PowerMockito.mockStatic(JobnamePrefix.class);
		when(JobnamePrefix.get(Mockito.anyString())).thenReturn(FIXED_JOBNAME);
	}
	
	@Test
	public void testSubmitJobandCleanup1() throws ZosBatchException {		
		assertEquals(zosBatchSpy.submitJob("JCL", null), zosBatchJobMock);

		when(zosBatchJobMock.submitted()).thenReturn(false);
		zosBatch.cleanup();
		
		assertEquals(zosBatchSpy.submitJob("JCL", null), zosBatchJobMock);

		when(zosBatchJobMock.submitted()).thenReturn(true);
		when(zosBatchJobMock.isArchived()).thenReturn(false);
		when(zosBatchJobMock.isPurged()).thenReturn(false);
		zosBatch.cleanup();		
		
		assertEquals(zosBatchSpy.submitJob("JCL", zosJobnameMock), zosBatchJobMock);

		doNothing().when(zosBatchJobMock).archiveJobOutput();
		doNothing().when(zosBatchJobMock).purgeJob();
		when(zosBatchJobMock.isArchived()).thenReturn(true);
		when(zosBatchJobMock.isPurged()).thenReturn(true);
		zosBatch.cleanup();
	}
}
