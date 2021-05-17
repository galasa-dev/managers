/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.cicsts.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private Log                            logger       = LogFactory.getLog(getClass());

    public final ICicsRegionProvisioned cicsRegion;
    public final ICicstsManagerSpi cicstsManager;

    public final boolean connectAtStartup;

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, String host, int port, boolean ssl, boolean connectAtStartup)
            throws TerminalInterruptedException, Zos3270ManagerException {
        super(cicsRegion.getNextTerminalId(), host, port, ssl, framework, false, cicsRegion.getZosImage());

        this.cicsRegion = cicsRegion;
        this.cicstsManager = cicstsManager;
        this.connectAtStartup = connectAtStartup;

        setAutoReconnect(connectAtStartup);
    }

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, IIpHost ipHost, boolean connectAtStartup)
            throws TerminalInterruptedException, IpNetworkManagerException, Zos3270ManagerException {
        this(cicstsManager, framework, cicsRegion, ipHost.getHostname(), ipHost.getTelnetPort(), ipHost.isTelnetPortTls(), connectAtStartup);
    }

    public CicsTerminalImpl(ICicstsManagerSpi cicstsManager, IFramework framework, ICicsRegionProvisioned cicsRegion, boolean connectAtStartup) throws TerminalInterruptedException, IpNetworkManagerException,
    Zos3270ManagerException {
        this(cicstsManager, framework, cicsRegion, cicsRegion.getZosImage().getIpHost(), connectAtStartup);
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
        // Really don't like this code.   Wish we had an alternative.

        logger.trace("Attempting to reset the CICS TS screen");

        try {
            boolean foundNative = false;
            for (int resetCount = 0; resetCount < 20; resetCount++ ) {
                if ( (resetCount % 2) == 0 ) { 
                    clear().wfk().pf3().wfk();
                    if (isTextInField("DFHAC2001") || isTextInField("DFHRT4401")) {
                        foundNative = true;
                        break;
                    }
                }

                clear().wfk().enter().wfk();

                if (isTextInField("DFHAC2001") || isTextInField("DFHRT4401")) {
                    foundNative = true;
                    break;
                }
            }
            if (!foundNative) {
                throw new CicstsManagerException("Unable to locate the native CICS TS screen");
            }
            
            clear().wfk();
        } catch(Exception e) {
            throw new CicstsManagerException("Unable to reset the CICS TS screen", e);
        }

        return this;
    }

    public boolean isConnectAtStartup() {
        return this.connectAtStartup;
    }

}