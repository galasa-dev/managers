/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import dev.galasa.zos3270.ui.Zos3270Activator;

public class Zos3270Preferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor liveTerminals;
    private BooleanFieldEditor logConsole;
    private ColorFieldEditor backgroundColour;
    private ColorFieldEditor normalColour;
    private ColorFieldEditor intenseColour;

    public Zos3270Preferences() {
        super(GRID);

        IPreferenceStore store = Zos3270Activator.getDefault().getPreferenceStore();

        setPreferenceStore(store);
        setDescription("Galasa z/OS 3270 Preferences");

        store.addPropertyChangeListener(this);
    }

    @Override
    public void init(IWorkbench arg0) {
    }

    @Override
    protected void createFieldEditors() {
        liveTerminals = new BooleanFieldEditor(PreferenceConstants.P_LIVE_TERMINALS, "Use live terminal views", getFieldEditorParent());
        logConsole = new BooleanFieldEditor(PreferenceConstants.P_LOG_CONSOLE, "Log terminal images to console", getFieldEditorParent());
        backgroundColour = new ColorFieldEditor(PreferenceConstants.P_BACKGROUND_COLOUR, "Background Colour", getFieldEditorParent());
        normalColour = new ColorFieldEditor(PreferenceConstants.P_NORMAL_COLOUR, "Normal Text Colour", getFieldEditorParent());
        intenseColour = new ColorFieldEditor(PreferenceConstants.P_INTENSE_COLOUR, "Intense Text Colour", getFieldEditorParent());

        addField(liveTerminals);
        addField(logConsole);
        addField(backgroundColour);
        addField(normalColour);
        addField(intenseColour);
    }

}
