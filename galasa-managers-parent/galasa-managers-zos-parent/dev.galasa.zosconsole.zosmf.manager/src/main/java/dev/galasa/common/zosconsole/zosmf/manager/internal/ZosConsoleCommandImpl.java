package dev.galasa.common.zosconsole.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.common.zos.IZosImage;
import dev.galasa.common.zos.ZosManagerException;
import dev.galasa.common.zosconsole.IZosConsoleCommand;
import dev.galasa.common.zosconsole.ZosConsoleException;
import dev.galasa.common.zosconsole.ZosConsoleManagerException;
import dev.galasa.common.zosconsole.zosmf.manager.internal.properties.RequestRetry;
import dev.galasa.common.zosmf.IZosmf;
import dev.galasa.common.zosmf.IZosmfResponse;
import dev.galasa.common.zosmf.ZosmfException;
import dev.galasa.common.zosmf.ZosmfManagerException;
import dev.galasa.common.zosmf.ZosmfRequestType;

/**
 * Implementation of {@link IZosConsoleCommand} using zOS/MF
 *
 */
public class ZosConsoleCommandImpl implements IZosConsoleCommand {
	
	private String imageId;
	private String consoleName;
	private String command;	
	private String commandImmediateResponse;
	private String commandResponseKey;
	private String commandDelayedResponse = "";
	private int commandErrorReturnCode;
	private int commandErrorReasonCode;
	
	private int retryRequest;
	private IZosmf currentZosmf;
	private String currentZosmfImageId;
	private final HashMap<String, IZosmf> zosmfs = new LinkedHashMap<>();
	private static final String RESTCONSOLE_PATH = "/zosmf/restconsoles/consoles/";
	private static final String USE_DEFAULT_CONSOLE_NAME = "defcn";
	
	private static final Log logger = LogFactory.getLog(ZosConsoleCommandImpl.class);

	public ZosConsoleCommandImpl(@NotNull String command, String consoleName, IZosImage image) throws ZosConsoleException {
		this.imageId = image.getImageID();
		this.consoleName = setConsoleName(consoleName);
		this.command = command;
		try {
			this.retryRequest = RequestRetry.get(this.imageId);
		} catch (ZosConsoleManagerException e) {
			throw new ZosConsoleException("Unable to get request retry property value", e);
		}
		
		try {
			this.zosmfs.putAll(ZosConsoleManagerImpl.zosmfManager.getZosmfs(image.getClusterID()));
		} catch (ZosManagerException e) {
			throw new ZosConsoleException("Unable to create new zOSMF objects", e);
		}
		
		this.currentZosmfImageId = this.zosmfs.entrySet().iterator().next().getKey();
		this.currentZosmf = this.zosmfs.get(this.currentZosmfImageId);
	}

	public @NotNull IZosConsoleCommand issueCommand() throws ZosConsoleException {
		JsonObject requestBody = new JsonObject();
		requestBody.addProperty("cmd", this.command);
		requestBody.addProperty("system", this.imageId);
		
		IZosmfResponse response = sendRequest(ZosmfRequestType.PUT_JSON, RESTCONSOLE_PATH + this.consoleName, requestBody, 
				new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		if (response == null || response.getStatusCode() == 0) {
			throw new ZosConsoleException("Unable to issue console command \"" + this.command + "\"");
		}
		
		JsonObject content;
		try {
			content = response.getJsonContent();
		} catch (ZosmfException e) {
			throw new ZosConsoleException("Unable to issue console command \"" + this.command + "\"");
		}
		
		logger.trace(content);
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			this.commandImmediateResponse = content.get("cmd-response").getAsString();
			this.commandResponseKey = content.get("cmd-response-key").getAsString();
		} else {
			// Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
			this.commandImmediateResponse = content.get("reason").getAsString();
			this.commandErrorReturnCode = content.get("return-code").getAsInt();
			this.commandErrorReasonCode = content.get("reason-code").getAsInt();
			logger.error("Command \"" + this.command + "\" failed. Reason=" + this.commandImmediateResponse + 
					" Return Code=" + this.commandErrorReturnCode + " Reason Code=" + this.commandErrorReasonCode);
			throw new ZosConsoleException("Console command \"" + this.command + "\" failed. Reason \"" + this.commandImmediateResponse + "\"");
		}
		logger.info("Command " + this + " issued");
				
		return this;
	}

