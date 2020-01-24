package dev.galasa.kubernetes.internal.resources;

import dev.galasa.kubernetes.IPodLog;

public class PodLogImpl implements IPodLog {
    private final String name;
    private final String log;

    public PodLogImpl(String name, String log) {
        this.name = name;
        this.log  = log;
    }

    @Override
    public String getLog() {
        return this.log;
    }

}
