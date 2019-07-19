package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.voras.common.zosbatch.zosmf.internal.ZosBatchImpl;
import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosbatch.IZosBatchJob;
import dev.voras.common.zosbatch.IZosBatchJobname;
import dev.voras.common.zosbatch.ZosBatchException;

@RunWith(MockitoJUnitRunner.class)
public class ManagerTest {
	
	@Mock
	private IZosBatchJobname jobname;
	
	@Mock
	private IZosImage image;
	
	@Test
	public void testZosManagerException() throws ZosBatchException {
		ZosBatchImpl zosBatch = new ZosBatchImpl();
		
		IZosBatchJob zosBatchJob = zosBatch.submitJob("Boo", jobname, image);
		
		Assert.assertEquals("dummy", 0, zosBatchJob.waitForJob());
		Assert.assertEquals("dummy", null, zosBatchJob.retrieveOutput());
	}
	
	
}
