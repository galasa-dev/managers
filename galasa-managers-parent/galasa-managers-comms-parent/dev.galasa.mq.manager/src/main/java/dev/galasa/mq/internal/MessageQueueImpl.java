package dev.galasa.mq.internal;

import javax.jms.Message;
import javax.jms.TextMessage;

import dev.galasa.mq.IMessageQueue;

public class MessageQueueImpl implements IMessageQueue {

	@Override
	public TextMessage getNewTextMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message getNewMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Message receiveMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message receiveMessage(long timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message receiveMessageNoWait() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setArchiveMessages(boolean archive) {
		// TODO Auto-generated method stub
		
	}

}
