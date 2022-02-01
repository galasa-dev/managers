/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.db2.internal;

import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.db2.Db2ManagerException;
import dev.galasa.db2.Db2ManagerField;
import dev.galasa.db2.IDb2;
import dev.galasa.db2.IDb2Schema;
import dev.galasa.db2.internal.properties.Db2PropertiesSingleton;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class Db2ManagerImpl extends AbstractManager{
	protected final String               		NAMESPACE = "db2";
    private final static Log                    logger = LogFactory.getLog(Db2ManagerImpl.class);
    
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
    	super.initialise(framework, allManagers, activeManagers, galasaTest);
    	
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
	        throws Db2ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}
        activeManagers.add(this);
	}
	
	@Override
	public void provisionGenerate() throws ManagerException {
		logger.info("Db2 Manager provision Generating");
		generateDb2Fields();
	}
	
	// Creates Stubs for now
	private void generateDb2Fields() {
		List<AnnotatedField> annotatedFields = findAnnotatedFields(Db2ManagerField.class);
		
		for (AnnotatedField annotatedField: annotatedFields) {
			Field field = annotatedField.getField();
			
			if (field.getType() == IDb2.class) {
				IDb2 db2 = new Db2Impl();
				registerAnnotatedField(field, db2);
			} else if (field.getType() == IDb2Schema.class) {
				IDb2Schema schema = new Db2SchemaImpl();
				registerAnnotatedField(field, schema);
			}
		}
	}
}