	@Override
	public String getResponse() throws ZosConsoleException {
		return this.commandImmediateResponse;
	}

	@Override
	public String requestResponse() throws ZosConsoleException {
		IZosmfResponse response = sendRequest(ZosmfRequestType.GET, RESTCONSOLE_PATH + this.consoleName + "/solmsgs/" + this.commandResponseKey, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
		if (response == null || response.getStatusCode() == 0 || response.getStatusCode() != HttpStatus.SC_OK) {
			//Retrieve 
			throw new ZosConsoleException("Unable to retrieve console response for command \"" + this.command + "\"");
		}
		
		if (response.getStatusCode() == HttpStatus.SC_OK) {
			JsonObject content;
			try {
				content = response.getJsonContent();
			} catch (ZosmfException e) {
				throw new ZosConsoleException("Unable to issue console command \"" + this.command + "\"");
			}
		
			logger.trace(content);
			this.commandDelayedResponse = content.get("cmd-response").getAsString();
		}
		return this.commandDelayedResponse;
	}

	@Override
	public String getCommand() {
		return this.command;
	}

	private String setConsoleName(String consoleName) throws ZosConsoleException {
		if (consoleName == null) {
			return USE_DEFAULT_CONSOLE_NAME;
		}
		if (consoleName.length() < 2 || consoleName.length() > 8) {
			throw new ZosConsoleException("Invalid console name \"" + consoleName + "\" must be between 2 and 8 charaters long");
		}
		return null;
	}
	
	private IZosmfResponse sendRequest(ZosmfRequestType requestType, String path, Object body, List<Integer> validStatusCodes) throws ZosConsoleException {
		if (validStatusCodes == null) {
			validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
		}
		IZosmfResponse response = null;
		for (int i = 0; i <= this.retryRequest; i++) {
			try {
				switch (requestType) {
				case PUT_TEXT:
					response = getCurrentZosmfServer().putText(path, (String) body, validStatusCodes);
					break;
				case PUT_JSON:
					response = getCurrentZosmfServer().putJson(path, (JsonObject) body, validStatusCodes);
					break;
				case GET:
					response = getCurrentZosmfServer().get(path, validStatusCodes);
					break;
				case DELETE:
					response = getCurrentZosmfServer().delete(path, validStatusCodes);
					break;
				default:
					throw new ZosConsoleException("Invalid request type");
				}

				if (response == null || validStatusCodes.contains(response.getStatusCode())) {
			    	return response;
				} else {
					logger.error("Expected HTTP status codes: " + validStatusCodes);
			    	getNextZosmf();
				}
			} catch (ZosmfManagerException e) {
		    	logger.error(e);
		    	getNextZosmf();
			}
		}
		return response;
	}

	private IZosmf getCurrentZosmfServer() {
		logger.info("Using zOSMF on " + this.currentZosmf);
		return this.currentZosmf;
	}

	private void getNextZosmf() {
		if (this.zosmfs.size() == 1) {
			logger.debug("Only one zOSMF server available");
			return;
		}
		Iterator<Entry<String, IZosmf>> zosmfsIterator = this.zosmfs.entrySet().iterator();
		while (zosmfsIterator.hasNext()) {
			if (zosmfsIterator.next().getKey().equals(this.currentZosmfImageId)) {
				Entry<String, IZosmf> entry;
				if (zosmfsIterator.hasNext()) {
					entry = zosmfsIterator.next();
				} else {
					entry = this.zosmfs.entrySet().iterator().next();
				}
				this.currentZosmfImageId = entry.getKey();
				this.currentZosmf = this.zosmfs.get(this.currentZosmfImageId);
				return;
			}
		}
		logger.debug("No alternate zOSMF server available");
	}

	@Override
	public String toString() {
		return "COMMAND=" + this.command + (this.imageId != null ? " IMAGE=" +  this.imageId : ""
			              + (this.commandImmediateResponse != null ? " RESPONSE:\n " + this.commandImmediateResponse : ""));
	}
}
