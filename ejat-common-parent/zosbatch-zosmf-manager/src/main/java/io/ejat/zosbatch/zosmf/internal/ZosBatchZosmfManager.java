package io.ejat.zosbatch.zosmf.internal;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.ejat.zos.IZosImage;
import io.ejat.zosbatch.IBatchJob;
import io.ejat.zosbatch.IJobname;
import io.ejat.zosbatch.IZosBatchManager;
import io.ejat.zosbatch.ZosBatchException;

public class ZosBatchZosmfManager implements IZosBatchManager {

	@Override
	public @NotNull IBatchJob submitJob(@NotNull String jcl, IJobname jobname, @NotNull IZosImage image)
			throws ZosBatchException {
		return new IBatchJob() {
			
			@Override
			public int waitForJob() throws ZosBatchException {
				return 0;
			}
			
			@Override
			public List<String> retrieveOutput() throws ZosBatchException {
				return null;
			}
		};
	}
	

}
