/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchJobcard;

/**
 * Implementation of {@link IZosBatch} using zOS/MF
 *
 */
public class ZosBatchImpl implements IZosBatch {
    
    private List<ZosBatchJobImpl> zosBatchJobs = new ArrayList<>();
    private IZosImage image;
    
    public ZosBatchImpl(IZosImage image) {
        this.image = image;
    }
    
    @Override
    public @NotNull IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname) throws ZosBatchException {
        return submitJob(jcl, jobname, null);
    }
    
    @Override
    public @NotNull IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname, ZosBatchJobcard jobcard)
            throws ZosBatchException {
        if (jobname == null) {
            jobname = new ZosBatchJobnameImpl(this.image.getImageID());        
        }
        
        if (jobcard == null) {
            jobcard = new ZosBatchJobcard();
        }
        
        ZosBatchJobImpl zosBatchJob = new ZosBatchJobImpl(this.image, jobname, jcl, jobcard);
        this.zosBatchJobs.add(zosBatchJob);
        
        return zosBatchJob.submitJob();
    }


    /**
     * Clean up any existing batch jobs
     * @throws ZosBatchException
     */
    public void cleanup() throws ZosBatchException {
        
        Iterator<ZosBatchJobImpl> iterator = zosBatchJobs.iterator();
        while (iterator.hasNext()) {
            ZosBatchJobImpl zosBatchJobImpl = iterator.next();
            if (zosBatchJobImpl.submitted()) {
                if (!zosBatchJobImpl.isComplete()) {
                    zosBatchJobImpl.cancel();
                    zosBatchJobImpl.archiveJobOutput();
                    zosBatchJobImpl.purge();
                } else {
                    if (!zosBatchJobImpl.isArchived()) {
                        zosBatchJobImpl.archiveJobOutput();
                    }
                    if (!zosBatchJobImpl.isPurged()) {
                        zosBatchJobImpl.purge();
                    }
                }
            }
            iterator.remove();
        }
    }

}
