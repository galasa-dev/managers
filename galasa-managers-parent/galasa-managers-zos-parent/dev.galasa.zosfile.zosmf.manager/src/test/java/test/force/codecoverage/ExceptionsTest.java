/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package test.force.codecoverage;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zosfile.ZosFileManagerException;

public class ExceptionsTest {
	
	@Test
	public void testZosManagerException() {
		Throwable throwable = new ZosFileManagerException();
		new ZosFileManagerException("Message");		
		new ZosFileManagerException("Message", throwable);		
		new ZosFileManagerException(throwable);		
		new ZosFileManagerException("Message", throwable, false, false);		
		Assert.assertTrue("dummy",true);
	}
	
	
}
