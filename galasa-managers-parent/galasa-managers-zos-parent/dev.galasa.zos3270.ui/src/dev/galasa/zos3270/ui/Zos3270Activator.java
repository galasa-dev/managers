/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import dev.galasa.eclipse.Activator;
import dev.galasa.eclipse.liveupdates.ILiveUpdateServer;
import dev.galasa.zos3270.ui.terminal.LiveTerminalsServlet;

/**
 * The activator class controls the plug-in life cycle
 */
public class Zos3270Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "dev.galasa.zos3270.ui"; //$NON-NLS-1$

    // The shared instance
    private static Zos3270Activator plugin;

    private Path                    liveTerminalsPath;

    private LiveTerminalsServlet    liveTerminalServlet;
    private URL                     liveTerminalUrl;

    /**
     * The constructor
     */
    public Zos3270Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
     * BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        if (this.liveTerminalServlet != null) {
            Activator.getLiveUpdateServer().unregisterServlet(liveTerminalServlet);
            this.liveTerminalServlet = null;
        }

        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Zos3270Activator getDefault() {
        return plugin;
    }

    /**
     * Log a throwable
     * 
     * @param e
     */
    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e)); //$NON-NLS-1$
    }

    /**
     * Log a status
     * 
     * @param status
     */
    public static void log(IStatus status) {
        ILog log = plugin.getLog();
        if (log != null) {
            log.log(status);
        }
    }

    /**
     * 
     * @return - plugin ID
     */
    public static String getPluginId() {
        return PLUGIN_ID;
    }

    public synchronized Path getLiveTerminalsPath() {
        try {
            if (liveTerminalsPath == null) {
                Path cachePath = Activator.getCachePath();
                this.liveTerminalsPath = cachePath.resolve("liveterminals");
                Files.createDirectories(this.liveTerminalsPath);
            }
        } catch (Exception e) {
            log(e);
        }
        return this.liveTerminalsPath;
    }

    public synchronized URL getLiveTerminalURL() throws Exception {
        if (this.liveTerminalServlet == null) {
            this.liveTerminalServlet = new LiveTerminalsServlet();

            ILiveUpdateServer liveServer = Activator.getLiveUpdateServer();
            liveServer.registerServlet(this.liveTerminalServlet, "/zos3270/liveterminals");

            this.liveTerminalUrl = new URL(liveServer.getLiveUpdateUrl() + "zos3270/liveterminals");
        }

        return this.liveTerminalUrl;
    }

}
