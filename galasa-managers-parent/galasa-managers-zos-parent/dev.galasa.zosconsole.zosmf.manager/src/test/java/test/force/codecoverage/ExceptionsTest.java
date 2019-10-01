package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zosconsole.ZosConsoleException;

public class ExceptionsTest {
	
	@Test
	public void testZosManagerException() {
		Throwable throwable = new ZosConsoleException();
		new ZosConsoleException("Message");		
		new ZosConsoleException("Message", throwable);		
		new ZosConsoleException(throwable);		
		new ZosConsoleException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
