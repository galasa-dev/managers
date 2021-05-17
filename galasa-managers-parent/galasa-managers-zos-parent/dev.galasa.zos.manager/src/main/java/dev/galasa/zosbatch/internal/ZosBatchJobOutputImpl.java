/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.zosbatch.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutput;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.spi.IZosBatchJobOutputSpi;

/**
 * Implementation of {@link IZosBatchJobOutput}
 *
 */
public class ZosBatchJobOutputImpl implements IZosBatchJobOutputSpi, Iterable<IZosBatchJobOutputSpoolFile> {

	private IZosBatchJob batchJob;
    private String jobname;
    private String jobid;
    
    private ArrayList<IZosBatchJobOutputSpoolFile> spoolFiles = new ArrayList<>();

    public ZosBatchJobOutputImpl(IZosBatchJob batchJob, String jobname, String jobid) {
    	this.batchJob = batchJob;
        this.jobname = jobname;
        this.jobid = jobid;
    }

    @Override
    public void addJcl(String jcl) {
        spoolFiles.add(new ZosBatchJobOutputSpoolFileImpl(batchJob, this.jobname, this.jobid, "", "", "JESJCLIN", "JCL", jcl));
    }

    @Override
    public void addSpoolFile(String stepname, String procstep, String ddname, String id, String records) {
        spoolFiles.add(new ZosBatchJobOutputSpoolFileImpl(batchJob, this.jobname, this.jobid, Objects.toString(stepname, ""), Objects.toString(procstep, ""), ddname, id, records));
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

    @Override
    public int size() {
        return spoolFiles.size();
    }

    @Override
    public boolean isEmpty() {
        return spoolFiles.isEmpty();
    }
}
