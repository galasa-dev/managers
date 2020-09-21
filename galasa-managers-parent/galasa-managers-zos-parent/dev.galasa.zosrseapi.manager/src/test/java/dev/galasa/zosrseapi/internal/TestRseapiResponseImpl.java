/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.internal.properties.RequestRetry;
import dev.galasa.zosrseapi.internal.properties.ServerHostname;
import dev.galasa.zosrseapi.internal.properties.ServerImages;
import dev.galasa.zosrseapi.internal.properties.ServerPort;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerImages.class, ServerHostname.class, ServerPort.class, RequestRetry.class})
public class TestRseapiResponseImpl {
    
    private RseapiResponseImpl rseapiResponse;
    
    private RseapiResponseImpl rseapiResponseSpy;
    
    @Mock
    private IHttpClient httpClientMock;
    
    @Mock
    private HttpClientResponse<String> httpClientResponseStringMock;
    
    @Mock
    private HttpClientResponse<byte[]> httpClientResponseByteMock;
    
    @Mock
    private HttpClientResponse<JsonObject> httpClientResponseJsonMock;
    
    @Mock
    private CloseableHttpResponse closeableHttpResponseMock;
    
    @Mock 
    private HttpEntity httpEntity;
    
    @Mock
    private StatusLine statusLineMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String URL = "http://domain/";

    private static final String PATH = "request-path";
    
    private static final String CONTENT_STRING = "content";
    
    private static final String JSONOBJECT_CONTENT_STRING = "{\"name\": \"value\"}";
    
    private static final JsonObject JSONOBJECT_CONTENT = new JsonParser().parse((String) JSONOBJECT_CONTENT_STRING).getAsJsonObject();
    
    private static final String JSONARRAY_CONTENT_STRING = "[\"element1\", \"element2\"]";
    
    private static final JsonArray JSONARRAY_CONTENT = new JsonParser().parse((String) JSONARRAY_CONTENT_STRING).getAsJsonArray();

    private static final String STATUS_LINE = "status-line";
    
    @Before
    public void setup() throws Exception {        
        rseapiResponse = new RseapiResponseImpl(URL, PATH);
        rseapiResponseSpy = PowerMockito.spy(rseapiResponse);
    }
    
    @Test
    public void testGetJsonContent() throws RseapiException {
        Whitebox.setInternalState(rseapiResponseSpy, "content", JSONOBJECT_CONTENT_STRING);
        Assert.assertTrue("getJsonContent() should return the expected value", JSONOBJECT_CONTENT.equals(rseapiResponseSpy.getJsonContent()));
        
        Whitebox.setInternalState(rseapiResponseSpy, "content", JSONOBJECT_CONTENT_STRING.getBytes());
        Assert.assertTrue("getJsonContent() should return the expected value", JSONOBJECT_CONTENT.equals(rseapiResponseSpy.getJsonContent()));
        
        Whitebox.setInternalState(rseapiResponseSpy, "content", new ByteArrayInputStream(JSONOBJECT_CONTENT_STRING.getBytes()));
        Assert.assertTrue("getJsonContent() should return the expected value", JSONOBJECT_CONTENT.equals(rseapiResponseSpy.getJsonContent()));
        
        Whitebox.setInternalState(rseapiResponseSpy, "content", JSONOBJECT_CONTENT);
        Assert.assertTrue("getJsonContent() should return the expected value", JSONOBJECT_CONTENT.equals(rseapiResponseSpy.getJsonContent()));
        
        Whitebox.setInternalState(rseapiResponseSpy, "content", new Integer(0));
        exceptionRule.expect(RseapiException.class);
        exceptionRule.expectMessage("Content not a JsonObject - " + Integer.class.getName());
        rseapiResponseSpy.getJsonContent();
    }
    
