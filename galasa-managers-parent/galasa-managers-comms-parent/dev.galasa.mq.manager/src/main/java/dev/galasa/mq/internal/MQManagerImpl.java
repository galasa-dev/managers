/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.mq.IMessageQueue;
import dev.galasa.mq.IMessageQueueManager;
import dev.galasa.mq.MqManagerException;
import dev.galasa.mq.MqManagerField;
import dev.galasa.mq.Queue;
import dev.galasa.mq.QueueManager;
import dev.galasa.mq.internal.properties.InstanceChannelName;
import dev.galasa.mq.internal.properties.InstanceForTag;
import dev.galasa.mq.internal.properties.InstanceHost;
import dev.galasa.mq.internal.properties.MqPropertiesSingleton;
import dev.galasa.mq.internal.properties.InstanceName;
import dev.galasa.mq.internal.properties.InstancePort;

@Component(service = { IManager.class })
public class MQManagerImpl extends AbstractManager {

    private static final Log  logger = LogFactory.getLog(MQManagerImpl.class);
    private static final String NAMESPACE   = "mq";
    private Path storedArtifactsRoot;
    private ICredentialsService credentialService;
    
    private HashMap<String,MessageQueueManagerImpl> queueManagers = new HashMap<>();
    private List<MessageQueueImpl> queues = new ArrayList<>();

    @GenerateAnnotatedField(annotation = Queue.class)
    public IMessageQueue generateMessageQueue(Field field, List<Annotation> annotations) throws MqManagerException {
    	Queue annotation = field.getAnnotation(Queue.class);
    	
    	//check that we have provisioned the qmgr for this queue
    	String qmgrTag = annotation.queueMgrTag();
    	if(!queueManagers.containsKey(qmgrTag)) {
    		throw new MqManagerException("Unable to provision queue: " + annotation.name() + " no QMGR found tagged: " + qmgrTag);
    	}
    	
    	//construct the queue and add it to our list
        MessageQueueImpl queue = new MessageQueueImpl(annotation.name(),queueManagers.get(qmgrTag), Boolean.parseBoolean(annotation.archive()), storedArtifactsRoot, logger);
        this.queues.add(queue);
        return queue;
    }
    
    @GenerateAnnotatedField(annotation = QueueManager.class)
    public IMessageQueueManager generateMessageQueueManager(Field field, List<Annotation> annotations) throws MqManagerException{
    	//obtain the tag for this queue manager
    	QueueManager annotation = field.getAnnotation(QueueManager.class);
    	String tag = annotation.queueMgrTag();
		
		//obtain the configuration for this queue manager
		//obtain the instanceid for the tag
		logger.info("Obtaining instance ID for tag: " + tag);
		String instanceid = InstanceForTag.get(tag);
		
		logger.info("Obtaining configuration information for instance: " + instanceid);
		String host = InstanceHost.get(instanceid);
		int port = InstancePort.get(instanceid);
		String channel = InstanceChannelName.get(instanceid);
		String name = InstanceName.get(instanceid);
		
		//pull the access credentials
    	ICredentials credentials;
    	String credentialsKey = NAMESPACE+"."+instanceid;
    	logger.info("Obtaining credentials for namespace: " + credentialsKey);
		try {
			credentials = credentialService.getCredentials(credentialsKey);
		} catch (CredentialsException e) {
			throw new MqManagerException("Unable to locate credentials for MQ Queue Manager with tag: " + tag);
		}
		
		if(credentials == null) {
			throw new MqManagerException("Unable to obtain credentials for namespace: " + credentialsKey);
		}
		
		//construct the queue manager and add it to the list
        MessageQueueManagerImpl qmgr = new MessageQueueManagerImpl(host, port, channel, name,(ICredentialsUsernamePassword)credentials,logger);
        this.queueManagers.put(tag, qmgr);
        return qmgr;
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    	List<AnnotatedField> annotatedFields = findAnnotatedFields(MqManagerField.class);
    	
    	//First generate the queue managers
    	for(AnnotatedField af : annotatedFields) {
    		Field f = af.getField();
    		if(f.getType() == IMessageQueueManager.class) {
    			QueueManager annotation = f.getAnnotation(QueueManager.class);
    			if(annotation != null) {
    				IMessageQueueManager qmgr = generateMessageQueueManager(f, af.getAnnotations());
    				registerAnnotatedField(f, qmgr);
    			}
    			
    		}
    	}
    	
    	//Now generate the queues
    	for(AnnotatedField af : annotatedFields) {
    		Field f = af.getField();
    		if(f.getType() == IMessageQueue.class) {
    			Queue annotation = f.getAnnotation(Queue.class);
    			if(annotation != null) {
    				IMessageQueue queue = generateMessageQueue(f, af.getAnnotations());
    				registerAnnotatedField(f, queue);
    			}
    		}
    	}
    	
        generateAnnotatedFields(MqManagerField.class);
    }

    @Override
    public void shutdown() {
    	
    }

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
			MqPropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
		} catch (ConfigurationPropertyStoreException e1) {
			throw new MqManagerException("Unable to access framework services", e1); 	
		}
        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(MqManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
        
        this.storedArtifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        try {
			this.credentialService = getFramework().getCredentialsService();
		} catch (CredentialsException e) {
			throw new MqManagerException("Unable to access credentials service",e);
		}
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
    public boolean doYouSupportSharedEnvironments() {
        return true;   // this manager does not provision resources, therefore support environments 
    }
    
    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {
    	/*
    	 * This method of starting the qmgrs and then the queues is not the most effective
    	 * If we had a test with lots of qmgrs and queues then it would be better to 
    	 * start the qmgrs async and on completion start the required queues, however this 
    	 * is unlikely to be a real issue
    	 */
    	//start the queue managers first 
    	for(Entry<String, MessageQueueManagerImpl> entry : this.queueManagers.entrySet()) {
    		MessageQueueManagerImpl qmgr = entry.getValue();
    		try {
    			qmgr.startup();
    		}catch(MqManagerException mqme) {
    			logger.error("Unable to start Queue Manager");
    			throw mqme;
    		}	
    	}
    	
    	//Now that the queue managers are active start the queues
    	for(MessageQueueImpl queue : this.queues) {
    		queue.startup();
    	}
    	
    }
    
    @Override
    public void provisionStop() {
    	for(Entry<String, MessageQueueManagerImpl> entry : this.queueManagers.entrySet()) {
    		MessageQueueManagerImpl qmgr = entry.getValue();
    		qmgr.close();	
    	}
    }
    
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
    	super.startOfTestMethod(galasaMethod);
    	for(MessageQueueImpl queue : queues) {
    		queue.startOfNewMethod(galasaMethod.getJavaExecutionMethod().getName());
    	}
    }

}
