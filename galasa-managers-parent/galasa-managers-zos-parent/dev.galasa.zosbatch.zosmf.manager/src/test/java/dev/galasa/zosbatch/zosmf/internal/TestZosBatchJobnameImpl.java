/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.zosmf.internal.properties.JobnamePrefix;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JobnamePrefix.class})
public class TestZosBatchJobnameImpl {
	
	private static final String FIXED_JOBNAME = "GAL45678";
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	
	@Before
	public void setup() throws ZosBatchManagerException {
		PowerMockito.mockStatic(JobnamePrefix.class);
	}
	
	@Test
	public void testGetJobname() throws ZosBatchManagerException {
		PowerMockito.mockStatic(JobnamePrefix.class);
		when(JobnamePrefix.get(anyString())).thenReturn(FIXED_JOBNAME);
		
		ZosBatchJobnameImpl zosJobname = new ZosBatchJobnameImpl("image");
		assertEquals(FIXED_JOBNAME, zosJobname.getName());
		assertEquals(FIXED_JOBNAME, zosJobname.toString());
	}
	
	@Test
	public void testException() throws ZosBatchManagerException {
		when(JobnamePrefix.get(anyString())).thenThrow(new ZosBatchManagerException("exception"));
		
		exceptionRule.expect(ZosBatchManagerException.class);
	    exceptionRule.expectMessage("Problem getting batch jobname prefix");
	    new ZosBatchJobnameImpl("image");
	}
}
