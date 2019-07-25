package dev.voras.common.zos3270.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.voras.common.zos3270.AttentionIdentification;
import dev.voras.common.zos3270.IScreenUpdateListener;

public class Zos3270TerminalImpl extends Terminal implements IScreenUpdateListener {
	
	private Log    logger = LogFactory.getLog(getClass());
	
	private final String terminalId;
	private long         updateId;

	public Zos3270TerminalImpl(String id, String host, int port) {
		super(host, port);
		this.terminalId = id;
		
		getScreen().registerScreenUpdateListener(this);
		
	}

	@Override
	public synchronized void screenUpdated(Direction direction, AttentionIdentification aid) {
		String update = terminalId + "-" + (updateId++);
		
		String screenData = getScreen().printScreenTextWithCursor();
		String aidString;
		if (aid != null) {
			aidString = ", " + aid.toString();
		} else {
			aidString = " update";
		}
		
		logger.debug(direction.toString() + aidString + " to 3270 terminal " + this.terminalId + ",  updateId=" + update + "\n" + screenData);
	}

	public String getId() {
		return this.terminalId;
	}

}
