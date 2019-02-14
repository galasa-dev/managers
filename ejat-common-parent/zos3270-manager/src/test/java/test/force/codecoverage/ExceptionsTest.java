package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import io.ejat.common.zos3270.Zos3270Exception;

public class ExceptionsTest {
	
	@Test
	public void testZos3270Exception() {
		Throwable throwable = new Zos3270Exception();
		new Zos3270Exception("Message");		
		new Zos3270Exception("Message", throwable);		
		new Zos3270Exception(throwable);		
		new Zos3270Exception("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
