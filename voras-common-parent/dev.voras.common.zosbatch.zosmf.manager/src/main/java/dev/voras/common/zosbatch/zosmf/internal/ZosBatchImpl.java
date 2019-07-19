package dev.voras.common.zosbatch.zosmf.internal;

import javax.validation.constraints.NotNull;

import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosbatch.IZosBatch;
import dev.voras.common.zosbatch.IZosBatchJob;
import dev.voras.common.zosbatch.IZosBatchJobname;
import dev.voras.common.zosbatch.ZosBatchException;
import dev.voras.common.zosbatch.ZosBatchManagerException;

public class ZosBatchImpl implements IZosBatch {
	
	@Override
	public @NotNull IZosBatchJob submitJob(@NotNull String jcl, IZosBatchJobname jobname, @NotNull IZosImage image)	throws ZosBatchException {
		
		if (jobname == null) {
			jobname = new ZosBatchJobnameImpl(image.getImageID());		
		}
			
		ZosBatchJobImpl zosBatchJobImpl;
		try {
			zosBatchJobImpl = new ZosBatchJobImpl(image, jobname, jcl);
		} catch (ZosBatchManagerException e) {
			throw new ZosBatchException("Unable to submit batch job", e);
		}
				
		return zosBatchJobImpl.submitJob();
	}
}
