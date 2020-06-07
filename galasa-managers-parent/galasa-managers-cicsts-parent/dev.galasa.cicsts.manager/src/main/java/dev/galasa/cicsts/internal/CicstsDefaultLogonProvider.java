package dev.galasa.cicsts.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.internal.properties.DefaultLogonGmText;
import dev.galasa.cicsts.internal.properties.DefaultLogonInitialText;
import dev.galasa.cicsts.spi.ICicsRegionLogonProvider;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.Zos3270Exception;

public class CicstsDefaultLogonProvider implements ICicsRegionLogonProvider {

    private final static Log logger = LogFactory.getLog(CicstsDefaultLogonProvider.class);

    private final String initialText;
    private final String gmText;

    public CicstsDefaultLogonProvider() throws CicstsManagerException {

        try {
            initialText = DefaultLogonInitialText.get();
            gmText = DefaultLogonGmText.get();
        } catch (CicstsManagerException e) {
            throw new CicstsManagerException("Problem retrieving logon text for the default logon provider", e);
        }
    }

    @Override
    public boolean logonToCicsRegion(ICicsTerminal cicsTerminal) throws CicstsManagerException {

        try {
            cicsTerminal.connect();

            // Ensure we can type something first
            cicsTerminal.waitForKeyboard();

            // Check we are at the right screen
            if (initialText != null) {
                try {
                    cicsTerminal.verifyTextInField(initialText);
                } catch (TextNotFoundException e) {
                    throw new CicstsManagerException(
                            "Unable to logon to CICS, initial screen does not contain '" + initialText + "'");
                }
            }

            cicsTerminal.type("LOGON APPLID(" + cicsTerminal.getCicsRegion().getApplid() + ")").enter();

            try {
                cicsTerminal.waitForTextInField(gmText);
            } catch (Exception e) {
                throw new CicstsManagerException(
                        "Unable to wait for the initial CICS screen, looking for '" + gmText + "'", e);
            }

            cicsTerminal.clear();

            logger.debug("Logged onto " + cicsTerminal.getCicsRegion());
        } catch (Zos3270Exception e) {
            throw new CicstsManagerException("Problem logging onto the CICS region");
        }

        return false;
    }

}