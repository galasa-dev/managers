/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.FrameworkUtil;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class})
public class TestRseapiZosUnixCommand {

    @Mock
    private IZosImage zosImageMock;

    @Mock
    private IZosManagerSpi zosManagerMock;
    
    @Mock
    private IRseapiRestApiProcessor rseapiApiProcessorMock;
    
    @Mock
    private IRseapiResponse rseapiResponseMock;
    
    @Mock
    private RseapiZosFileHandlerImpl zosFileHandlerMock;
    
    @Mock
    private RseapiZosDatasetImpl execDatasetMock;

    private static final String COMMAND = "command";
    
    private static final String PROP_INVOCATION = "invocation";
    private static final String PROP_PATH = "path";
    private static final String PROP_OUTPUT = "output";
    private static final String PROP_STDOUT = "stdout";
    private static final String PROP_STDERR = "stderr";
    private static final String PROP_EXIT_CODE = "exit code";

	private static final String EXCEPTION = "exception";
    
    @Test
    public void testExecute() throws RseapiException, ZosDatasetException {
    	Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
    	Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    	Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(getCommandJsonObject(0));
		Assert.assertEquals("execute() should return the expected value", getCommandJsonObject(0), RseapiZosUnixCommand.execute(rseapiApiProcessorMock, COMMAND));
    }
    
    @Test
    public void testExecuteException1() throws RseapiException, ZosDatasetException {
    	Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new RseapiException(EXCEPTION));
        Assert.assertThrows(EXCEPTION, ZosDatasetException.class, ()->{
        	RseapiZosUnixCommand.execute(rseapiApiProcessorMock, COMMAND);
        });
    }
    
    @Test
    public void testExecuteException2() throws RseapiException, ZosDatasetException {
    	Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
    	Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
    	Mockito.when(rseapiResponseMock.getStatusLine()).thenReturn("NOT_FOUND");
        String expectedMessage = "Error zOS UNIX command, HTTP Status Code " + HttpStatus.SC_NOT_FOUND + " : NOT_FOUND";
        Assert.assertThrows(expectedMessage, ZosDatasetException.class, ()->{
        	RseapiZosUnixCommand.execute(rseapiApiProcessorMock, COMMAND);
        });
    }
    
    @Test
    public void testExecuteException3() throws RseapiException, ZosDatasetException {
    	Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
    	Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    	Mockito.when(rseapiResponseMock.getJsonContent()).thenThrow(new RseapiException(EXCEPTION));
        String expectedMessage = "Issue command failed";
        Assert.assertThrows(expectedMessage, ZosDatasetException.class, ()->{
        	RseapiZosUnixCommand.execute(rseapiApiProcessorMock, COMMAND);
        });
    }
    
    @Test
    public void testExecuteException4() throws RseapiException, ZosDatasetException {
    	Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
    	Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    	Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(getCommandJsonObject(99));
        String expectedMessage = "Command failed. Response body:\n" + getCommandJsonObject(99);
        Assert.assertThrows(expectedMessage, ZosDatasetException.class, ()->{
        	RseapiZosUnixCommand.execute(rseapiApiProcessorMock, COMMAND);
        });
    }
    
    @Test
    public void testExecuteException5() throws RseapiException, ZosDatasetException {
    	Mockito.when(rseapiApiProcessorMock.sendRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
    	Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    	Mockito.when(rseapiResponseMock.getJsonContent()).thenReturn(getCommandJsonObject(-1));
        String expectedMessage = "Command failed. Response body:\n" + getCommandJsonObject(-1);
        Assert.assertThrows(expectedMessage, ZosDatasetException.class, ()->{
        	RseapiZosUnixCommand.execute(rseapiApiProcessorMock, COMMAND);
        });
    }
    
    private JsonObject getCommandJsonObject(int rc) {
		JsonObject output = new JsonObject();
		output.addProperty(PROP_STDOUT, "");
		output.addProperty(PROP_STDERR, "");
		JsonObject responseBody = new JsonObject();
		responseBody.add(PROP_OUTPUT, output);
		responseBody.addProperty(PROP_PATH, "path");
		if (rc >= 0) {
			responseBody.addProperty(PROP_EXIT_CODE, rc);
		}
		responseBody.addProperty(PROP_INVOCATION, COMMAND);
		return responseBody;
	}
}
