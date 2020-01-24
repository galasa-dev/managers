/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.http.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;

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
    public Log         logger;

    @HttpClient
    public IHttpClient client1;

    @HttpClient
    public IHttpClient client2;

    @HttpClient
    public IHttpClient client3;

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

    // @Test
    // public void downLoadFileTest() throws URISyntaxException {
    //     boolean fileExists = false;
    //     client3.setURI(new URI("https://p2.galasa.dev"));
    //     //client3.setAuthorisation("username", "password");
    //     client3.getFile(Paths.get("/tmp/dev.galasa_0.3.0.jar"), "/plugins/dev.galasa_0.3.0.jar");
    //     File f = new File("/tmp/dev.galasa_0.3.0.jar");
    //     if (f.exists() && !f.isDirectory()) {
    //         fileExists = true;
    //     }
    //     assertThat(fileExists).isTrue();

    //     f.delete();
    // }
}
