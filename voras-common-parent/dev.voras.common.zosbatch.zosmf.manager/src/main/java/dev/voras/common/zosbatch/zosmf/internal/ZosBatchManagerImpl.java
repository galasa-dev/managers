package dev.voras.common.zosbatch.zosmf.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.voras.ManagerException;
import dev.voras.common.http.IHttpManager;
import dev.voras.common.zos.IZosManager;
import dev.voras.common.zos.ZosManagerException;
import dev.voras.common.zosbatch.IZosBatch;
import dev.voras.common.zosbatch.IZosBatchJobname;
import dev.voras.common.zosbatch.ZosBatch;
import dev.voras.common.zosbatch.ZosBatchField;
import dev.voras.common.zosbatch.ZosBatchJobname;
import dev.voras.framework.spi.AbstractManager;
import dev.voras.framework.spi.AnnotatedField;
import dev.voras.framework.spi.IConfigurationPropertyStoreService;
import dev.voras.framework.spi.IDynamicStatusStoreService;
import dev.voras.framework.spi.IFramework;
import dev.voras.framework.spi.IManager;
import dev.voras.framework.spi.ResourceUnavailableException;

@Component(service = { IManager.class })
public class ZosBatchManagerImpl extends AbstractManager {
	protected static final String NAMESPACE = "zosbatch";

	private static final Log logger = LogFactory.getLog(ZosBatchManagerImpl.class);

	protected static ZosBatchProperties zosBatchProperties;
	public static void setZosBatchProperties(ZosBatchProperties zosBatchProperties) {
		ZosBatchManagerImpl.zosBatchProperties = zosBatchProperties;
	}

	private IConfigurationPropertyStoreService cps;
	private IDynamicStatusStoreService dss;
	
	protected static IZosManager zosManager;
	public static void setZosManager(IZosManager zosManager) {
		ZosBatchManagerImpl.zosManager = zosManager;
	}

	protected static IHttpManager httpManager;	
	public static void setHttpManager(IHttpManager httpManager) {
		ZosBatchManagerImpl.httpManager = httpManager;
	}

	private final HashMap<String, ZosBatchImpl> taggedZosBatches = new HashMap<>();
	
	/* (non-Javadoc)
	 * @see dev.voras.framework.spi.AbstractManager#initialise(dev.voras.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, testClass);
		setZosBatchProperties(new ZosBatchProperties(framework));

		//*** Check to see if any of our annotations are present in the test class
		//*** If there is,  we need to activate
		List<AnnotatedField> ourFields = findAnnotatedFields(ZosBatchField.class);
		if (!ourFields.isEmpty()) {
			youAreRequired(allManagers, activeManagers);
		}

		try {
			this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
			this.cps = framework.getConfigurationPropertyService(NAMESPACE);
		} catch (Exception e) {
			throw new ZosManagerException("Unable to request framework services", e);
		}
	}
	

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
			throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
		setZosManager(addDependentManager(allManagers, activeManagers, IZosManager.class));
		if (zosManager == null) {
			throw new ZosManagerException("The zOS Manager is not available");
		}
		setHttpManager(addDependentManager(allManagers, activeManagers, IHttpManager.class));
		if (httpManager == null) {
			throw new ZosManagerException("The HTTP Manager is not available");
		}
	}
	
	
	/* (non-Javadoc)
	 * @see dev.voras.framework.spi.AbstractManager#provisionGenerate()
	 */
	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		// Get all our annotated fields
		List<AnnotatedField> annotatedFields = findAnnotatedFields(ZosBatchField.class);

		// Process annotations
		Iterator<AnnotatedField> annotatedFieldIterator = annotatedFields.iterator();
		while(annotatedFieldIterator.hasNext()) {
			AnnotatedField annotatedField = annotatedFieldIterator.next();
			final Field field = annotatedField.getField();

			if (field.getType() == IZosBatch.class) {
				IZosBatch zosBatch = generateZosBatch(field);
				registerAnnotatedField(field, zosBatch);
			}
			if (field.getType() == IZosBatchJobname.class) {
				IZosBatchJobname zosBatchJobname = generateZosBatchJobname(field);
				registerAnnotatedField(field, zosBatchJobname);
			}
			
		}
	}
	
	
	private IZosBatch generateZosBatch(Field field) {
		ZosBatch annotationZosBatch = field.getAnnotation(ZosBatch.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationZosBatch.imageTag(), "primary");

		//*** Have we already generated this tag
		if (taggedZosBatches.containsKey(tag)) {
			return taggedZosBatches.get(tag);
		}

		IZosBatch zosBatch = new ZosBatchImpl();
		taggedZosBatches.put(tag, (ZosBatchImpl) zosBatch);
		
		return zosBatch;
	}
	
	
	private IZosBatchJobname generateZosBatchJobname(Field field) throws ZosManagerException {
		ZosBatchJobname annotationZosBatchJobname = field.getAnnotation(ZosBatchJobname.class);

		//*** Default the tag to primary
		String tag = defaultString(annotationZosBatchJobname.imageTag(), "primary");
		return new ZosBatchJobnameImpl(tag);
	}
}
