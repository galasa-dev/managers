/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.http.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StandAloneHttpClientImpl extends HttpClientImpl {
	
    protected static final Log  logger = LogFactory.getLog(StandAloneHttpClientImpl.class);


	public StandAloneHttpClientImpl(int timeout) {
		super(timeout);
	}

}
