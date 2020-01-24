/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.zosmf.manager.internal.properties.RestrictToImage;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * Implementation of {@link IZosConsoleCommand} using zOS/MF
 *
 */
public class ZosConsoleCommandImpl implements IZosConsoleCommand {
    
    IZosmfRestApiProcessor zosmfApiProcessor;
    
    private String imageId;
    private String consoleName;
    private String command;    
    private String commandImmediateResponse;
    private String commandResponseKey;
    private String commandDelayedResponse = "";
    
    private static final String SLASH = "/";
    private static final String RESTCONSOLE_PATH = SLASH + "zosmf" + SLASH + "restconsoles" + SLASH + "consoles" + SLASH;
    private static final String DEFAULT_CONSOLE_NAME = "defcn";
    
    private static final Log logger = LogFactory.getLog(ZosConsoleCommandImpl.class);

    public ZosConsoleCommandImpl(@NotNull String command, String consoleName, IZosImage image) throws ZosConsoleException {
        this.imageId = image.getImageID();
        this.consoleName = setConsoleName(consoleName);
        this.command = command;
        
        try {
            this.zosmfApiProcessor = ZosConsoleManagerImpl.zosmfManager.newZosmfRestApiProcessor(image, RestrictToImage.get(image.getImageID()));
        } catch (ZosConsoleManagerException | ZosmfManagerException e) {
            throw new ZosConsoleException(e);
        }
    }

    public @NotNull IZosConsoleCommand issueCommand() throws ZosConsoleException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("cmd", this.command);
        requestBody.addProperty("system", this.imageId);
        
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_JSON, RESTCONSOLE_PATH + this.consoleName, null, requestBody, 
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        } catch (ZosmfException e) {
            throw new ZosConsoleException(e);
        }
        
        JsonObject content;
        try {
            content = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosConsoleException(logUnableToIsuueCommand());
        }
        
        logger.trace(content);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            this.commandImmediateResponse = content.get("cmd-response").getAsString();
            this.commandResponseKey = content.get("cmd-response-key").getAsString();
        } else {
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            this.commandImmediateResponse = content.get("reason").getAsString();
            int commandErrorReturnCode = content.get("return-code").getAsInt();
            int commandErrorReasonCode = content.get("reason-code").getAsInt();
            logger.error("Command \"" + this.command + "\" failed. Reason=" + this.commandImmediateResponse + 
                    " Return Code=" + commandErrorReturnCode + " Reason Code=" + commandErrorReasonCode);
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
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, RESTCONSOLE_PATH + this.consoleName + "/solmsgs/" + this.commandResponseKey, null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        } catch (ZosmfException e) {
            throw new ZosConsoleException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            JsonObject content;
            try {
                content = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosConsoleException(logUnableToIsuueCommand());
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
            return DEFAULT_CONSOLE_NAME;
        }
        if (consoleName.length() < 2 || consoleName.length() > 8) {
            throw new ZosConsoleException("Invalid console name \"" + consoleName + "\" must be between 2 and 8 charaters long");
        }
        return null;
    }
    
    private String logUnableToIsuueCommand() {
        return "Unable to issue console command \"" + this.command + "\"";
    }

    @Override
    public String toString() {
        String cir = this.commandImmediateResponse != null ? " RESPONSE:\n " + this.commandImmediateResponse : "";
        return "COMMAND=" + this.command + (this.imageId != null ? " IMAGE=" +  this.imageId : "" + cir);
    }
}
