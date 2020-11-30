/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosmf.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
import org.mockito.exceptions.base.MockitoException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.properties.Https;
import dev.galasa.zosmf.internal.properties.RequestRetry;
import dev.galasa.zosmf.internal.properties.ServerCreds;
import dev.galasa.zosmf.internal.properties.ServerImage;
import dev.galasa.zosmf.internal.properties.ServerPort;
import dev.galasa.zosmf.internal.properties.SysplexServers;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SysplexServers.class, ServerImage.class, ServerCreds.class, ServerPort.class, Https.class, RequestRetry.class})
public class TestZosmfImpl {
    
    private ZosmfImpl zosmf;
    
    private ZosmfImpl zosmfSpy;

    @Mock
    private IFramework frameworkMock;
    
    @Mock
    private ICredentialsService credentialsServiceMock;
    
    @Mock
    private IZosImage zosImageMock;
    
    @Mock
    private ZosmfManagerImpl zosmfManagerMock;
    
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
    private IZosmfRestApiProcessor zosmfApiProcessorMock;
    
    @Mock
    private ICredentialsUsernamePassword credentialsUsernamePasswordMock;
    
    @Mock
    private ICredentials credentialsMock;
    
    @Mock
    private ICredentialsToken credentialsTokenMock;
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private static final String SERVER_ID = "SERVER1";

    private static final String IMAGE = "image";

    private static final String IMAGE_TAG = "tag";

    private static final String CLUSTER = "cluster";

    private static final String CREDSID = "ZOS";

    private static final String USERID = "userid";

    private static final String PASSWORD = "password";

    private static final String HOSTNAME = "hostname";

    private static final int PORT = 999;

    private static final String PATH = "request-path";

    private static final String KEY = "key";

    private static final String VALUE = "value";

    private static final String CONTENT = "content";

    private static final String STATUS_LINE = "status-line";

    private static final String EXCEPTION = "exception";

    private static final int REQUEST_RETRY = 5;
    
    @Before
    public void setup() throws Exception {
        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
        Mockito.when(zosImageMock.getClusterID()).thenReturn(CLUSTER);
        Mockito.when(zosImageMock.getDefaultHostname()).thenReturn(HOSTNAME);
        
        PowerMockito.mockStatic(SysplexServers.class);
        Mockito.when(SysplexServers.get(Mockito.any())).thenReturn(Arrays.asList(IMAGE));
        
        PowerMockito.mockStatic(ServerPort.class);
        Mockito.when(ServerPort.get(Mockito.any())).thenReturn(PORT);
        
        PowerMockito.mockStatic(Https.class);
        Mockito.when(Https.get(Mockito.any())).thenReturn(true);
                
        PowerMockito.mockStatic(RequestRetry.class);
        Mockito.when(RequestRetry.get(Mockito.any())).thenReturn(REQUEST_RETRY);
        
        PowerMockito.mockStatic(ServerImage.class);
        Mockito.when(ServerImage.get(Mockito.any())).thenReturn(IMAGE);
        
        PowerMockito.mockStatic(ServerCreds.class);
        Mockito.when(ServerCreds.get(Mockito.any())).thenReturn(CREDSID);
        
        PowerMockito.doReturn(credentialsUsernamePasswordMock).when(zosImageMock, "getDefaultCredentials");
        PowerMockito.doReturn(USERID).when(credentialsUsernamePasswordMock, "getUsername");
        PowerMockito.doReturn(PASSWORD).when(credentialsUsernamePasswordMock, "getPassword");
        PowerMockito.doReturn(zosManagerMock).when(zosmfManagerMock, "getZosManager");
        PowerMockito.doReturn(httpManagerMock).when(zosmfManagerMock, "getHttpManager");
        PowerMockito.doReturn(frameworkMock).when(zosmfManagerMock, "getFramework");
        Mockito.when(httpManagerMock.newHttpClient()).thenReturn(httpClientMock);
        Mockito.when(zosManagerMock.getUnmanagedImage(IMAGE)).thenReturn(zosImageMock);
        Mockito.when(frameworkMock.getCredentialsService()).thenReturn(credentialsServiceMock);
        Mockito.when(credentialsServiceMock.getCredentials(CREDSID)).thenReturn(credentialsUsernamePasswordMock);
        
        
        zosmf = new ZosmfImpl(zosmfManagerMock, SERVER_ID);
        zosmfSpy = PowerMockito.spy(zosmf);
    }
    
    @Test
    public void testStringConstructor() throws ZosmfException {
        ZosmfImpl localZosmf = new ZosmfImpl(zosmfManagerMock, SERVER_ID);
        Assert.assertTrue("Error in String constructor", localZosmf instanceof ZosmfImpl);
        Assert.assertEquals("requestRetry() should return the expected value", REQUEST_RETRY, localZosmf.getRequestRetry());
    }
    
