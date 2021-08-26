/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zosliberty.angel.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zosliberty.angel.IZosLibertyAngel;
import dev.galasa.zosliberty.angel.ZosLibertyAngelManagerException;

public class ZosLibertyAngelImpl implements IZosLibertyAngel {
    private static final Log logger = LogFactory.getLog(ZosLibertyAngelImpl.class);

    private ZosLibertyAngelManagerImpl zosLibertyAngelManager;

    public ZosLibertyAngelImpl(ZosLibertyAngelManagerImpl zosLibertyManager) {
        this.zosLibertyAngelManager = zosLibertyManager;
    }

    protected ZosLibertyAngelManagerImpl getZosLibertyManager() throws ZosLibertyAngelManagerException {
        return this.zosLibertyAngelManager;
    }
    
    protected void cleanup() {
    	// stop
    	// archive job
    	// cancel job
    }
}
