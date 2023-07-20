/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.hursley.cicsts.test.sem.complex.CICSRegion;
import com.ibm.hursley.cicsts.test.sem.complex.Complex;
import com.ibm.hursley.cicsts.test.sem.complex.Sit;
import com.ibm.hursley.cicsts.test.sem.complex.jcl.Job;

import conrep.CICS;
import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.MasType;
import dev.galasa.cicsts.spi.BaseCicsImpl;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.sem.SemManagerException;
import dev.galasa.sem.internal.properties.ExternalVersion;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;

public class SemCicsImpl extends BaseCicsImpl {
	
    private static final Log logger = LogFactory.getLog(SemCicsImpl.class);

    private final SemManagerImpl semManager;
    private final SemZosHandler semZosHandler;

    private final Complex    complex;
    private final CICS       conrepCics;
    private CICSRegion       complexCics;

    private String     		 jcl;
    private final String     jobname;
    private final String     vtamnode;
    private final boolean    provisionStart;

    private IZosBatchJob     job;

    private ProductVersion productVersion;

    public SemCicsImpl(SemManagerImpl semManager, ICicstsManagerSpi cicstsManager, SemZosHandler semZosHandler, Complex complex, CICS conrepCics, IZosImage zosImage, String cicsTag, MasType masType, boolean provisionStart) throws CicstsManagerException {
        super(cicstsManager, cicsTag, zosImage, conrepCics.getApplid().getApplid(), masType);
        this.semManager  = semManager;
        this.semZosHandler = semZosHandler;
        this.conrepCics  = conrepCics;
        this.complex     = complex;
        this.provisionStart = provisionStart;

        try {
            this.productVersion = ExternalVersion.get(this.conrepCics.getCICSVersion().getVersion().getLiteral().substring(1));
        } catch (ManagerException e) {
            throw new SemManagerException("Unable to parse the CICS TS version", e);
        }

        for(CICSRegion cicsRegion : complex.getCICS()) {
            if (cicsRegion.getApplid().equals(getApplid())) {
                this.complexCics = cicsRegion;
                break;
            }
        }

        if (this.complexCics == null) {
            throw new SemManagerException("Unable to locate CICS Region in SEM complex with applid '" + getApplid() + "'");
        }

		// Get a runtime job for this CICS
		ArrayList<Job> jobs = new ArrayList<>();
		
		try {
			this.complexCics.Build_Runtime_Jobs(this.complex, jobs);

		} catch (Exception e) {
            throw new SemManagerException("Could not generate the CICS runtime JCL", e);
		}

		// Only one job should found
		if (jobs.size() != 1) {
			throw new SemManagerException("More than one runtime JCL returned from complex");
		}

		// Get the single job in the array
		Job job = jobs.get(0);

		// Set the constant information of this CICS job
		this.jobname = job.getJobname();
		this.vtamnode = this.complexCics.getVtamnode();

		// Build JCL to startup CICS
		buildCicsJcl(job);  
    }

    /**
     * From a batch job, generate some JCL to startup CICS
     * and activate the APPLID of the CICS job. Store the JCL
     * ready for CICS startup.
     * 
     * @param job
     * @throws SemManagerException
     */
	private void buildCicsJcl(Job job) throws SemManagerException {

		try {
			
			// Generate the JCL from the job
			String runtimeJcl = semZosHandler.generateJCL(job);

			StringBuilder sb = new StringBuilder();
			
			// Append the start of the JCL with system information
			sb.append("/*JOBPARM SYSAFF=");
			sb.append(getZosImage().getSysname());
			sb.append("\n");

			// Append the JCL with commands to initialise the APPLID
			if (this.vtamnode != null && vtamnode.length() > 0) {
				sb.append("//NET      COMMAND 'VARY NET,ID=");
				sb.append(this.vtamnode);
				sb.append(",ACT'\n");
			}

			// Append the runtime CICS JCL to existing JCL
			sb.append(runtimeJcl);

			// Store the JCL ready for runtime
			this.jcl = sb.toString();
			
		} catch (Exception e) {
			
            throw new SemManagerException("Unable to generate CICS region JCL", e);
		}
	}

    @Override
    public ProductVersion getVersion() throws CicstsManagerException {
        return this.productVersion;
    }

    protected CICSRegion getComplexCics() {
        return this.complexCics;
    }

    protected CICS getConrepCics() {
        return this.conrepCics;
    }

    protected String getJCL() {
        return this.jcl;
    }

    protected String getJobname() {
        return this.jobname;
    }

    @Override
    public boolean isProvisionStart() {
        return this.provisionStart;
    }

