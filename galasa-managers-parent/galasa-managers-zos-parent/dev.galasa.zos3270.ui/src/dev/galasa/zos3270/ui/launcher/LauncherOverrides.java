/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.launcher;

import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;

import dev.galasa.eclipse.launcher.ILauncherOverridesExtension;
import dev.galasa.zos3270.ui.Zos3270Activator;
import dev.galasa.zos3270.ui.preferences.PreferenceConstants;

public class LauncherOverrides implements ILauncherOverridesExtension {

    @Override
    public void appendOverrides(ILaunchConfiguration configuration, Properties generatedOverrides) {
        IPreferenceStore preferenceStore = Zos3270Activator.getDefault().getPreferenceStore();
        boolean liveTerminals = preferenceStore.getBoolean(PreferenceConstants.P_LIVE_TERMINALS);
        boolean logConsole = preferenceStore.getBoolean(PreferenceConstants.P_LOG_CONSOLE);

        try {
            int configLiveTerminals = configuration.getAttribute(LauncherConfiguration.LIVE_TERMINAL, 0);
            int configLogConsole = configuration.getAttribute(LauncherConfiguration.LOG_CONSOLE, 0);

            switch (configLiveTerminals) {
                case 1:
                    liveTerminals = true;
                    break;
                case 2:
                    liveTerminals = false;
                    break;
            }

            switch (configLogConsole) {
                case 1:
                    logConsole = true;
                    break;
                case 2:
                    logConsole = false;
                    break;
            }

        } catch (Exception e) {
            Zos3270Activator.log(e);
        }

        if (liveTerminals) {
            try {
                URL liveTerminalUrl = Zos3270Activator.getDefault().getLiveTerminalURL();
                if (liveTerminalUrl != null) {
                    generatedOverrides.setProperty("zos3270.live.terminal.images", liveTerminalUrl.toString());
                }
            } catch (Exception e) {
                Zos3270Activator.log(e);
            }

            Path liveTerminalsPath = Zos3270Activator.getDefault().getLiveTerminalsPath();
            if (liveTerminalsPath != null) {

            }
        }

        if (!logConsole) {
            generatedOverrides.setProperty("zos3270.console.terminal.images", "false");
        }
    }

}
