package test.force.codecoverage;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosbatch.IZosBatchJob;
import dev.voras.common.zosbatch.IZosBatchJobname;
import dev.voras.common.zosbatch.ZosBatchManagerException;

@RunWith(MockitoJUnitRunner.class)
public class ManagerTest {
	
	@Mock
	private IZosBatchJobname jobname;
	
	@Mock
	private IZosImage image;
	
	@Mock
	private IZosBatchJob zosBatchJob;
	
	@Test
	public void testZosManagerException() throws ZosBatchManagerException {
		
		Assert.assertEquals("dummy", 0, zosBatchJob.waitForJob());		
		List<String> expected = Arrays.asList();
		Assert.assertEquals("dummy", expected, zosBatchJob.retrieveOutput());
	}
	
	
}
