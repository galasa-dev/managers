/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq;

import javax.jms.Message;

public interface IMessageQueue {
	
	/**
	 * puts the provided message onto the queue
	 * @param message the message to send
	 */
	public void sendMessage(Message message);
	
	/**
	 * Retrieves the first message from the queue, waiting until a message is ready
	 * @return the message from the queue
	 */
	public Message getMessage();
	
	/**
	 * Retrieves the first message from the queue, waiting for the timeout
	 * @param timeout - long timeout in milliseconds
	 * @return the message - or null if no message is available before timeout
	 */
	public Message getMessage(long timeout);
	
	/**
	 * Retrieves the first message from the queue returning immediately if no message is available
	 * @return the message from the queue or null
	 */
	public Message getMessageNoWait();
	
	/**
	 * Clears the queue by consuming messages until queue is empty
	 * messages consumed are not archived regardless of the '@queue' annotation
	 */
	public void clearQueue();
}
