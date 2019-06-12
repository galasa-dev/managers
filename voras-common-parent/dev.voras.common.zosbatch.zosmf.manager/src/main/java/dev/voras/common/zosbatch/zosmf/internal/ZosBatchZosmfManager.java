package dev.voras.common.zosbatch.zosmf.internal;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.voras.common.zos.IZosImage;
import dev.voras.common.zosbatch.IBatchJob;
import dev.voras.common.zosbatch.IJobname;
import dev.voras.common.zosbatch.IZosBatchManager;
import dev.voras.common.zosbatch.ZosBatchException;

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
