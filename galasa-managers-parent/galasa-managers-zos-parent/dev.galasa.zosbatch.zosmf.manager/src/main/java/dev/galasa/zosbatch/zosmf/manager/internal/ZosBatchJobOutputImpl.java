/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;

import dev.galasa.zosbatch.IZosBatchJobOutput;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;

/**
 * Implementation of {@link IZosBatchJobOutput} using zOS/MF
 *
 */
public class ZosBatchJobOutputImpl implements IZosBatchJobOutput, Iterable<IZosBatchJobOutputSpoolFile> {

    private String jobname;
    private String jobid;
    
    private ArrayList<IZosBatchJobOutputSpoolFile> spoolFiles = new ArrayList<>();

    public ZosBatchJobOutputImpl(String jobname, String jobid) {
        this.jobname = jobname;
        this.jobid = jobid;
    }

    public void addJcl(String jcl) {
        spoolFiles.add(new ZosBatchJobOutputSpoolFileImpl(this.jobname, this.jobid, jcl));
    }

    public void add(JsonObject spoolFile, String records) {
        spoolFiles.add(new ZosBatchJobOutputSpoolFileImpl(spoolFile, records));
    }

    @Override
    public String getJobname() throws ZosBatchException {
        return this.jobname;
    }

    @Override
    public String getJobid() throws ZosBatchException {
        return this.jobid;
    }
    
    @Override
    public List<IZosBatchJobOutputSpoolFile> getSpoolFiles() {
        return this.spoolFiles;
    }

    @Override
    public List<String> toList() {
        ArrayList<String> spoolFilesList = new ArrayList<>();
        spoolFiles.forEach(spoolFile-> 
            spoolFilesList.add(spoolFile.getRecords())
        );
        return spoolFilesList;
    }

    @Override
    public String toString() {
        return this.jobname + "_" + this.jobid;
    }

    @Override
    public Iterator<IZosBatchJobOutputSpoolFile> iterator() {
        Iterator<IZosBatchJobOutputSpoolFile> spoolFilesIterator = spoolFiles.iterator();
        return new Iterator<IZosBatchJobOutputSpoolFile>() {

            @Override
            public boolean hasNext() {
                return spoolFilesIterator.hasNext();
            }

            @Override
            public IZosBatchJobOutputSpoolFile next() {
                return spoolFilesIterator.next();
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Object can not be updated");
            }
        };
    }
}
