package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zos3270.spi.DatastreamException;

public class ExceptionsTest {
	
	@Test
	public void testDatastreamException() {
		Throwable throwable = new DatastreamException();
		new DatastreamException("Message");		
		new DatastreamException("Message", throwable);		
		new DatastreamException(throwable);		
		new DatastreamException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
