/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.mq.IMessageQueue;

public class MessageQueueImpl implements IMessageQueue {
	
	//Fields received from the constructor
	private String queueName;
	private MessageQueueManagerImpl qmgr;
	private boolean archive;
	private MQManagerImpl manager;
	
	private static String QUEUE_PROTOCOL = "queue:///";
	private static String RAS_TOP_LEVEL = "messages";
	
	//Generated Fields
	private Destination destination;
	private JMSConsumer consumer;
	private JMSProducer producer;
	
	//Internal State
	private boolean started = false;
	private String currentMethod = new String();
	private int numberOfMessagesLoggedInThisMethod = 1;
	
	private static final Log  logger = LogFactory.getLog(MessageQueueImpl.class);
	
	public MessageQueueImpl(String name, MessageQueueManagerImpl qmgr, boolean archive, MQManagerImpl manager) {
		this.queueName = name;
		this.qmgr = qmgr;
		this.archive = archive;
		this.manager = manager;
	}
	
	/**
	 * Starts the connection to the queue by obtaining the context from the qmgr
	 * If we have already been started then just exit
	 */
	public void startup() {
		if(started) {
			logger.info("Connection to queue: " + this.queueName + " already started");
			return;
		}
		JMSContext context = qmgr.getContext();
		destination = context.createQueue(QUEUE_PROTOCOL + this.queueName);
		producer = context.createProducer();
		consumer = context.createConsumer(destination);
		this.started = true;
		logger.info("Connection to queue: " + this.queueName + " complete");
	}

	@Override
	public void sendMessage(Message message) {
		archiveMessage(message,MessageDirection.OUTBOUND);
		producer.send(destination, message);
	}

	@Override
	public Message getMessage() {
		Message m = consumer.receive();
		archiveMessage(m, MessageDirection.INBOUND);
		return m;
	}

	@Override
	public Message getMessage(long timeout) {
		Message m = consumer.receive(timeout);
		archiveMessage(m, MessageDirection.INBOUND);
		return m;
	}

	@Override
	public Message getMessageNoWait() {
		Message m = consumer.receiveNoWait();
		archiveMessage(m, MessageDirection.INBOUND);
		return m;
	}
	
	@Override
	public void clearQueue() {
		while(consumer.receiveNoWait() != null) {}
	}
	
	/**
	 * If archival has been set in the annotation for this queue then 
	 * we archive this message in the RAS in the structure:
	 * 
	 * messages
	 * |
	 * ---<method name>
	 *      |
	 *      ---<queue name>
	 *           |
	 *           ---<inbound/outbound>
	 *               |
	 *               ---message: <id>
	 * @param m the message we are archiving
	 * @param direction the direction in which this message is travelling from the perspecitive of the test
	 */
	private void archiveMessage(Message m, MessageDirection direction) {
		if(m == null || !archive)
			return;
		Path folder = manager.getStoredArtifactRoot()
							 .resolve(RAS_TOP_LEVEL)
							 .resolve(getCurrentMethod())
							 .resolve(this.queueName)
							 .resolve(direction.toString().toLowerCase())
							 .resolve("message:" + Integer.toString(numberOfMessagesLoggedInThisMethod));
		try {
			Files.write(folder, m.getBody(String.class).getBytes(), StandardOpenOption.CREATE);
		} catch (Exception e) {
			logger.info("Unable to log message for a queue", e);
		} 
		this.numberOfMessagesLoggedInThisMethod++;
	}
	
	/**
	 * Internal check that the current method is the same as the 
	 * method that we last checked  This allows us to create an index of the 
	 * messages we have archived
	 * @return
	 */
	private String getCurrentMethod() {
		String managerCurrentMethod = this.manager.getCurrentMethod();
		if(this.currentMethod.equals(managerCurrentMethod)) {
			return currentMethod;
		}else {
			this.currentMethod = managerCurrentMethod;
			this.numberOfMessagesLoggedInThisMethod = 1;
		}
		return this.currentMethod;
	}

	@Override
	public void sendBytes(byte[] messageBytes) {
		// TODO Auto-generated method stub
		
	}
}
