/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq;

import javax.jms.Message;
import javax.jms.TextMessage;

public interface IMessageQueue {
	
	public TextMessage getNewTextMessage();
	public Message getNewMessage();
	
	public void sendMessage(Message message);
	
	public Message receiveMessage();
	public Message receiveMessage(long timeout);
	public Message receiveMessageNoWait();
}
