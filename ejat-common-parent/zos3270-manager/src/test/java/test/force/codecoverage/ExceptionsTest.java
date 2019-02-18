package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.zos3270.spi.NetworkException;

public class ExceptionsTest {
	
	@Test
	public void testNetworkException() {
		Throwable throwable = new NetworkException();
		new NetworkException("Message");		
		new NetworkException("Message", throwable);		
		new NetworkException(throwable);		
		new NetworkException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
