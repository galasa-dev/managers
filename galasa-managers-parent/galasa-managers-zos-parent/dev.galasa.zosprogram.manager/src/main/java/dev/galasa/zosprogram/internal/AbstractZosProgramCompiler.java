/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosprogram.ZosProgramException;
import dev.galasa.zosprogram.ZosProgramManagerException;

public class AbstractZosProgramCompiler {
    
    private static final Log logger = LogFactory.getLog(AbstractZosProgramCompiler.class); 
    
    protected ZosProgramImpl zosProgram;
    
    protected static final String NEWLINE = "\n";
    protected static final String SYSLIN = "DISP=(OLD,DELETE),DSN=&&SYSLIN";
    protected static final String DD = "//         DD ";
    protected static final String DD_ASTERISK = "//         DD *";
    protected static final String LKED_SYSIN_ENTRY = "  ENTRY ++NAME++";
    protected static final String LKED_SYSIN_NAME_REPLACE = "  NAME ++NAME++(R)";

    public AbstractZosProgramCompiler(ZosProgramImpl zosProgram) throws ZosProgramException {
        this.zosProgram = zosProgram;
        if (zosProgram.getLoadlib() == null) {
            try {
                zosProgram.setLoadlib(ZosProgramManagerImpl.getRunLoadlib(zosProgram.getImage()));
            } catch (ZosProgramManagerException e) {
                throw new ZosProgramException(e);
            }
        }
    }
    
    protected void compile() throws ZosProgramException {
        submitCompileJob(buildCompileJcl());
    }

    protected String buildCompileJcl() throws ZosProgramException {
        IBundleResources managerBundleResources = ZosProgramManagerImpl.getManagerBundleResources();
        try {
            InputStream inputStream = managerBundleResources.retrieveSkeletonFile("resources/" + getSkelName(), buildParameters());
            return managerBundleResources.streamAsString(inputStream);
        } catch (TestBundleResourceException | IOException e) {
            throw new ZosProgramException("Problem loading JCL skeleton", e);
        }
    }

    protected void submitCompileJob(String compileJcl) throws ZosProgramException {
        IZosBatchJob compileJob;
        try {
            compileJob = ZosProgramManagerImpl.getZosBatch(zosProgram.getImage()).submitJob(compileJcl, null);
            zosProgram.setCompileJob(compileJob);
        } catch (ZosBatchException e) {
            throw new ZosProgramException("Problem submitting compile job for " + zosProgram.getLanguage() + " program " + zosProgram.getName() + zosProgram.logForField(), e);
        }
        int maxCc;
        try {
            maxCc = compileJob.waitForJob();
        } catch (ZosBatchException e) {
            throw new ZosProgramException("Problem waiting for compile job for " + zosProgram.getLanguage() + " program " + zosProgram.getName() + zosProgram.logForField() + ". Jobname=" + compileJob.getJobname().getName() + " Jobid=" + compileJob.getJobId(), e);
        }
        try {
            compileJob.saveOutputToTestResultsArchive();
            compileJob.purge();
        } catch (ZosBatchException e) {
            throw new ZosProgramException("Problem saving compile job output for " + zosProgram.getLanguage() + " program " + zosProgram.getName() + zosProgram.logForField() + ". Jobname=" + compileJob.getJobname().getName() + " Jobid=" + compileJob.getJobId(), e);
        }
        if (maxCc < 0 || maxCc > 4) {
            String message = "Compile job for " + zosProgram.getLanguage() + " program " + zosProgram.getName() + zosProgram.logForField() + " failed: " + compileJob.getRetcode() + ". Jobname=" + compileJob.getJobname().getName() + " Jobid=" + compileJob.getJobId();
            logger.error(message);
            throw new ZosProgramException(message);
        } else {
            logger.info("Compile job for " + zosProgram.getLanguage() + " program " + zosProgram.getName() + zosProgram.logForField() + " complete: " + compileJob.getRetcode() + ". Jobname=" + compileJob.getJobname().getName() + " Jobid=" + compileJob.getJobId());
        }
    }
    
    protected String getSkelName() {
        return null;
    }

    protected Map<String, Object> buildParameters() throws ZosProgramException {
        return null;
    }
    
    protected String formatDatasetConcatenation(List<String> datasetList) {
        if (datasetList.isEmpty()) {
            return "DUMMY";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = datasetList.iterator();
        while (it.hasNext()) {
            String dataset = it.next();
            if (dataset.startsWith("ASIS-")) {
                sb.append(dataset.substring(5));
            } else {
                sb.append("DISP=SHR,DSN=");
                sb.append(dataset);
            }
            sb.append("\n//         DD ");
        }
        return sb.delete(sb.length()-15, sb.length()).toString();
    }
}
