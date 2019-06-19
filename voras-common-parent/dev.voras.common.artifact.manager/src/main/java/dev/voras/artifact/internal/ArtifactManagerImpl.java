package dev.voras.artifact.internal;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.artifact.ArtifactManager;
import dev.voras.artifact.IArtifactManager;
import dev.voras.artifact.IBundleResources;
import dev.voras.artifact.ISkeletonProcessor;
import dev.voras.artifact.OutputRepositoryException;
import dev.voras.artifact.SkeletonProcessorException;
import dev.voras.artifact.ISkeletonProcessor.SkeletonType;
import io.ejat.framework.spi.AbstractManager;
import io.ejat.framework.spi.GenerateAnnotatedField;
import io.ejat.framework.spi.ManagerException;
import io.ejat.framework.spi.ResourceUnavailableException;

public class ArtifactManagerImpl extends AbstractManager implements IArtifactManager {

    private final static Log logger = LogFactory.getLog(ArtifactManagerImpl.class);

    @GenerateAnnotatedField(annotation=ArtifactManager.class)
	public IArtifactManager fillField(Field field, List<Annotation> annotations) {
		return this;
	}

	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		generateAnnotatedFields(ArtifactManager.class);
	}

	@Override
	public void provisionStop() {
	}

	@Override
	public IBundleResources getBundleResources(Class<?> owningClass) {
		return new BundleResourcesImpl(owningClass, this.getFramework());
	}

	@Override
	public ISkeletonProcessor getSkeletonProcessor() {
		return new VelocitySkeletonProcessor(this.getFramework());
	}

	@Override
	public ISkeletonProcessor getSkeletonProcessor(int skeletonType) throws SkeletonProcessorException {
		switch (skeletonType) {
			case SkeletonType.PLUSPLUS:
				return new PlusPlusSkeletonProcessor(this.getFramework());
			case SkeletonType.VELOCITY:
				return new VelocitySkeletonProcessor(this.getFramework());
			default:
				throw new SkeletonProcessorException("SkeletonType '" + skeletonType + "' is not a valid type");
		}
	}
}