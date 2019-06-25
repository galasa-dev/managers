package dev.voras.common.artifact.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.voras.ManagerException;
import dev.voras.common.artifact.ArtifactManager;
import dev.voras.common.artifact.IArtifactManager;
import dev.voras.common.artifact.IBundleResources;
import dev.voras.common.artifact.ISkeletonProcessor;
import dev.voras.common.artifact.ISkeletonProcessor.SkeletonType;
import dev.voras.common.artifact.SkeletonProcessorException;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.AnnotatedField;
import dev.voras.framework.spi.GenerateAnnotatedField;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IManager;
import dev.voras.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class ArtifactManagerImpl extends AbstractManager implements IArtifactManager {

    private static final Log logger = LogFactory.getLog(ArtifactManagerImpl.class);

    @GenerateAnnotatedField(annotation=ArtifactManager.class)
	public IArtifactManager fillField(Field field, List<Annotation> annotations) {
		return this;
	}

	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		generateAnnotatedFields(ArtifactManagerField.class);
	}

	@Override
	public void provisionStop() {
		//Nothing is provisioned by this manager so we don't have anything to stop
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

	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);
		List<AnnotatedField> ourFields = findAnnotatedFields(ArtifactManagerField.class);
		if (!ourFields.isEmpty()) {
			youAreRequired(allManagers, activeManagers);
		}
	}

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
			throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
	}
}