    @Test
    public void testSetHeader() {
        zosmfSpy.setHeader(KEY, VALUE);
        HashMap<String, String> commonHeaders = Whitebox.getInternalState(zosmfSpy, "commonHeaders");
        
        Assert.assertEquals("setHeader() should set the supplied value", VALUE, commonHeaders.get(KEY));
    }
    
    @Test
    public void testClearHeaders() {
        zosmfSpy.clearHeaders();
        HashMap<String, String> commonHeaders = Whitebox.getInternalState(zosmfSpy, "commonHeaders");
        
        Assert.assertTrue("clearHeaders() should set the supplied value", commonHeaders.isEmpty());
    }
    
    @Test
    public void testGet() throws ZosmfException {
        setupGet();
        IZosmfResponse zosmfResponse = zosmfSpy.get(PATH, null, false);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());

        zosmfResponse = zosmfSpy.get(PATH, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)), true);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());
    }
    
    @Test
    public void testGetBadHttpResponseException() throws ZosmfException {
        setupGet();
        Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage("Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND);
        zosmfSpy.get(PATH, null, true);
    }
    
    @Test
    public void testGetHttpException() throws ZosmfException, HttpClientException {
        setupGet();
        Mockito.when(httpClientMock.getText(Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage( "Problem with GET to zOSMF server");
        zosmfSpy.get(PATH, null, true);
    }
    
    private void setupGet() {
        try {
            Mockito.when(httpClientMock.getText(Mockito.anyString())).thenReturn(httpClientResponseStringMock); 
            Mockito.when(httpClientResponseStringMock.getContent()).thenReturn(CONTENT);
            Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseStringMock.getStatusLine()).thenReturn(STATUS_LINE);
            
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
    public void testPostJson() throws ZosmfException {
        setupPostJson();
        IZosmfResponse zosmfResponse = zosmfSpy.postJson(PATH, new JsonObject(), null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());

        zosmfResponse = zosmfSpy.postJson(PATH, new JsonObject(), new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());
    }
    
    @Test
    public void testPostJsonBadHttpResponseException() throws ZosmfException {
        setupPostJson();
        Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage("Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND);
        zosmfSpy.postJson(PATH, new JsonObject(), null);
    }
    
    @Test
    public void testPostJsonHttpException() throws ZosmfException, HttpClientException {
        setupPostJson();
        Mockito.when(httpClientMock.postJson(Mockito.any(), Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage( "Problem with POST to zOSMF server");
        zosmfSpy.postJson(PATH, new JsonObject(), null);
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
    public void testPutText() throws ZosmfException {
        setupPutText();
        IZosmfResponse zosmfResponse = zosmfSpy.putText(PATH, "", null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());

        zosmfResponse = zosmfSpy.putText(PATH, "", new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());
    }
    
    @Test
    public void testPutTextBadHttpResponseException() throws ZosmfException {
        setupPutText();
        Mockito.when(httpClientResponseStringMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage("Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND);
        zosmfSpy.putText(PATH, "", null);
    }
    
    @Test
    public void testPutTextHttpException() throws ZosmfException, HttpClientException {
        setupPutText();
        Mockito.when(httpClientMock.putText(Mockito.any(), Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage( "Problem with PUT to zOSMF server");
        zosmfSpy.putText(PATH, "", null);
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
    public void testPutJson() throws ZosmfException {
        setupPutJson();
        IZosmfResponse zosmfResponse = zosmfSpy.putJson(PATH, new JsonObject(), null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());

        zosmfResponse = zosmfSpy.putJson(PATH, new JsonObject(), new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());
    }
    
    @Test
    public void testPutJsonBadHttpResponseException() throws ZosmfException {
        setupPutJson();
        Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage("Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND);
        zosmfSpy.putJson(PATH, new JsonObject(), null);
    }
    
    @Test
    public void testPutJsonHttpException() throws ZosmfException, HttpClientException {
        setupPutJson();
        Mockito.when(httpClientMock.putJson(Mockito.any(), Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage( "Problem with PUT to zOSMF server");
        zosmfSpy.putJson(PATH, new JsonObject(), null);
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
    public void testPutBinary() throws ZosmfException {
        setupPutBinary();
        IZosmfResponse zosmfResponse = zosmfSpy.putBinary(PATH, "".getBytes(), null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());

        zosmfResponse = zosmfSpy.putBinary(PATH, "".getBytes(), new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());
    }
    
    @Test
    public void testPutBinaryBadHttpResponseException() throws ZosmfException {
        setupPutBinary();
        Mockito.when(httpClientResponseByteMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage("Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND);
        zosmfSpy.putBinary(PATH, "".getBytes(), null);
    }
    
    @Test
    public void testPutBinaryHttpException() throws ZosmfException, HttpClientException {
        setupPutBinary();
        Mockito.when(httpClientMock.putBinary(Mockito.any(), Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage("Problem with PUT to zOSMF server");
        zosmfSpy.putBinary(PATH, "".getBytes(), null);
    }

    private void setupPutBinary() {
        try {
            Mockito.when(httpClientMock.putBinary(Mockito.anyString(), Mockito.any())).thenReturn(httpClientResponseByteMock); 
            Mockito.when(httpClientResponseByteMock.getContent()).thenReturn(CONTENT.getBytes());
            Mockito.when(httpClientResponseByteMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            Mockito.when(httpClientResponseByteMock.getStatusLine()).thenReturn(STATUS_LINE);
        } catch (HttpClientException | UnsupportedOperationException e) {
            throw new MockitoException("Problem in setupPutBinary() method ", e);
        }
    }

    @Test
    public void testDelete() throws ZosmfException {
        setupDelete();
        IZosmfResponse zosmfResponse = zosmfSpy.delete(PATH, null);
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());

        zosmfResponse = zosmfSpy.delete(PATH, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)));
        Assert.assertEquals("get() should return the expected value", HttpStatus.SC_OK, zosmfResponse.getStatusCode());
    }
    
    @Test
    public void testDeleteBadHttpResponseException() throws ZosmfException {
        setupDelete();
        Mockito.when(httpClientResponseJsonMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage("Unexpected HTTP status code: " + HttpStatus.SC_NOT_FOUND);
        zosmfSpy.delete(PATH, null);
    }
    
    @Test
    public void testDeleteHttpException() throws ZosmfException, HttpClientException {
        setupDelete();
        Mockito.when(httpClientMock.deleteJson(Mockito.any())).thenThrow(new HttpClientException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage( "Problem with DELETE to zOSMF server");
        zosmfSpy.delete(PATH, null);
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
    public void testGetImage() {        
        Assert.assertEquals("getImage() should return the expected value", zosImageMock, zosmfSpy.getImage());
    }
    
    @Test
    public void testToString() {
        String result = zosImageMock.getImageID() + " https://" + HOSTNAME + ":" + PORT;
        Assert.assertEquals("toString() should return the expected value", result, zosmfSpy.toString());
    }
    
    @Test
    public void testValidPath() {
        Assert.assertEquals("validPath() should return the expected value", "/" + PATH, zosmfSpy.validPath(PATH));
        Assert.assertEquals("validPath() should return the expected value", "/" + PATH, zosmfSpy.validPath("/" + PATH));
    }
    
    @Test
    public void testInitialize() throws Exception {
        Mockito.when(Https.get(Mockito.any())).thenReturn(true);
        zosmfSpy.initialize();
        String toStringValue = zosImageMock.getImageID() + " https://" + HOSTNAME + ":" + PORT;
        Assert.assertEquals("toString() should return the expected value", toStringValue, zosmfSpy.toString());
        
        Mockito.when(Https.get(Mockito.any())).thenReturn(false);
        zosmfSpy.initialize();
        toStringValue = zosImageMock.getImageID() + " http://" + HOSTNAME + ":" + PORT;
        Assert.assertEquals("toString() should return the expected value", toStringValue, zosmfSpy.toString());
        
        PowerMockito.doReturn(credentialsMock).when(zosImageMock, "getDefaultCredentials");
        zosmfSpy.initialize();
    }
    
    @Test
    public void testInitializeServerPortException() throws Exception {
        Mockito.when(ServerPort.get(Mockito.any())).thenThrow(new ZosmfManagerException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosmfSpy.initialize();
    }
    
    @Test
    public void testInitializeHttpsException() throws Exception {
        Mockito.when(Https.get(Mockito.any())).thenThrow(new ZosmfManagerException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosmfSpy.initialize();
    }
    
    @Test
    public void testInitializeHttpClientException() throws Exception {
        Mockito.when(ServerCreds.get(Mockito.any())).thenReturn(null);
        Mockito.when(zosImageMock.getDefaultCredentials()).thenThrow(new ZosManagerException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage("Unable to create HTTP Client");
        
        zosmfSpy.initialize();
    }
    
    @Test
    public void testInitializeRequestRetryException() throws Exception {
        Mockito.when(RequestRetry.get(Mockito.any())).thenThrow(new ZosmfManagerException(EXCEPTION));
        exceptionRule.expect(ZosmfException.class);
        exceptionRule.expectMessage(EXCEPTION);
        
        zosmfSpy.initialize();
    }
    
}
