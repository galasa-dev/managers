package dev.voras.common.zosmf.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.voras.ManagerException;
import dev.voras.common.http.spi.IHttpManagerSpi;
import dev.voras.common.zos.IZosImage;
import dev.voras.common.zos.ZosManagerException;
import dev.voras.common.zos.spi.IZosManagerSpi;
import dev.voras.common.zosmf.IZosmf;
import dev.voras.common.zosmf.Zosmf;
import dev.voras.common.zosmf.ZosmfException;
import dev.voras.common.zosmf.ZosmfManagerException;
import dev.voras.common.zosmf.spi.IZosmfManagerSpi;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.AnnotatedField;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IManager;
import dev.voras.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class ZosmfManagerImpl extends AbstractManager implements IZosmfManagerSpi {
	protected static final String NAMESPACE = "zosmf";

	protected static ZosmfProperties zosmfProperties;
	public static void setZosmfProperties(ZosmfProperties zosmfProperties) {
		ZosmfManagerImpl.zosmfProperties = zosmfProperties;
	}

	
	protected static IZosManagerSpi zosManager;
	public static void setZosManager(IZosManagerSpi zosManager) {
		ZosmfManagerImpl.zosManager = zosManager;
	}

	protected static IHttpManagerSpi httpManager;	
	public static void setHttpManager(IHttpManagerSpi httpManager) {
		ZosmfManagerImpl.httpManager = httpManager;
	}

	private final HashMap<String, ZosmfImpl> taggedZosmfs = new HashMap<>();
	
	/* (non-Javadoc)
	 * @see dev.voras.framework.spi.AbstractManager#initialise(dev.voras.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);
		setZosmfProperties(new ZosmfProperties(framework));

		//*** Check to see if any of our annotations are present in the test class
		//*** If there is,  we need to activate
		List<AnnotatedField> ourFields = findAnnotatedFields(ZosmfManagerField.class);
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
		setZosManager(addDependentManager(allManagers, activeManagers, IZosManagerSpi.class));
		if (zosManager == null) {
			throw new ZosManagerException("The zOS Manager is not available");
		}
		setHttpManager(addDependentManager(allManagers, activeManagers, IHttpManagerSpi.class));
		if (httpManager == null) {
			throw new ZosManagerException("The HTTP Manager is not available");
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework
     * .spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
    	return otherManager instanceof IZosManagerSpi ||
    		   otherManager instanceof IHttpManagerSpi;
    }
	
	
	/* (non-Javadoc)
	 * @see dev.voras.framework.spi.AbstractManager#provisionGenerate()
	 */
	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		// Get all our annotated fields
		List<AnnotatedField> annotatedFields = findAnnotatedFields(ZosmfManagerField.class);

		// Process annotations
		Iterator<AnnotatedField> annotatedFieldIterator = annotatedFields.iterator();
		while(annotatedFieldIterator.hasNext()) {
			AnnotatedField annotatedField = annotatedFieldIterator.next();
			final Field field = annotatedField.getField();

			if (field.getType() == IZosmf.class) {
				IZosmf zosmf = generateZosmf(field);
				registerAnnotatedField(field, zosmf);
			}			
		}
	}
	
	
	private IZosmf generateZosmf(Field field) throws ZosmfManagerException {
		Zosmf annotationZosmf = field.getAnnotation(Zosmf.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationZosmf.imageTag(), "primary");

		//*** Have we already generated this tag
		if (taggedZosmfs.containsKey(tag)) {
			return taggedZosmfs.get(tag);
		}

		IZosmf zosmf = new ZosmfImpl(tag);
		taggedZosmfs.put(tag, (ZosmfImpl) zosmf);
		
		return zosmf;
	}


	@Override
	public IZosmf newZosmf(IZosImage image) throws ZosmfException {
		return new ZosmfImpl(image);
	}
}
