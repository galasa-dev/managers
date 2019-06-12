package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dev.voras.common.zosbatch.zosmf.internal.ZosBatchZosmfManager;
import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosbatch.IBatchJob;
import dev.voras.common.zosbatch.IJobname;
import dev.voras.common.zosbatch.ZosBatchException;

@RunWith(MockitoJUnitRunner.class)
public class ManagerTest {
	
	@Mock
	private IJobname jobname;
	
	@Mock
	private IZosImage image;
	
	@Test
	public void testZosManagerException() throws ZosBatchException {
		ZosBatchZosmfManager batchManager = new ZosBatchZosmfManager();
		
		IBatchJob batchJob = batchManager.submitJob("Boo", jobname, image);
		
		Assert.assertEquals("dummy", 0, batchJob.waitForJob());
		Assert.assertEquals("dummy", null, batchJob.retrieveOutput());
	}
	
	
}
