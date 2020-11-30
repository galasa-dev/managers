/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.spi;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class CicsTerminalImpl extends Zos3270TerminalImpl implements ICicsTerminal {

    public final ICicsRegionProvisioned cicsRegion;
    public final ICicstsManagerSpi cicstsManager;

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, String host, int port, boolean ssl)
            throws TerminalInterruptedException, Zos3270ManagerException {
        super(cicsRegion.getNextTerminalId(), host, port, ssl, framework, false);

        this.cicsRegion = cicsRegion;
        this.cicstsManager = cicstsManager;
        
        setAutoReconnect(true);
    }

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, IIpHost ipHost)
            throws TerminalInterruptedException, IpNetworkManagerException, Zos3270ManagerException {
        this(cicstsManager, framework, cicsRegion, ipHost.getHostname(), ipHost.getTelnetPort(), ipHost.isTelnetPortTls());
    }

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion) throws TerminalInterruptedException, IpNetworkManagerException,
            Zos3270ManagerException {
        this(cicstsManager, framework, cicsRegion, cicsRegion.getZosImage().getIpHost());
    }

    @Override
    public ICicsRegion getCicsRegion() {
        return this.cicsRegion;
    }

    @Override
    public boolean connectToCicsRegion() throws CicstsManagerException {
        try {
            for(ICicsRegionLogonProvider logonProvider : this.cicstsManager.getLogonProviders()) {
                if (logonProvider.logonToCicsRegion(this)) {
                    return true;
                }
            }
        } catch(Exception e) {
            throw new CicstsManagerException("Failed to connect terminal",e);
        }
        
        return false;
    }

    @Override
    public ICicsTerminal resetAndClear() throws CicstsManagerException {
        throw new UnsupportedOperationException("PLACEHOLDER");
    }
    
}