    @Override
    public void startup() throws SemManagerException {
        submitRuntimeJcl();

        Instant expire = Instant.now().plus(1, ChronoUnit.MINUTES);
        while(expire.isAfter(Instant.now())) {
            if (hasRegionStarted()) {
                try {
                    this.semManager.getCicsManager().cicstsRegionStarted(this);
                } catch (CicstsManagerException e) {
                    throw new SemManagerException("Post startup for CICS TS region " + getApplid() + " failed", e);
                }
                return;
            }
        }

        throw new SemManagerException("Provisioned CICS TS Region " + getApplid() + " failed to start in time");
    }

    @Override
    public void shutdown() throws SemManagerException {
        logger.info("Shutting down CICS TS region " + getApplid());
        if (this.job == null) {
            logger.info("Not shutting down CICS TS Region " + getApplid() + " as it is not up");
            return;
        }

        JobStatus currentStatus = job.getStatus();        
        switch(currentStatus) {
            case ACTIVE:
                issueShutdownCommand(false);
                break;
            case INPUT:
                logger.warn("CICS TS region is on input queue, cancelling");
                try {
                    this.job.cancel();
                } catch (ZosBatchException e) {
                    throw new SemManagerException("Unable to cancel the CICS TS region from the JES2 input queue", e);
                }
                saveCicsRegion();
                return;
            case NOTFOUND:
                logger.warn("Unable to locate CICS TS region, shutdown bypassed");
                return;
            case OUTPUT:
                logger.info("CICS TS region already down");
                saveCicsRegion();
                return;
            case UNKNOWN:
                throw new SemManagerException("Unable to determine status of the CICS TS region");
        }

        logger.info("Waiting for provisioned CICS TS region " + getApplid() + " to shutdown");
        
        if (waitForRegionsToStop(2)) {
            return;
        }
        
        logger.info("CICS TS region " + getApplid() + " did not stop within 2 minutes, trying immediate shutdown");
        
        issueShutdownCommand(true);
        
        if (waitForRegionsToStop(2)) {
            return;
        }

        logger.info("CICS TS region " + getApplid() + " did not stop within 2 minutes, trying cancel");
        
        try {
            this.job.cancel();
        } catch (ZosBatchException e) {
            throw new SemManagerException("Cancel for CICS TS region failed", e);
        }
        
        if (waitForRegionsToStop(2)) {
            return;
        }
        
        throw new SemManagerException("CICS TS region " + getApplid() + " failed to shutdown");
    }

    private boolean waitForRegionsToStop(int timeoutMinutes) throws SemManagerException {
        
        Instant expire = Instant.now().plus(timeoutMinutes, ChronoUnit.MINUTES);
        while(expire.isAfter(Instant.now())) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SemManagerException("Wait for CICS TS region stop interrupted", e);
            }

