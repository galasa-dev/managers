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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.mq.IMessageQueue;

public class MessageQueueImpl implements IMessageQueue {
	
	//Fields received from the constructor
	private String queueName;
	private MessageQueueManagerImpl qmgr;
	private boolean archive;
	private MQManagerImpl manager;
	
	//Fields received from MessageQueueManagerImpl
	private JMSContext context;
	
	private static String QUEUE_PROTOCOL = "queue:///";
	private static String RAS_TOP_LEVEL = "messages";
	
	//Generated Fields
	private Destination destination;
	private JMSConsumer consumer;
	private JMSProducer producer;
	
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
	
	public void startup() {
		if(started) {
			logger.info("Connection to queue: " + this.queueName + " already started");
			return;
		}
		this.context = qmgr.getContext();
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
}
