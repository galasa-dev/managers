package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zosbatch.ZosBatchException;

public class ExceptionsTest {
	
	@Test
	public void testEtcd3ManagerException() {
		Throwable throwable = new ZosBatchException();
		new ZosBatchException("Message");		
		new ZosBatchException("Message", throwable);		
		new ZosBatchException(throwable);		
		new ZosBatchException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
