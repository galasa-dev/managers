/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.hursley.cicsts.test.sem.complex.IOSFileSupplier;
import com.ibm.hursley.cicsts.test.sem.complex.ZOSJobSupplier;
import com.ibm.hursley.cicsts.test.sem.complex.jcl.JCLException;
import com.ibm.hursley.cicsts.test.sem.complex.jcl.Job;
import com.ibm.hursley.cicsts.test.sem.complex.jcl.Step;

import dev.galasa.sem.SemManagerException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.spi.IZosBatchSpi;

public class SemZosHandler implements IOSFileSupplier, ZOSJobSupplier {
    
    private static final Log logger = LogFactory.getLog(SemZosHandler.class);

    private IZosManagerSpi zosManager;
    private IZosBatchSpi   zosBatch;
    private IZosImage      primaryZosImage;
    private IZosImage      secondaryZosImage;
    
    private ArrayList<IZosBatchJob> batchJobs = new ArrayList<>();
    
    public SemZosHandler(IZosManagerSpi zosManager, IZosBatchSpi zosBatch, IZosImage primaryZosImage, IZosImage secondaryZosImage) throws SemManagerException {
        this.zosManager        = zosManager;
        this.zosBatch          = zosBatch;
        this.primaryZosImage   = primaryZosImage;
        this.secondaryZosImage = secondaryZosImage;
        
    }

    @Override
    public void submitJob(Job job) throws JCLException {
        try {
            String jcl = generateJCL(job);
            
            // Get the system to submit the job on
            IZosImage zosImage = null;
            String system = job.getSystem();
            if (system == null) {
                zosImage = this.primaryZosImage;
            } else {
                if (this.primaryZosImage.getSysname().equals(system)) {
                    zosImage = this.primaryZosImage;
                } else if (this.secondaryZosImage.getSysname().equals(system)) {
                    zosImage = this.secondaryZosImage;
                } else {
                    throw new JCLException("Unable to submit job as system '" + system + "' is unknown to the SEM Manager");
                }
            }
            
            IZosBatchJobname jobname = this.zosManager.newZosBatchJobname(zosImage);
            IZosBatch batch = this.zosBatch.getZosBatch(this.primaryZosImage);
            IZosBatchJob batchJob = batch.submitJob(jcl, jobname);
            
            job.setMvsJob(batchJob);
            
            this.batchJobs.add(batchJob);
            
        } catch(Exception e) {
            throw new JCLException("Unable to submit job", e);
        }
    }
    
    public String generateJCL(Job job) throws IllegalArgumentException, IllegalAccessException {
        List<Step> steps = job.getSteps();
        
        StringBuilder jcl = new StringBuilder();
        for(Step step : steps) {
            if (step != null) {
                for(String line : step.buildJCL()) {
                    jcl.append(line);
                }
            }
        }
        
        return jcl.toString();
    }

    @Override
    public boolean isJobOutputReady(Job job) {
        IZosBatchJob batchJob = (IZosBatchJob) job.getMvsJob();
        
        if (batchJob.getStatus() == JobStatus.OUTPUT) {
            return true;
        }
        
        return false;
    }

    @Override
    public void discardOutput(Job job) {
        throw new UnsupportedOperationException("Not written yet");
    }

    @Override
    public List<String> getJobOutput(Job job) {
        IZosBatchJob batchJob = (IZosBatchJob) job.getMvsJob();

        try {
            IZosBatchJobOutputSpoolFile output = batchJob.getSpoolFile("JESYSMSG");
            String records = output.getRecords();
            
            
            ArrayList<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new StringReader(records))) {
                String line = null;
                while((line = br.readLine()) != null) {
                    if (line.length() > 2) {
                        lines.add(line.substring(1));   //  remove the ASA
                    }
                }
            }
            
            return lines;
        } catch (Exception e) {
            logger.error("Failed to retrieve output for " + batchJob,e);
            return new ArrayList<>();
        }
    }

    @Override
    public void cancelConflictingJobnames(ArrayList<String> jobnames) throws IOException {
        // Ignoring this as the zOS Batch Manager should cater for this
    }

    @Override
    public boolean isJobnameOnExcecutionQueue(String jobname) throws IOException {
        // Ignoring as zOS Batch Manager takes responsibility
        return false;
    }

    @Override
    public List<String> getJesMsgLg(Job job) {
        throw new UnsupportedOperationException("Not written yet");
        //        return null;
    }

    @Override
    public void cancelJob(Job job) {
        throw new UnsupportedOperationException("Not written yet");
    }

    @Override
    public BufferedReader getFile(String filename) throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not written yet");
        //        return null;
    }
    
    protected List<IZosBatchJob> getJobs() {
        return this.batchJobs;
    }
    
    protected void clearJobs() {
        this.batchJobs.clear();
    }

}
