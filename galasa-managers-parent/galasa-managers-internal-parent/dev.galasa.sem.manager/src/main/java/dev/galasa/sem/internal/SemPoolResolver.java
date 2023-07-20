/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal;

import com.ibm.hursley.cicsts.test.sem.interfaces.complex.IPool;
import com.ibm.hursley.cicsts.test.sem.interfaces.complex.IPoolResolver;
import com.ibm.hursley.cicsts.test.sem.interfaces.complex.ISymbolic;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.sem.SemManagerException;

public class SemPoolResolver implements IPoolResolver {

    private final SemManagerImpl manager;
    private final IDynamicStatusStoreService dss;
    private final IResourcePoolingService rps;
    
    private final SemPoolApplids poolApplids;
    private final SemPoolPorts   poolPorts;

    protected SemPoolResolver(SemManagerImpl manager) throws SemManagerException {
        this.manager = manager;
        this.dss = this.manager.getDss();
        this.rps = this.manager.getFramework().getResourcePoolingService();

        this.poolApplids = new SemPoolApplids(this.manager, dss, rps);
        this.poolPorts   = new SemPoolPorts(this.manager, dss, rps);
    }

    @Override
    public IPool getPool(String poolname, ISymbolic symbolicResolver, Object modelObject) {
        
        if ("APPLIDS".equalsIgnoreCase(poolname)) {
            return this.poolApplids;
        }
        if ("PORTS".equalsIgnoreCase(poolname)) {
            return this.poolPorts;
        }
        return null;
    }

    public void generateComplete() {
        this.poolApplids.generateComplete();
        this.poolPorts.generateComplete();
    }

    public void discard() {
        this.poolApplids.discard();
        this.poolPorts.discard();
    }

    public SemPoolApplids getApplidPool() {
        return this.poolApplids;
    }

    public SemPoolPorts getPortPool() {
        return this.poolPorts;
    }

}
