/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.launcher;


import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import dev.galasa.eclipse.launcher.tabs.IConfigurationGroups;
import dev.galasa.eclipse.launcher.tabs.IConfigurationTab;
import dev.galasa.zos3270.ui.Zos3270Activator;

public class LauncherConfiguration implements IConfigurationGroups, SelectionListener {

    public static final String LIVE_TERMINAL       = "dev.galasa.zos3270.ui.launcher.LauncherConfiguration.live.terminal";
    public static final String LOG_CONSOLE         = "dev.galasa.zos3270.ui.launcher.LauncherConfiguration.log.console";

    private Button buttonLiveDefault;
    private Button buttonLiveYes;
    private Button buttonLiveNo;

    private Button buttonConsoleDefault;
    private Button buttonConsoleYes;
    private Button buttonConsoleNo;
    
    private IConfigurationTab tab;

    @Override
    public void createControl(IConfigurationTab tab, Composite parent) {
        this.tab = tab;
        
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;

        GridLayout topLayout = new GridLayout(3, false);

        Group zos3270Live = new Group(parent, SWT.NONE);
        zos3270Live.setText("z/OS 3270 - Show live terminals");
        zos3270Live.setLayoutData(gd);
        zos3270Live.setLayout(topLayout);

        buttonLiveDefault = new Button(zos3270Live, SWT.RADIO);
        buttonLiveDefault.setText("Default");
        buttonLiveDefault.addSelectionListener(this);
        buttonLiveYes = new Button(zos3270Live, SWT.RADIO);
        buttonLiveYes.setText("Yes");
        buttonLiveYes.addSelectionListener(this);
        buttonLiveNo = new Button(zos3270Live, SWT.RADIO);
        buttonLiveNo.setText("No");
        buttonLiveNo.addSelectionListener(this);
        
        Group zos3270Console = new Group(parent, SWT.NONE);
        zos3270Console.setText("z/OS 3270 - Show terminal images in console");
        zos3270Console.setLayoutData(gd);
        zos3270Console.setLayout(topLayout);

        buttonConsoleDefault = new Button(zos3270Console, SWT.RADIO);
        buttonConsoleDefault.setText("Default");
        buttonConsoleDefault.addSelectionListener(this);
        buttonConsoleYes = new Button(zos3270Console, SWT.RADIO);
        buttonConsoleYes.setText("Yes");
        buttonConsoleYes.addSelectionListener(this);
        buttonConsoleNo = new Button(zos3270Console, SWT.RADIO);
        buttonConsoleNo.setText("No");
        buttonConsoleNo.addSelectionListener(this);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(LIVE_TERMINAL, 0);
        config.setAttribute(LOG_CONSOLE, 0);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        try {
            switch(config.getAttribute(LIVE_TERMINAL, 0)) {
                case 1:
                    buttonLiveDefault.setSelection(false);
                    buttonLiveYes.setSelection(true);
                    buttonLiveNo.setSelection(false);
                    break;
                case 2:
                    buttonLiveDefault.setSelection(false);
                    buttonLiveYes.setSelection(false);
                    buttonLiveNo.setSelection(true);
                    break;
                case 0:
                default:
                    buttonLiveDefault.setSelection(true);
                    buttonLiveYes.setSelection(false);
                    buttonLiveNo.setSelection(false);
                    break;
            }
        } catch(Exception e) {
            Zos3270Activator.log(e);
        }


        try {
            switch(config.getAttribute(LOG_CONSOLE, 0)) {
                case 1:
                    buttonConsoleDefault.setSelection(false);
                    buttonConsoleYes.setSelection(true);
                    buttonConsoleNo.setSelection(false);
                    break;
                case 2:
                    buttonConsoleDefault.setSelection(false);
                    buttonConsoleYes.setSelection(false);
                    buttonConsoleNo.setSelection(true);
                    break;
                case 0:
                default:
                    buttonConsoleDefault.setSelection(true);
                    buttonConsoleYes.setSelection(false);
                    buttonConsoleNo.setSelection(false);
                    break;
            }
        } catch(Exception e) {
            Zos3270Activator.log(e);
        }



    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        if (buttonLiveYes.getSelection()) {
            configuration.setAttribute(LIVE_TERMINAL, 1);
        } else if (buttonLiveNo.getSelection()) {
            configuration.setAttribute(LIVE_TERMINAL, 2);
        } else {
            configuration.setAttribute(LIVE_TERMINAL, 0);
        }
        
        if (buttonConsoleYes.getSelection()) {
            configuration.setAttribute(LOG_CONSOLE, 1);
        } else if (buttonConsoleNo.getSelection()) {
            configuration.setAttribute(LOG_CONSOLE, 2);
        } else {
            configuration.setAttribute(LOG_CONSOLE, 0);
        }
    }

    @Override
    public boolean validatePage() {
        return true;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent arg0) {
    }

    @Override
    public void widgetSelected(SelectionEvent arg0) {
        tab.configurationUpdate();
    }

}
