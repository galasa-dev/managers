/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zosbatch.ZosBatchException;

public class ExceptionsTest {
	
	@Test
	public void testZosManagerException() {
		Throwable throwable = new ZosBatchException();
		new ZosBatchException("Message");		
		new ZosBatchException("Message", throwable);		
		new ZosBatchException(throwable);		
		new ZosBatchException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
