package dev.galasa.zos3270.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.IScreenUpdateListener;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.internal.properties.ApplyConfidentialTextFiltering;

public class Zos3270TerminalImpl extends Terminal implements IScreenUpdateListener {
	
	private Log    logger = LogFactory.getLog(getClass());
	
	private final String terminalId;
	private long         updateId;

	private final IConfidentialTextService cts;
	private final boolean applyCtf;
	
	public Zos3270TerminalImpl(String id, String host, int port, boolean tls, IFramework framework) throws Zos3270ManagerException {
		super(host, port, tls);
		this.terminalId = id;
		
		this.cts = framework.getConfidentialTextService();
		this.applyCtf = ApplyConfidentialTextFiltering.get();
		
		getScreen().registerScreenUpdateListener(this);
		
	}

	@Override
	public synchronized void screenUpdated(Direction direction, AttentionIdentification aid) {
		String update = terminalId + "-" + (updateId++);
		
		String screenData = getScreen().printScreenTextWithCursor();
		if (applyCtf) {
			screenData = cts.removeConfidentialText(screenData);
		}
		
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
