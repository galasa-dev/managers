package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.ejat.zos.IZosImage;
import io.ejat.zosbatch.IBatchJob;
import io.ejat.zosbatch.IJobname;
import io.ejat.zosbatch.ZosBatchException;
import io.ejat.zosbatch.zosmf.internal.ZosBatchZosmfManager;

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
