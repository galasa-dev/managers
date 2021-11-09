/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq.internal;

import javax.jms.JMSContext;
import javax.jms.JMSException;

import org.apache.commons.logging.Log;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.mq.IMessageQueueManager;
import dev.galasa.mq.MqManagerException;

public class MessageQueueManagerImpl implements IMessageQueueManager {
	
	private String host;
	private int port;
	private String channel;
	private String name;
	private Log log;
	
	private ICredentialsUsernamePassword credentials;
	
	private JMSContext context;
	
	public MessageQueueManagerImpl(String host, int port, String channel, String name, ICredentialsUsernamePassword credentials, Log log) {
		this.host = host;
		this.port = port;
		this.channel = channel;
		this.name = name;
		this.log = log;
		this.credentials = credentials;
	}
	
	/*
	 * Readies this queue manager for operation
	 * constructs and 
	 */
	public void startup() throws MqManagerException{
		log.info("Starting connection to queue manager: " + this.name);
		try {
			JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
			JmsConnectionFactory cf = ff.createConnectionFactory();

			// Set the properties
			cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
			cf.setIntProperty(WMQConstants.WMQ_PORT, port);
			cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
			cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
			cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, name);
			cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
			cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
//			cf.setStringProperty(WMQConstants.USERID, credentials.getUsername());
//			cf.setStringProperty(WMQConstants.PASSWORD, credentials.getPassword());
			cf.setStringProperty(WMQConstants.USERID, "hobbit");
			cf.setStringProperty(WMQConstants.PASSWORD, "un1cycle");
			//cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, "*TLS12");
			this.context = cf.createContext();
		} catch (JMSException e) {
			throw new MqManagerException("Error while constructing connection to queue manager");
		}
	}

	@Override
	public void close() {
		log.info("Shutting down connection to queue manager: " + this.getName());
		//this.context.close();
		
	}
	
	protected JMSContext getContext() {
		return this.context;
	}
	
	protected String getName() {
		return this.name;
	}

}
