package dev.voras.artifact;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import io.ejat.framework.spi.IManager;


/**
 * This is a very simple manager which provides access to objects to assist in retrieving
 * artifacts/resources from the 'resources' directory in your test bundle, and manipulating those resources
 * 
 * @author James Bartlett
 *
 */
public interface IArtifactManager extends IManager  {

	/**
	 * Return an {@link IBundleResources} object to assist in retrieving artifacts from within
	 * a bundles 'resources' directory.
	 * 
	 * @param owningClass - any class within the bundle to be accessed, simply used as a reference to get 
	 * hold of the correct bundle
	 * @return {@link IBundleResources}
	 */
	public IBundleResources getBundleResources(Class<?> owningClass);
	
	/**
	 * Return an {@link ISkeletonProcessor} object to perform substitutions on skeleton files
	 * 
	 * @return {@link ISkeletonProcessor}
	 */
	public ISkeletonProcessor getSkeletonProcessor();
	
	/**
	 * Return an {@link ISkeletonProcessor} object to perform substitutions on skeleton files
	 * Select Type using {@link SkeletonType}
	 * 
	 * @param skeletonType
	 * @return {@link ISkeletonProcessor}
	 * @throws SkeletonProcessorException
	 */
	public ISkeletonProcessor getSkeletonProcessor(int skeletonType) throws SkeletonProcessorException;
	
}
