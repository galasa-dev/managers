package dev.galasa.galasaecosystem.internal;

import java.nio.file.Path;

public class LocalRun {

    private final String bundle;
    private final String testname;
    private final String group;
    private final String runName;
    private final int    procnumber;
    private final Path   log;
    private final Path   overrides;


    public LocalRun(String bundle,
            String testname,
            String group,
            String runName,
            int    procnumber,
            Path   log,
            Path   overrides
            ) {
        this.bundle     = bundle;
        this.testname   = testname;
        this.group      = group;
        this.runName    = runName;
        this.procnumber = procnumber;
        this.log        = log;
        this.overrides  = overrides;
    }
    
    public String getTestname() {
        return testname;
    }

    public String getGroup() {
        return group;
    }

    public int getProcnumber() {
        return procnumber;
    }

    public Path getLog() {
        return log;
    }

    public Path getOverrides() {
        return overrides;
    }

    public String getRunName() {
        return this.runName;
    }

    public String getBundle() {
        return bundle;
    }
    

}
