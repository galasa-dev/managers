package dev.galasa.http.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.http.HttpClient;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;

@Test
public class HttpManagerIVT {

    @Logger
    public Log logger;

    @HttpClient
    public IHttpClient client;

    @Test
    public void checkNotNull(){
        assertThat(logger).isNotNull();
        assertThat(client).isNotNull();
    }
    @Test
    public void getTests() throws Exception{
        client.setURI(new URI("https://httpbin.org"));
        String sResponse = client.getText("/get").getContent();
        assertThat(sResponse).startsWith("{");
        assertThat(sResponse).contains("\"url\": \"https://httpbin.org/get\"");

        JsonObject jResponse = client.getJson("/get").getContent();
        assertThat(jResponse.get("url").getAsString()).isEqualTo("https://httpbin.org/get");


    }

    @Test
    public void headerTest() throws Exception{
        String headerName = "HobbitHeader";
        String headerValue = "Frodo";

        client.setURI(new URI("https://httpbin.org"));
        client.addCommonHeader(headerName, headerValue);
        JsonObject response = client.getJson("/get").getContent();
        assertThat(response.toString()).contains(headerName);
        assertThat(response.toString()).contains(headerValue);
    }

    @Test
    public void authTest() throws Exception{
        String user = "hobbit";
        String pword = "passw0rd";
        String path = "/basic-auth/" + user + "/" + pword;

        client.setAuthorisation(user, pword);
        HttpClientResponse<JsonObject> response = client.getJson(path);
        assertThat(response.getStatusCode()).isEqualTo(200);
        
        assertThat(response.getContent().get("authenticated").getAsBoolean()).isTrue();
        assertThat(response.getContent().get("user").getAsString()).isEqualTo(user);
        
    }

    @Test
    public void postTests() throws Exception {
        String key = "Project";
        String value = "Galasa";
        client.setURI(new URI("https://httpbin.org"));
        JsonObject json = new JsonObject();
        json.addProperty(key,value);
        HttpClientResponse<JsonObject> jResponse = client.postJson("/post", json);
        assertThat(jResponse.getStatusCode()).isEqualTo(200);
        logger.info(jResponse.getContent().toString());
        assertThat(jResponse.getContent().get("data").getAsString()).isEqualTo(json.toString());

        HttpClientResponse<String> sResponse = client.postText("/post", json.toString());
        assertThat(sResponse.getStatusCode()).isEqualTo(200);
        logger.info(sResponse.getContent().toString());
        assertThat(sResponse.getContent()).contains(key);
        assertThat(sResponse.getContent()).contains(value);
    }

    @Test
    public void downloadFileTest()
            throws Exception {
        boolean fileExists = false;
        File f = new File("/tmp/jenkins.hpi");

        client.setURI(new URI("https://resources.galasa.dev"));

        InputStream in = client.getFile("/jenkins.hpi").getEntity().getContent();
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