    @Test
    public void testGetJsonArrayContent() throws RseapiException {
        Whitebox.setInternalState(rseapiResponseSpy, "content", JSONARRAY_CONTENT_STRING);
        Assert.assertTrue("getJsonArrayContent() should return the expected value", JSONARRAY_CONTENT.equals(rseapiResponseSpy.getJsonArrayContent()));
        
        Whitebox.setInternalState(rseapiResponseSpy, "content", JSONARRAY_CONTENT_STRING.getBytes());
        Assert.assertTrue("getJsonArrayContent() should return the expected value", JSONARRAY_CONTENT.equals(rseapiResponseSpy.getJsonArrayContent()));
        
        Whitebox.setInternalState(rseapiResponseSpy, "content", new ByteArrayInputStream(JSONARRAY_CONTENT_STRING.getBytes()));
        Assert.assertTrue("getJsonArrayContent() should return the expected value", JSONARRAY_CONTENT.equals(rseapiResponseSpy.getJsonArrayContent()));
        
        Whitebox.setInternalState(rseapiResponseSpy, "content", new Integer(0));
        exceptionRule.expect(RseapiException.class);
        exceptionRule.expectMessage("Content not a JsonArray Object - " + Integer.class.getName());
        rseapiResponseSpy.getJsonArrayContent();
    }
    
    @Test
    public void testTextContent() throws RseapiException {
        Whitebox.setInternalState(rseapiResponseSpy, "content", CONTENT_STRING);
        Assert.assertTrue("getTextContent() should return the expected value", CONTENT_STRING.equals(rseapiResponseSpy.getTextContent()));
        
        Whitebox.setInternalState(rseapiResponseSpy, "content", new Integer(0));
        exceptionRule.expect(RseapiException.class);
        exceptionRule.expectMessage("Content not a String Object - " + Integer.class.getName());
        rseapiResponseSpy.getTextContent();
    }
    
    @Test
    public void testSetHttpClientresponseHttpClientResponse() throws RseapiException {
        Mockito.when(httpClientResponseStringMock.getContent()).thenReturn(CONTENT_STRING);
        Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(httpClientResponseStringMock.getStatusLine()).thenReturn(STATUS_LINE);
        
        rseapiResponseSpy.setHttpClientresponse(httpClientResponseStringMock);

        Assert.assertEquals("getContent() should return the expected value", CONTENT_STRING, rseapiResponseSpy.getContent());
        Assert.assertEquals("getStatusCode() should return the expected value", HttpStatus.SC_OK, rseapiResponseSpy.getStatusCode());
        Assert.assertEquals("getStatusLine() should return the expected value", STATUS_LINE, rseapiResponseSpy.getStatusLine());
    }
    
    @Test
    public void testSetHttpClientresponseCloseableHttpResponse() throws UnsupportedOperationException, IOException, RseapiException {
      Mockito.when(closeableHttpResponseMock.getEntity()).thenReturn(httpEntity);        
      Mockito.when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(CONTENT_STRING.getBytes()));
      Mockito.when(closeableHttpResponseMock.getStatusLine()).thenReturn(statusLineMock);
      Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
      Mockito.when(statusLineMock.getReasonPhrase()).thenReturn(STATUS_LINE);
      
      rseapiResponseSpy.setHttpClientresponse(closeableHttpResponseMock);
      Assert.assertTrue("getContent() should return the expected value", CONTENT_STRING.equals(IOUtils.toString((InputStream) rseapiResponseSpy.getContent(), StandardCharsets.UTF_8)));
      Assert.assertEquals("getStatusCode() should return the expected value", HttpStatus.SC_OK, rseapiResponseSpy.getStatusCode());
      Assert.assertEquals("getStatusLine() should return the expected value", STATUS_LINE, rseapiResponseSpy.getStatusLine());
      
      Mockito.when(httpEntity.getContent()).thenThrow(new IOException());
      exceptionRule.expect(RseapiException.class);
      exceptionRule.expectMessage("Could not retrieve response");
      
      rseapiResponseSpy.setHttpClientresponse(closeableHttpResponseMock);
    }
    
    @Test
    public void testGetRequestUrl() throws RseapiException, MalformedURLException {
        Assert.assertTrue("getRequestUrl() should return the expected value", new URL(URL + PATH).equals(rseapiResponseSpy.getRequestUrl()));
    }
}
