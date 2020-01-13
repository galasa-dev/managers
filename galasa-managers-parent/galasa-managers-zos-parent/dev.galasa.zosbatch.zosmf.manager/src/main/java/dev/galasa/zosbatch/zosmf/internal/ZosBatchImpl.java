/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch.zosmf.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;

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
		
		if (jobname == null) {
			jobname = new ZosBatchJobnameImpl(this.image.getImageID());		
		}
		
		ZosBatchJobImpl zosBatchJob = newZosBatchJob(jcl, jobname);
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
			ZosBatchJobImpl zosBatchJob = iterator.next();
			if (zosBatchJob.submitted()) {
				if (!zosBatchJob.isArchived()) {
					zosBatchJob.archiveJobOutput();
				}
				if (!zosBatchJob.isPurged()) {
					zosBatchJob.purgeJob();
				}
			}
			iterator.remove();
		}
	}

	public ZosBatchJobImpl newZosBatchJob(String jcl, IZosBatchJobname jobname) throws ZosBatchException {
		ZosBatchJobImpl zosBatchJob;
		try {
			zosBatchJob = new ZosBatchJobImpl(this.image, jobname, jcl);
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Unable to submit batch job", e);
		}
		return zosBatchJob;
	}
}
