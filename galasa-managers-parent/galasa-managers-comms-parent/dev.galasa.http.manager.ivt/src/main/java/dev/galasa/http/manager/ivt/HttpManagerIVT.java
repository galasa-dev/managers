/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.http.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.assertj.core.internal.bytebuddy.agent.builder.ResettableClassFileTransformer;

import com.google.gson.JsonObject;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.http.HttpClient;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;

@Test
public class HttpManagerIVT {

    @Logger
    public Log logger;

    @HttpClient
    public IHttpClient client1;

    @HttpClient
    public IHttpClient client2;

    @HttpClient
    public IHttpClient client3;

    @HttpClient
    public IHttpClient reusableClient;

    @Test
    public void checkClientNotNull() throws Exception {
        assertThat(client1).isNotNull();
        assertThat(client2).isNotNull();
        assertThat(client3).isNotNull();
    }

    @Test
    public void makeOutBoundHttpCall() throws Exception, URISyntaxException, HttpClientException {
        client1.setURI(new URI("http://google.com"));
        String response = client1.get("/images", false);
        assertThat(response).isNotNull();
    }

    @Test
    public void makeJsonRequest() throws HttpClientException, URISyntaxException, Exception {
        client2.setURI(new URI("http://jsonplaceholder.typicode.com"));
        HttpClientResponse<JsonObject> resp = client2.getJson("/todos/1");
        JsonObject json = resp.getContent();
        String title = json.get("title").getAsString();
        assertThat("delectus aut autem".equals(title)).isTrue();
    }

    @Test
    public void testResuableClient() throws Exception {
        reusableClient.setURI(new URI("http://google.com"));
        String response = reusableClient.get("/images", false);
        assertThat(response).isNotNull();

        reusableClient.setURI(new URI("http://jsonplaceholder.typicode.com"));
        HttpClientResponse<JsonObject> resp = reusableClient.getJson("/todos/1");
        JsonObject json = resp.getContent();
        String title = json.get("title").getAsString();
        assertThat("delectus aut autem".equals(title)).isTrue();
    }

    @Test
    public void downLoadFileTest()
            throws URISyntaxException, HttpClientException, UnsupportedOperationException, IOException {
        boolean fileExists = false;
        File f = new File("/tmp/jenkins.hpi");

        client3.setURI(new URI("https://resources.galasa.dev"));

        InputStream in = client3.getFile("/jenkins.hpi").getEntity().getContent();
        OutputStream out = new FileOutputStream(f);

        int count;
        byte data[] = new byte[2048];
        while((count = in.read(data)) != -1) {
            out.write(data, 0, count);
        }
        out.flush();
        out.close();

        
        if (f.exists() && !f.isDirectory() && f.getTotalSpace()>0) {
            fileExists = true;
        }
        assertThat(fileExists).isTrue();

        f.delete();
    }
}
