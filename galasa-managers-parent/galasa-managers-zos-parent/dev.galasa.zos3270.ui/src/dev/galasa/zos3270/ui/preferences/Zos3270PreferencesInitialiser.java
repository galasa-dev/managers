/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import dev.galasa.zos3270.ui.Zos3270Activator;

public class Zos3270PreferencesInitialiser  extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        
        IPreferenceStore store = Zos3270Activator.getDefault().getPreferenceStore();
        
        store.setDefault(PreferenceConstants.P_BACKGROUND_COLOUR, "0, 0, 0");
        store.setDefault(PreferenceConstants.P_NORMAL_COLOUR, "0, 204, 0");
        store.setDefault(PreferenceConstants.P_INTENSE_COLOUR, "255, 255, 255");
        store.setDefault(PreferenceConstants.P_LOG_CONSOLE, true);
        store.setDefault(PreferenceConstants.P_LIVE_TERMINALS, true);
    }

}
