/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosrseapi.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.properties.Https;
import dev.galasa.zosrseapi.internal.properties.RequestRetry;
import dev.galasa.zosrseapi.internal.properties.ServerHostname;
import dev.galasa.zosrseapi.internal.properties.ServerImages;
import dev.galasa.zosrseapi.internal.properties.ServerPort;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogFactory.class, ServerImages.class, ServerHostname.class, ServerPort.class, Https.class, RequestRetry.class})
public class TestRseapiImpl {
    
    private RseapiImpl rseapi;
    
    private RseapiImpl rseapiSpy;

    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private ZosManagerImpl zosManagerMock;
    
    @Mock
    private IHttpManagerSpi httpManagerMock;
    
    @Mock
    private IHttpClient httpClientMock;
    
    @Mock
    private CloseableHttpResponse closeableHttpResponseMock;
    
    @Mock 
    private HttpEntity httpEntity;
    
    @Mock
    private StatusLine statusLineMock;
    
    @Mock
    private HttpClientResponse<String> httpClientResponseStringMock;
    
    @Mock
    private HttpClientResponse<byte[]> httpClientResponseByteMock;
    
    @Mock
    private HttpClientResponse<JsonObject> httpClientResponseJsonMock;
    
    @Mock
    private IRseapiRestApiProcessor rseapiApiProcessorMock;
    
    @Mock
    private ICredentialsUsernamePassword credentialsUsernamePasswordMock;
    
    @Mock
    private ICredentials credentialsMock;
    
    @Mock
    private ICredentialsToken credentialsTokenMock;
    
    @Mock
    private Log logMock;
    
    private static String logMessage;

    private static final String IMAGE = "image";

    private static final String IMAGE_TAG = "tag";

    private static final String CLUSTER = "cluster";

    private static final String USERID = "userid";

    private static final String PASSWORD = "password";

    private static final String HOSTNAME = "hostname";

    private static final String PORT = "999";

    private static final String PATH = "request-path";

    private static final String KEY = "key";

    private static final String VALUE = "value";

    private static final String CONTENT = "content";

    private static final String STATUS_LINE = "status-line";

    private static final String EXCEPTION = "exception";

