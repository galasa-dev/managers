/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.db2.Db2Instance;
import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.Db2ManagerField;
import dev.galasa.db2.Db2Schema;
import dev.galasa.db2.IDb2Instance;
import dev.galasa.db2.IDb2Schema;
import dev.galasa.db2.internal.properties.Db2PropertiesSingleton;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class Db2ManagerImpl extends AbstractManager{
	private IFramework 							framework;
	
	private Map<String,Db2InstanceImpl> 					connections = new HashMap<>();
	
	protected final String               		NAMESPACE = "db2";
    private final static Log                    logger = LogFactory.getLog(Db2ManagerImpl.class);
    
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
    	super.initialise(framework, allManagers, activeManagers, galasaTest);
    	this.framework = framework;
    	
    	if(galasaTest.isJava()) {
    		List<AnnotatedField> annotatedFields = findAnnotatedFields(Db2ManagerField.class);
    		if(!annotatedFields.isEmpty()) {
    			youAreRequired(allManagers, activeManagers, galasaTest);
    		}
    	}
    	try {
    		Db2PropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
    	} catch(ConfigurationPropertyStoreException e) {
    		throw new Db2ManagerException("Failed to set CPS for the 'db2' namespace", e);
    	}
    	logger.info("Db2 manager initialised");
    }

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
	        throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}
        activeManagers.add(this);
	}
	
	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		logger.info("Db2 Manager provision Generating");
		List<AnnotatedField> annotatedFields = findAnnotatedFields(Db2ManagerField.class);
    	
    	//First generate the queue managers
    	for(AnnotatedField annotatedField : annotatedFields) {
    		Field field = annotatedField.getField();
    		if(field.getType() == IDb2Instance.class) {
    			Db2Instance annotation = field.getAnnotation(Db2Instance.class);
    			if(annotation != null) {
    				generateDb2Instance(field, annotatedField.getAnnotations());
    			}
    			
    		}
    	}
        generateAnnotatedFields(Db2ManagerField.class);
	}
	
	@GenerateAnnotatedField(annotation = Db2Instance.class)
	public IDb2Instance generateDb2Instance(Field field, List<Annotation> annotations) throws Db2ManagerException {
		Db2Instance annotation = field.getAnnotation(Db2Instance.class);
		
		Db2InstanceImpl instance = new Db2InstanceImpl(framework, this, annotation.tag());
		connections.put(annotation.tag(), instance);
		registerAnnotatedField(field, instance);
		return instance;
	}
	
	@GenerateAnnotatedField(annotation = Db2Schema.class)
	public IDb2Schema generateDb2Schema(Field field, List<Annotation> annotations) throws Db2ManagerException {
		Db2Schema annotation = field.getAnnotation(Db2Schema.class);
		
		Db2InstanceImpl conn = connections.get(annotation.db2Tag());
		if (conn == null) {
			throw new Db2ManagerException("Requested db2 connection not valid. Please define the Db2 instance tagged: " + annotation.tag());
		}
		
		IDb2Schema schema = new Db2SchemaImpl(framework, conn, annotation.tag(), annotation.archive(), annotation.resultSetType(), annotation.resultSetConcurrency());
		registerAnnotatedField(field, schema);
		return schema;
	}
}