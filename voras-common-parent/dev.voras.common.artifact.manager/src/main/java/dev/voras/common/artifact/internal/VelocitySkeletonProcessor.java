package dev.voras.common.artifact.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import dev.voras.common.artifact.ISkeletonProcessor;
import dev.voras.common.artifact.SkeletonProcessorException;
import dev.voras.framework.spi.IFramework;


public class VelocitySkeletonProcessor  implements ISkeletonProcessor {
	
	private final static Log logger = LogFactory.getLog(VelocitySkeletonProcessor.class);
	

	public VelocitySkeletonProcessor(IFramework framework) {
	}

	@Override
	public InputStream processSkeleton(InputStream skeleton, Map<String, Object> parameters) throws SkeletonProcessorException {
		
		return processVelocitySkeleton(skeleton, parameters);		
	}
	
	private InputStream processVelocitySkeleton(InputStream skeleton, Map<String, Object> parameters) throws SkeletonProcessorException {
		
		logger.info("Processing skeleton with Velocity");
		
		InputStream safeEOF = new ByteArrayInputStream(new String(" ").getBytes());
		InputStream streamPlus = new SequenceInputStream(skeleton, safeEOF);
		InputStreamReader ir = new InputStreamReader(streamPlus);
		
		try {
			Velocity.init();
		} catch (Exception e) {
			throw new SkeletonProcessorException("Error attempting to initialise velocity", e);
		}

		VelocityContext context = new VelocityContext();

		//Supplied parameters will override our defaults
		for(Entry<String, Object> entry : parameters.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter ow = new OutputStreamWriter(baos);

		try {
			Velocity.evaluate(context, ow, "VelocityRenderer", ir);
			ow.close();
		} catch (Exception e) {
			throw new SkeletonProcessorException("Error attempting to process skeleton with velocity", e);
		}


		return new ByteArrayInputStream(baos.toByteArray());		
	}
}