    private static final int REQUEST_RETRY = 5;
    
    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
        Answer<String> answer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                logMessage = invocation.getArgument(0);
                System.err.println("Captured Log Message:\n" + logMessage);
                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
                    ((Throwable) invocation.getArgument(1)).printStackTrace();
                }
                return null;
            }
        };
        Mockito.doAnswer(answer).when(logMock).debug(Mockito.any());
        
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        Mockito.when(zosImageMock.getClusterID()).thenReturn(CLUSTER);
        
        PowerMockito.mockStatic(ServerImages.class);
        Mockito.when(ServerImages.get(Mockito.any())).thenReturn(Arrays.asList(IMAGE));
        
        PowerMockito.mockStatic(ServerHostname.class);
        Mockito.when(ServerHostname.get(Mockito.any())).thenReturn(HOSTNAME);
        
        PowerMockito.mockStatic(ServerPort.class);
        Mockito.when(ServerPort.get(Mockito.any())).thenReturn(PORT);
        
        PowerMockito.mockStatic(Https.class);
        Mockito.when(Https.get(Mockito.any())).thenReturn(true);
        
        Whitebox.setInternalState(RseapiManagerImpl.class, "httpManager", httpManagerMock);
        Mockito.when(httpManagerMock.newHttpClient()).thenReturn(httpClientMock);
        
        Whitebox.setInternalState(RseapiManagerImpl.class, "zosManager", zosManagerMock);
        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
        
        PowerMockito.mockStatic(RequestRetry.class);
        Mockito.when(RequestRetry.get(Mockito.any())).thenReturn(REQUEST_RETRY);
        
        PowerMockito.doReturn(credentialsUsernamePasswordMock).when(zosImageMock, "getDefaultCredentials");
        PowerMockito.doReturn(USERID).when(credentialsUsernamePasswordMock, "getUsername");
        PowerMockito.doReturn(PASSWORD).when(credentialsUsernamePasswordMock, "getPassword");
        
        rseapi = new RseapiImpl(zosImageMock);
        rseapiSpy = PowerMockito.spy(rseapi);
    }
    
    @Test
    public void testStringConstructor() throws RseapiException {
        RseapiImpl localRseapi = new RseapiImpl(IMAGE_TAG);
        Assert.assertTrue("Error in String constructor", localRseapi instanceof RseapiImpl);
        Assert.assertEquals("requestRetry() should return the expected value", REQUEST_RETRY, localRseapi.getRequestRetry());
    }
    
    @Test
    public void testSetHeader() {
        rseapiSpy.setHeader(KEY, VALUE);
        HashMap<String, String> commonHeaders = Whitebox.getInternalState(rseapiSpy, "commonHeaders");
        
        Assert.assertEquals("setHeader() should set the supplied value", VALUE, commonHeaders.get(KEY));
    }
    
    @Test
    public void testClearHeaders() {
        rseapiSpy.clearHeaders();
        HashMap<String, String> commonHeaders = Whitebox.getInternalState(rseapiSpy, "commonHeaders");
        
        Assert.assertTrue("clearHeaders() should set the supplied value", commonHeaders.isEmpty());
    }
    
    @Test
    public void testGet() throws RseapiException {
        setupGet();
        IRseapiResponse rseapiResponse = rseapiSpy.get(PATH, null, false);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());

        rseapiResponse = rseapiSpy.get(PATH, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)), true);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());
    }
    
    @Test
    public void testGetBadHttpResponseException() throws RseapiException {
        setupGet();
        Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        String expectedMessage = "Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND;
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.get(PATH, null, true);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testGetHttpException() throws RseapiException, HttpClientException {
        setupGet();
        Mockito.when(httpClientMock.getJson(Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        String expectedMessage =  "Problem with GET to RSE API server";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.get(PATH, null, true);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    private void setupGet() {
        try {
        	Mockito.when(httpClientMock.getText(Mockito.anyString())).thenReturn(httpClientResponseStringMock); 
            Mockito.when(httpClientResponseStringMock.getContent()).thenReturn(CONTENT);
            Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseStringMock.getStatusLine()).thenReturn(STATUS_LINE); 
        	Mockito.when(httpClientMock.getJson(Mockito.anyString())).thenReturn(httpClientResponseJsonMock);
            Mockito.when(httpClientResponseJsonMock.getContent()).thenReturn(new JsonObject());
            Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseJsonMock.getStatusLine()).thenReturn(STATUS_LINE);
            
            Mockito.when(httpClientMock.getFile(Mockito.anyString())).thenReturn(closeableHttpResponseMock);       
            Mockito.when(closeableHttpResponseMock.getEntity()).thenReturn(httpEntity);        
            Mockito.when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(CONTENT.getBytes()));
            Mockito.when(closeableHttpResponseMock.getStatusLine()).thenReturn(statusLineMock);
            Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(statusLineMock.getReasonPhrase()).thenReturn(STATUS_LINE);
        } catch (HttpClientException | UnsupportedOperationException | IOException e) {
            throw new MockitoException("Problem in setupGet() method ", e);
        }
    }

    @Test
    public void testPost() throws RseapiException {
        setupPost();
        IRseapiResponse rseapiResponse = rseapiSpy.post(PATH, null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());

        rseapiResponse = rseapiSpy.post(PATH, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());
    }
    
    @Test
    public void testPostBadHttpResponseException() throws RseapiException {
        setupPost();
        Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        String expectedMessage = "Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND;
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.post(PATH, null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testPostHttpException() throws RseapiException, HttpClientException {
        setupPost();
        Mockito.when(httpClientMock.postJson(Mockito.any(), Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        String expectedMessage =  "Problem with POST to RSE API server";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.post(PATH, null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    private void setupPost() {
        try {
            Mockito.when(httpClientMock.postJson(Mockito.anyString(), Mockito.any())).thenReturn(httpClientResponseJsonMock); 
            Mockito.when(httpClientResponseJsonMock.getContent()).thenReturn(new JsonObject());
            Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseJsonMock.getStatusLine()).thenReturn(STATUS_LINE);
        } catch (HttpClientException | UnsupportedOperationException e) {
            throw new MockitoException("Problem in setupPostJson() method ", e);
        }
    }

    @Test
    public void testPostJson() throws RseapiException {
        setupPostJson();
        IRseapiResponse rseapiResponse = rseapiSpy.postJson(PATH, new JsonObject(), null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());

        rseapiResponse = rseapiSpy.postJson(PATH, new JsonObject(), new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());
    }
    
    @Test
    public void testPostJsonBadHttpResponseException() throws RseapiException {
        setupPostJson();
        Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        String expectedMessage = "Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND;
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.postJson(PATH, new JsonObject(), null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testPostJsonHttpException() throws RseapiException, HttpClientException {
        setupPostJson();
        Mockito.when(httpClientMock.postJson(Mockito.any(), Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        String expectedMessage =  "Problem with POST to RSE API server";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.postJson(PATH, new JsonObject(), null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    private void setupPostJson() {
        try {
            Mockito.when(httpClientMock.postJson(Mockito.anyString(), Mockito.any())).thenReturn(httpClientResponseJsonMock); 
            Mockito.when(httpClientResponseJsonMock.getContent()).thenReturn(new JsonObject());
            Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseJsonMock.getStatusLine()).thenReturn(STATUS_LINE);
        } catch (HttpClientException | UnsupportedOperationException e) {
            throw new MockitoException("Problem in setupPostJson() method ", e);
        }
    }

    @Test
    public void testPutText() throws RseapiException {
        setupPutText();
        IRseapiResponse rseapiResponse = rseapiSpy.putText(PATH, "", null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());

        rseapiResponse = rseapiSpy.putText(PATH, "", new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());
    }
    
    @Test
    public void testPutBadHttpResponseException() throws RseapiException {
        setupPutText();
        Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        String expectedMessage = "Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND;
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.putText(PATH, "", null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testPutHttpException() throws RseapiException, HttpClientException {
        setupPutText();
        Mockito.when(httpClientMock.putText(Mockito.any(), Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        String expectedMessage =  "Problem with PUT to RSE API server";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.putText(PATH, "", null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    private void setupPutText() {
        try {
            Mockito.when(httpClientMock.putText(Mockito.anyString(), Mockito.anyString())).thenReturn(httpClientResponseStringMock); 
            Mockito.when(httpClientResponseStringMock.getContent()).thenReturn(CONTENT);
            Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseStringMock.getStatusLine()).thenReturn(STATUS_LINE);
        } catch (HttpClientException | UnsupportedOperationException e) {
            throw new MockitoException("Problem in setupPutText() method ", e);
        }
    }

    @Test
    public void testPutJson() throws RseapiException {
        setupPutJson();
        IRseapiResponse rseapiResponse = rseapiSpy.putJson(PATH, new JsonObject(), null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());

        rseapiResponse = rseapiSpy.putJson(PATH, new JsonObject(), new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());
    }
    
    @Test
    public void testPutJsonBadHttpResponseException() throws RseapiException {
        setupPutJson();
        Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        String expectedMessage = "Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND;
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.putJson(PATH, new JsonObject(), null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testPutJsonHttpException() throws RseapiException, HttpClientException {
        setupPutJson();
        Mockito.when(httpClientMock.putJson(Mockito.any(), Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        String expectedMessage =  "Problem with PUT to RSE API server";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.putJson(PATH, new JsonObject(), null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    private void setupPutJson() {
        try {
            Mockito.when(httpClientMock.putJson(Mockito.anyString(), Mockito.any())).thenReturn(httpClientResponseJsonMock); 
            Mockito.when(httpClientResponseJsonMock.getContent()).thenReturn(new JsonObject());
            Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseJsonMock.getStatusLine()).thenReturn(STATUS_LINE);
        } catch (HttpClientException | UnsupportedOperationException e) {
            throw new MockitoException("Problem in setupPutJson() method ", e);
        }
    }

    @Test
    public void testDelete() throws RseapiException {
        setupDelete();
        IRseapiResponse rseapiResponse = rseapiSpy.delete(PATH, null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());

        rseapiResponse = rseapiSpy.delete(PATH, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, rseapiResponse.getStatusCode());
    }
    
    @Test
    public void testDeleteBadHttpResponseException() throws RseapiException {
        setupDelete();
        Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        String expectedMessage = "Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND;
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.delete(PATH, null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testDeleteHttpException() throws RseapiException, HttpClientException {
        setupDelete();
        Mockito.when(httpClientMock.deleteJson(Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        String expectedMessage =  "Problem with DELETE to RSE API server";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.delete(PATH, null);
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    private void setupDelete() {
        try {
            Mockito.when(httpClientMock.deleteJson(Mockito.anyString())).thenReturn(httpClientResponseJsonMock); 
            Mockito.when(httpClientResponseJsonMock.getContent()).thenReturn(new JsonObject());
            Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseJsonMock.getStatusLine()).thenReturn(STATUS_LINE);
        } catch (HttpClientException | UnsupportedOperationException e) {
            throw new MockitoException("Problem in setupDelete() method ", e);
        }
    }

    @Test
    public void testServerInfo() throws HttpClientException, RseapiException {
    	setupGet(); 
    	JsonObject jsonObject = new JsonObject();
    	jsonObject.addProperty("rseapi_version", "version");
    	Mockito.when(httpClientMock.getJson(Mockito.anyString())).thenReturn(httpClientResponseJsonMock); 
        Mockito.when(httpClientResponseJsonMock.getContent()).thenReturn(jsonObject);
        Assert.assertEquals("serverInfo() should return the expected value", jsonObject, rseapi.serverInfo());
    }
    
    @Test
    public void testGetImage() {        
        Assert.assertEquals("getImage() should return the expected value", zosImageMock, rseapiSpy.getImage());
    }
    
    @Test
    public void testToString() {
        String result = zosImageMock.getImageID() + " https://" + HOSTNAME + ":" + PORT;
        Assert.assertEquals("toString() should return the expected value", result, rseapiSpy.toString());
    }
    
    @Test
    public void testValidPath() {
        Assert.assertEquals("validPath() should return the expected value", "/" + PATH, rseapiSpy.validPath(PATH));
        Assert.assertEquals("validPath() should return the expected value", "/" + PATH, rseapiSpy.validPath("/" + PATH));
    }
    
    @Test
    public void testInitialize() throws Exception {
        rseapiSpy.initialize();
        String toStringValue = zosImageMock.getImageID() + " https://" + HOSTNAME + ":" + PORT;
        Assert.assertEquals("toString() should return the expected value", toStringValue, rseapiSpy.toString());
        
        PowerMockito.doReturn(credentialsMock).when(zosImageMock, "getDefaultCredentials");
        Mockito.when(Https.get(Mockito.any())).thenReturn(false);
        rseapiSpy.initialize();
        toStringValue = zosImageMock.getImageID() + " http://" + HOSTNAME + ":" + PORT;
        Assert.assertEquals("toString() should return the expected value", toStringValue, rseapiSpy.toString());
    }
    
    @Test
    public void testInitializeServerImagesException() throws Exception {
        Mockito.when(ServerImages.get(Mockito.any())).thenThrow(new RseapiManagerException(EXCEPTION));
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.initialize();
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testInitializeImageNoConfiguredException() throws Exception {
        Mockito.when(ServerImages.get(Mockito.any())).thenReturn(Arrays.asList(""));
        String expectedMessage = "RSE API server not configured for image '" + IMAGE + "' on cluster '" + CLUSTER + "'";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.initialize();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testInitializeImageNoConfiguredTagException() throws Exception {
        Mockito.when(ServerImages.get(Mockito.any())).thenReturn(Arrays.asList(""));
        Whitebox.setInternalState(rseapiSpy, "imageTag", IMAGE_TAG);
        String expectedMessage = "RSE API server not configured for image '" + IMAGE + "' on cluster '" + CLUSTER + "' tag '" + IMAGE_TAG + "'";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.initialize();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testInitializeServerHostnameException() throws Exception {
        Mockito.when(ServerHostname.get(Mockito.any())).thenThrow(new RseapiManagerException(EXCEPTION));
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.initialize();
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testInitializeServerPortException() throws Exception {
        Mockito.when(ServerPort.get(Mockito.any())).thenThrow(new RseapiManagerException(EXCEPTION));
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.initialize();
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testInitializeHttpsException() throws Exception {
        Mockito.when(Https.get(Mockito.any())).thenThrow(new RseapiManagerException(EXCEPTION));
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.initialize();
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testInitializeHttpClientException() throws Exception {
        Mockito.when(zosImageMock.getDefaultCredentials()).thenThrow(new ZosManagerException(EXCEPTION));
        String expectedMessage = "Unable to create HTTP Client";
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.initialize();
        });
    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
    }
    
    @Test
    public void testInitializeRequestRetryException() throws Exception {
        Mockito.when(RequestRetry.get(Mockito.any())).thenThrow(new RseapiManagerException(EXCEPTION));
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.initialize();
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testSetImage() throws Exception {
        rseapiSpy.setImage();
        Assert.assertEquals("setImage() should set image to the expected value", zosImageMock, Whitebox.getInternalState(rseapiSpy, "image"));
        
        Whitebox.setInternalState(rseapiSpy, "image", (String) null);
        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenThrow(new RseapiManagerException(EXCEPTION));
        RseapiException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiException.class, ()->{
        	rseapiSpy.setImage();
        });
    	Assert.assertEquals("exception should contain expected message", EXCEPTION, expectedException.getCause().getMessage());
    }
    
    @Test
    public void testAddCommonHeaders() {
    	HashMap<String, String> commonHeaders = new HashMap<>();
    	logMessage = null;
    	rseapiSpy.addCommonHeaders();
    	Assert.assertNull("addCommonHeaders() should not log message", logMessage);
    	
    	commonHeaders.put("key", "value");
    	Whitebox.setInternalState(rseapiSpy, "commonHeaders", commonHeaders);
    	logMessage = null;
    	rseapiSpy.addCommonHeaders();
    	Assert.assertEquals("addCommonHeaders() should log the expected value", "Adding HTTP header: key: value", logMessage);
    }
}
