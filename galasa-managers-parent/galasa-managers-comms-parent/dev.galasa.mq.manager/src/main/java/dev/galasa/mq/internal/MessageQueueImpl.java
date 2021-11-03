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

import dev.galasa.mq.IMessageQueue;

public class MessageQueueImpl implements IMessageQueue {
	
	//Fields received from the constructor
	private String queueName;
	private MessageQueueManagerImpl qmgr;
	private boolean archive;
	private Path storedArtifactsRoot;
	private Log logger;
	
	//Fields received from MessageQueueManagerImpl
	private JMSContext context;
	private String currentMethodName;
	private int numberOfMessagesLoggedInThisMethod = 0;
	
	private static String QUEUE_PROTOCOL = "queue:///";
	
	//Generated Fields
	private Destination destination;
	private JMSConsumer consumer;
	private JMSProducer producer;
	
	public MessageQueueImpl(String name, MessageQueueManagerImpl qmgr, boolean archive, Path storedArtifactRoot, Log log) {
		this.queueName = name;
		this.qmgr = qmgr;
		this.archive = archive;
		this.storedArtifactsRoot = storedArtifactRoot;
		this.logger = log;
	}
	
	public void startup() {
		logger.info("Starting a connection to queue: " + this.queueName + " on queue manager: " + qmgr.getName());
		this.context = qmgr.getContext();
		destination = context.createQueue(QUEUE_PROTOCOL + this.queueName);
		producer = context.createProducer();
		consumer = context.createConsumer(destination);
	}

	@Override
	public TextMessage getNewTextMessage() {
		return context.createTextMessage();
	}

	@Override
	public Message getNewMessage() {
		return context.createMessage();
	}

	@Override
	public void sendMessage(Message message) {
		logMessage(message,MessageDirection.OUTBOUND);
		producer.send(destination, message);
	}

	@Override
	public Message receiveMessage() {
		Message m = consumer.receive();
		logMessage(m, MessageDirection.INBOUND);
		return m;
	}

	@Override
	public Message receiveMessage(long timeout) {
		Message m = consumer.receive(timeout);
		logMessage(m, MessageDirection.INBOUND);
		return m;
	}

	@Override
	public Message receiveMessageNoWait() {
		Message m = consumer.receiveNoWait();
		logMessage(m, MessageDirection.INBOUND);
		return m;
	}
	
	private void logMessage(Message m, MessageDirection direction) {
		if(m == null || !archive)
			return;
		Path folder = storedArtifactsRoot.resolve(currentMethodName)
										 .resolve(direction.toString())
										 .resolve(Integer.toString(numberOfMessagesLoggedInThisMethod));
		try {
			Files.write(folder, m.getBody(String.class).getBytes(), StandardOpenOption.CREATE);
		} catch (Exception e) {
			logger.info("Unable to log message for a queue", e);
		} 
		this.numberOfMessagesLoggedInThisMethod++;
	}
	
	public void startOfNewMethod(String methodName) {
		this.currentMethodName = methodName;
		this.numberOfMessagesLoggedInThisMethod = 0;
	}

}
