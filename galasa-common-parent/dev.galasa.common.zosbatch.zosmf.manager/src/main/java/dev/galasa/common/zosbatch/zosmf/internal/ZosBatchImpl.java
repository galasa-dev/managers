package dev.galasa.common.zosbatch.zosmf.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.common.zos.IZosImage;
import dev.galasa.common.zosbatch.IZosBatch;
import dev.galasa.common.zosbatch.IZosBatchJob;
import dev.galasa.common.zosbatch.IZosBatchJobname;
import dev.galasa.common.zosbatch.ZosBatchException;
import dev.galasa.common.zosbatch.ZosBatchManagerException;

public class ZosBatchImpl implements IZosBatch {
	
	private List<ZosBatchJobImpl> zosBatchJobs = new ArrayList<>();
	
	@Override
	public @NotNull IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname, @NotNull IZosImage image)	throws ZosBatchException {
		
		if (jobname == null) {
			jobname = new ZosBatchJobnameImpl(image.getImageID());		
		}
			
		ZosBatchJobImpl zosBatchJobImpl;
		try {
			zosBatchJobImpl = new ZosBatchJobImpl(image, jobname, jcl);
			this.zosBatchJobs.add(zosBatchJobImpl);
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Unable to submit batch job", e);
		}
				
		return zosBatchJobImpl.submitJob();
	}

	/**
	 * Clean up any existing batch jobs
	 * @throws ZosBatchException
	 */
	public void cleanup() throws ZosBatchException {
		
		Iterator<ZosBatchJobImpl> iterator = zosBatchJobs.iterator();
		while (iterator.hasNext()) {
			ZosBatchJobImpl zosBatchJob = iterator.next();
			if (!zosBatchJob.isArchived()) {
				zosBatchJob.archiveJobOutput();
			}
			if (!zosBatchJob.isPurged()) {
				zosBatchJob.purgeJob();
			}
		}
	}
}
