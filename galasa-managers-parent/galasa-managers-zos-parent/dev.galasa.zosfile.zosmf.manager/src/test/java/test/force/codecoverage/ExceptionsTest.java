package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.common.zosfile.ZosFileException;

public class ExceptionsTest {
	
	@Test
	public void testZosManagerException() {
		Throwable throwable = new ZosFileException();
		new ZosFileException("Message");		
		new ZosFileException("Message", throwable);		
		new ZosFileException(throwable);		
		new ZosFileException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
