/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.terminal;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import dev.galasa.zos3270.ui.Zos3270Activator;

public class LiveTerminalsMonitor extends Thread {

    private final Path monitorPath;

    private boolean shutdown = false;

    public LiveTerminalsMonitor(Path monitorPath) {
        this.monitorPath = monitorPath;
    }


    @Override
    public void run() {

        try {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                this.monitorPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                
                
                while(!shutdown) {
                    WatchKey key = watchService.poll(200, TimeUnit.MILLISECONDS);
                    if (key != null) {
                        for(WatchEvent<?> event : key.pollEvents()) {
                            Object context = event.context();
                            if (context instanceof Path) {
                                Path fullTerminalPath = this.monitorPath.resolve((Path) context);
                                TerminalView.openLiveTerminal(fullTerminalPath);                                
                            }
                        }
                        key.reset();
                    }
                }
            }
        } catch(Exception e) {
            Zos3270Activator.log(e);
        }
    }


    public void shutdown() {
        this.shutdown = true;
    }

}
