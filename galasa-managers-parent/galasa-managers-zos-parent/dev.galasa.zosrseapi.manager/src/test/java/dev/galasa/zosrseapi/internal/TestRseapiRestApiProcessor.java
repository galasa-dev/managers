/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.RseapiException;

@RunWith(PowerMockRunner.class)
public class TestRseapiRestApiProcessor {
    
    private RseapiRestApiProcessor rseapiRestApiProcessor;
    
    private RseapiRestApiProcessor rseapiRestApiProcessorSpy;

    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private RseapiImpl rseapiMock1;
    
    @Mock
    private RseapiImpl rseapiMock2;
    
    @Mock
    private RseapiImpl rseapiMock3;
    
    @Mock
    private IRseapiResponse rseapiResponseMock;
    
    private HashMap<String, IRseapi> rseapis = new LinkedHashMap<>();
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    
    private static final String PATH = "request-path";

    @Test
    public void testSendRequest() throws RseapiException {
        rseapis.put("image1", rseapiMock1);
        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiMock1.get(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean())).thenReturn(rseapiResponseMock);
        Mockito.when(rseapiMock1.getRequestRetry()).thenReturn(1);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("key", "value");
        
        IRseapiResponse response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.GET, PATH, headers, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)), false);
        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
        
        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.GET, PATH, null, null, null, false);
        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
        
        rseapis.put("image1", rseapiMock1);
        rseapis.put("image2", rseapiMock1);
        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND).thenReturn(HttpStatus.SC_OK);
        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.GET, PATH, null, null, null, false);
        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());

        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiMock1.put(Mockito.anyString(), Mockito.any())).thenReturn(rseapiResponseMock);
        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.PUT, PATH, null, null, null, false);
        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());

        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiMock1.putJson(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(rseapiResponseMock);
        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.PUT_JSON, PATH, null, null, null, false);
        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());

        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiMock1.post(Mockito.anyString(), Mockito.any())).thenReturn(rseapiResponseMock);
        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.POST, PATH, null, null, null, false);
        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());

        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiMock1.postJson(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(rseapiResponseMock);
        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.POST_JSON, PATH, null, null, null, false);
        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());

        Mockito.when(rseapiResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(rseapiMock1.delete(Mockito.anyString(), Mockito.any())).thenReturn(rseapiResponseMock);
        response = rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.DELETE, PATH, null, null, null, false);
        Assert.assertEquals("sendRequest() should return the expected value", HttpStatus.SC_OK, response.getStatusCode());
        
        exceptionRule.expect(RseapiException.class);
        exceptionRule.expectMessage("Unable to get valid response from RSE API server");
        rseapiRestApiProcessorSpy.sendRequest(RseapiRequestType.PUT_TEXT, PATH, null, null, null, false);
    }
    
    @Test
    public void testGetCurrentRseapiServer() throws RseapiException {
        rseapis.put("image1", rseapiMock1);
        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
        
        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock1, rseapiRestApiProcessorSpy.getCurrentRseapiServer());
    }
    
    @Test
    public void testGetNextRseapi() throws RseapiException {
        rseapis.put("image1", rseapiMock1);
        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
        
        rseapiRestApiProcessorSpy.getNextRseapi();
        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock1, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
        
        rseapis.put("image1", rseapiMock1);
        rseapis.put("image2", rseapiMock2);
        rseapis.put("image3", rseapiMock3);
        rseapiRestApiProcessor = new RseapiRestApiProcessor(rseapis);
        rseapiRestApiProcessorSpy = PowerMockito.spy(rseapiRestApiProcessor);
        
        rseapiRestApiProcessorSpy.getNextRseapi();
        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock2, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
        
        rseapiRestApiProcessorSpy.getNextRseapi();
        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock3, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
        
        rseapiRestApiProcessorSpy.getNextRseapi();
        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock1, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
        
        Whitebox.setInternalState(rseapiRestApiProcessorSpy, "rseapis", rseapis = new LinkedHashMap<>());
        rseapiRestApiProcessorSpy.getNextRseapi();
        Assert.assertEquals("getNextRseapi() should set the expected value", rseapiMock1, Whitebox.getInternalState(rseapiRestApiProcessorSpy, "currentRseapi"));
    }
}
