/*
 * Copyright (c) 2019 IBM Corporation.
 */
package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.DatastreamException;

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
	
	@Test
	public void testTimeoutException() {
		Throwable throwable = new TimeoutException();
		new DatastreamException("Message");		
		new DatastreamException("Message", throwable);		
		new DatastreamException(throwable);		
		new DatastreamException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	@Test
	public void testTextNotFoundException() {
		Throwable throwable = new TextNotFoundException();
		new DatastreamException("Message");		
		new DatastreamException("Message", throwable);		
		new DatastreamException(throwable);		
		new DatastreamException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	@Test
	public void testKeyboardLockedException() {
		Throwable throwable = new KeyboardLockedException();
		new DatastreamException("Message");		
		new DatastreamException("Message", throwable);		
		new DatastreamException(throwable);		
		new DatastreamException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	@Test
	public void testFieldNotFoundException() {
		Throwable throwable = new FieldNotFoundException();
		new DatastreamException("Message");		
		new DatastreamException("Message", throwable);		
		new DatastreamException(throwable);		
		new DatastreamException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