            JobStatus currentStatus = job.getStatus();        
            switch(currentStatus) {
                case ACTIVE:
                    break;
                case OUTPUT:
                    logger.info("CICS TS region has been shutdown");
                    saveCicsRegion();
                    return true;
                default:
                    throw new SemManagerException("Invalid status during CICS TS region shutdown, " + currentStatus.toString());
            }
        }
        
        return false;
    }

    private void issueShutdownCommand(boolean immediate) throws SemManagerException {
        if (job == null) {
            throw new SemManagerException("Unable to determine the batch job for CICS TS Region " + getApplid());
        }
        
        IZosConsole console;
        try {
            console = this.semManager.getZosConsoleManager().getZosConsole(getZosImage());
        } catch (ZosConsoleManagerException e) {
            throw new SemManagerException("Unable to obtain console for shutdown command", e);
        }

        switch(getMasType()) {
        case CICS:
        case LMAS:
        case WUI:
        case NONE:
            StringBuilder shutDownCommand = new StringBuilder("CEMT PERFORM SHUTDOWN");
            
            if (immediate) {
                shutDownCommand.append(" IMMEDIATE");
            }

            try {
                console.issueCommand("F " + job.getJobname() + "," + shutDownCommand.toString());
            } catch (ZosConsoleException e) {
                throw new SemManagerException("Error attempting to issue shutdown command", e);
            }
            logger.info("Issued '" + shutDownCommand + "' to CICS Region '"  + job + "'");
            break;
        case CMAS:
            try {
                console.issueCommand("F " + job.getJobname() + ",COSD");
            } catch (ZosConsoleException e) {
                throw new SemManagerException("Error attempting to issue shutdown command", e);
            }
            logger.info("Issued 'COSD' to CICS TS region "  + getApplid());
            return;
        }
    }


    private void saveCicsRegion() {
    	
    	try {
			job.saveOutputToResultsArchive("sem/cics");
		} catch (ZosBatchException e) {
			logger.error("Failed to save cics output to stored artifacts", e);
		}

    }

    @Override
    public void submitRuntimeJcl() throws SemManagerException {
        IZosBatch zosBatch = this.semManager.getZosBatch().getZosBatch(this.getZosImage());
        try {
            IZosBatchJobname jobname = this.semManager.getZosManager().newZosBatchJobname(getJobname());
            // TODO need to sort the full jobcard, ie userid etc
            this.job = zosBatch.submitJob(this.jcl, jobname);    
            this.job.setShouldArchive(false);
        } catch(Exception e) {
            throw new SemManagerException("Failed to submit the runtime JCL for provisioned CICS TS region " + getApplid(), e);
        }

        logger.info("Submitted provisioned CICS TS region " + getApplid() + " as " + this.job);

        Instant expire = Instant.now().plus(1, ChronoUnit.MINUTES);
        while(expire.isAfter(Instant.now())) {
            try {
                Thread.sleep(2000);

                switch(this.job.getStatus()) {
                    case ACTIVE:
                        logger.trace("Wait for region submission completed, job now active");
                        return;
                    case INPUT:
                    case NOTFOUND:
                        break;
                    case OUTPUT:
                        throw new SemManagerException("Wait for region submission failed, job was on output queue");
                    case UNKNOWN:
                    default:
                        throw new SemManagerException("Wait for region submission failed, status unknown");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SemManagerException("Wait for region submission was interrupted", e);
            }
        }

        throw new SemManagerException("Wait for provisioned CICS TS region submission failed, job did not start");
    }

    @Override
    public boolean hasRegionStarted() throws SemManagerException {

        IZosBatchJobOutputSpoolFile jesmsglgSpool;
        IZosBatchJobOutputSpoolFile eyulogSpool =  null;
        try {
            jesmsglgSpool = this.job.getSpoolFile("JESMSGLG");
            if (getMasType() == MasType.WUI) {
                eyulogSpool = this.job.getSpoolFile("EYULOG");
            }
        } catch(Exception e) {
            throw new SemManagerException("Failed to retrieve job output during region startup", e);
        }

        String jesmsglg = jesmsglgSpool.getRecords();
        if (jesmsglg.contains("DFHKE0101")) {
            throw new SemManagerException("Startup of CICS TS region " + getApplid() + " failed, not APF authorised");
        }

        if (jesmsglg.contains("DFHSI1538D")) {
            throw new SemManagerException(
                    "Startup of CICS TS region " + getApplid() + " failed, invalid CSD group");
        }

        if (jesmsglg.contains("DFHPA1909") || jesmsglg.contains("DFHPA1912") || jesmsglg.contains("DFHPA1915")) {
            throw new SemManagerException(
                    "Startup of CICS TS region " + getApplid() + " failed, invalid parameter found");
        }

        if (jesmsglg.contains("IEF404I")) {
            throw new SemManagerException("Startup of CICS TS region " + getApplid() + " failed, early termination");
        }

        if (jesmsglg.contains("$HASP395")) {
            throw new SemManagerException("Startup of CICS TS region " + getApplid() + " failed, early termination");
        }

        if (getMasType() == MasType.WUI) {
            String eyulog = eyulogSpool.getRecords();

            if (eyulog.contains("EYUVS0005S")) {
                throw new SemManagerException("Startup of WUI region " + getApplid() + " failed, EYUVS0005S detected");
            }

            boolean DFHSI1517   = jesmsglg.contains("DFHSI1517");

            //            if (wuiBasicStartup) {
            //                if (DFHSI1517) {
            //                    logger.info("WUI Region '" + job + "' has completed startup(basic)");
            //                    toBeCheckedi.remove();
            //                    checkDefaultSecurity(output);
            //                }
            //            } else {
            boolean EYUVS0002I  = eyulog.contains("EYUVS0002I");
            boolean EYUNL0099I  = jesmsglg.contains("EYUNL0099I");

            if (DFHSI1517 && EYUVS0002I && EYUNL0099I) {
                logger.info("WUI Region " + getApplid() + " has completed startup");
                //                checkDefaultSecurity(output);
                return true;
            }
            //            }
        } else if (getMasType() == MasType.CMAS) {
            boolean DFHSI1517   = jesmsglg.contains("DFHSI1517");

            //            if (cmasBasicStartup) {
            //                if (DFHSI1517) {
            //                    logger.info("CMAS Region '" + job + "' has completed startup(basic)");
            //                    toBeCheckedi.remove();
            //                    checkDefaultSecurity(output);
            //                }
            //            } else {
            boolean EYUXL0010I  = jesmsglg.contains("EYUXL0010I");

            if (DFHSI1517 && EYUXL0010I) {
                logger.info("CMAS Region " + getApplid() + " has completed startup");
                //                checkDefaultSecurity(output);
                return true;
            }
        } else {
            if (jesmsglg.contains("DFHSI1517")) {
                logger.info("CICS TS Region " + getApplid() + " has completed startup");
                //                checkDefaultSecurity(output);
                return true;
            }
        }

        return false;
    }

	@Override
	public String getUssHome() throws CicstsManagerException {
		throw new CicstsManagerException("Not developed yet");
	}

	@Override
	public String getJvmProfileDir() throws CicstsManagerException {
		throw new CicstsManagerException("Not developed yet");
	}

	@Override
	public String getJavaHome() throws CicstsManagerException {
		throw new CicstsManagerException("Not developed yet");
	}

	@Override
	public IZosBatchJob getRegionJob() throws CicstsManagerException {
		if (this.job == null) {
			throw new CicstsManagerException("The CICS Region job for the CICS TS region is missing");
		}
		
		return this.job;
	}

    @Override
    public void alterSit(@NotNull String sitParam, String sitValue) throws CicstsManagerException {

    	// Get the CICS
    	ICicsRegion theCics = cicstsManager.locateCicsRegion(this.getTag());
    	
    	// Restricted SIT parameter
    	if (sitParam.toUpperCase().equals("APPLID") | sitParam.toUpperCase().equals("GRPLIST") || sitParam.toUpperCase().equals("SYSID")) {
    		throw new CicstsManagerException(sitParam.toUpperCase() + " is a restricted SIT parameter. It cannot be changed");
    	}
    	
    	// CICS is currently running
        if (job.getStatus() == JobStatus.ACTIVE) {
        	throw new CicstsManagerException("The CICS region " + theCics.getApplid() + " with tag " + theCics.getTag() + " is still running. Cannot alter a running CICS region");
        }
        
        for (CICSRegion cicsRegion : complex.getCICS()) {
        	
        	// Find the correct region
        	if (cicsRegion.getApplid().equals(theCics.getApplid())) {
        		
        		// Add the Sit parameter
        		if (sitValue != null) {
        			
					Sit sitOverride = new Sit(sitParam, sitValue, "Overridden during runtime");

					// Apply the SIT override
					cicsRegion.runtimeOverrideSIT(sitOverride);
	        		rebuildRuntimeJob(cicsRegion);
        		
        		} else {
        			
        			throw new CicstsManagerException("Sit value in alterSit() cannot be null use removeSit()");
        		}
           	}
        }
    }
    
    @Override
    public void removeSit(@NotNull String sitParam) throws CicstsManagerException {
    	
    	// Get the CICS
    	ICicsRegion theCics = cicstsManager.locateCicsRegion(this.getTag());
    	
    	// Check the restricted parameters
    	if (sitParam.toUpperCase().equals("APPLID") | sitParam.toUpperCase().equals("GRPLIST") || sitParam.toUpperCase().equals("SYSID")) {
    		throw new CicstsManagerException(sitParam.toUpperCase() + " is a restricted SIT parameter. It cannot be removed");
       	}
    	
    	// Check if CICS is currently running
    	if (job.getStatus() == JobStatus.ACTIVE) {
    		throw new CicstsManagerException("The CICS region " + theCics.getApplid() + " with tag " + theCics.getTag() + " is still running. Cannot remove a SIT from a running region");
    	}
    	
		for (CICSRegion cicsRegion : complex.getCICS()) {

        	// Find the correct region
			if (cicsRegion.getApplid().equals(theCics.getApplid())) {

				// Remove the SIT
				cicsRegion.removeSit(sitParam);
				rebuildRuntimeJob(cicsRegion);
			}
		}
	}

    /**
     * Performs a rebuild of the given CICS job
     * 
     * @param region
     * @throws CicstsManagerException
     */
	private void rebuildRuntimeJob(CICSRegion region) throws CicstsManagerException {

		ArrayList<Job> jobs = new ArrayList<Job>();

		try {

			// Rebuild the JCL with the changed SIT parameter
			region.Build_Runtime_Jobs(complex, jobs);

		} catch (Exception e) {
			throw new CicstsManagerException("Unable to rebuild runtime JCL" , e);
		}

		// Perform a rebuild of the JCL
		buildCicsJcl(jobs.get(0));
	}
}
