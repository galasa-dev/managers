package dev.galasa.framework.ivt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.Test;

/**
 * Basic test with Test failing method
 */
public class BasicTestFail {
	
	private static Log logger = LogFactory.getLog(BasicTestFail.class);
	
	/**
	 * {@literal @}Test method that fails
	 */
	@Test
	public void testMethodFail() {
		logger.info("A failing test method");
		throw new RuntimeException("This test will always fail"); //NOSONAR
	}
}
