package dev.galasa.mq.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.mq.IMessageQueue;
import dev.galasa.mq.IMessageQueueManager;
import dev.galasa.mq.Queue;
import dev.galasa.mq.QueueManager;


@Test
public class MqManagerIVT {
	
	private static String testData = "Hello World";
	
	@QueueManager(queueMgrTag = "MAIN")
	public IMessageQueueManager qmgr;
	
	@Queue(archive = "true", name = "GALASA.INPUT.QUEUE", queueMgrTag = "MAIN")
	public IMessageQueue queue;

    @Logger
    public Log logger;


    @Test
    public void checkNotNull(){
        assertThat(logger).isNotNull();
    }
    
    @Test
    public void testPutMessage() throws JMSException {
    	TextMessage tm = queue.getNewTextMessage();
    	tm.setText(testData);
    	queue.sendMessage(tm);
    }
    
    @Test
    public void testGetMessage() throws JMSException {
    	Message m = queue.receiveMessage();
    	String response = m.getBody(String.class);
    	assertThat(response).isEqualTo(testData);
    }
    
    @Test
    public void clearQueue() throws JMSException {
    	TextMessage tm = queue.getNewTextMessage();
    	tm.setText(testData);
    	queue.sendMessage(tm);
    	queue.sendMessage(tm);
    	queue.sendMessage(tm);
    	queue.sendMessage(tm);
    	queue.sendMessage(tm);
    	queue.sendMessage(tm);
    	queue.sendMessage(tm);
    	queue.sendMessage(tm);
    	
    	queue.clearQueue();
    	Message m = queue.receiveMessageNoWait();
    	assertThat(m).isNull();
    }
    
}
