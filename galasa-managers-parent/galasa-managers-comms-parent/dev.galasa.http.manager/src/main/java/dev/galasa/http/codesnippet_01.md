<details><summary>Instantiate an HTTP Client</summary>

This code instantiates an HTTP Client.

```java
@Httpclient
public IHttpClient client;
```

You can just as simply instantiate multiple HTTP Clients.

```java
@Httpclient
public IHttpClient client1;

@Httpclient
public IHttpClient client2;
```

</details>

<details><summary>Set the target URI for an HTTP Client</summary>

This code sets an HTTP Client's target URI.

```java
@Httpclient
public IHttpClient client;

client.setURI("http://www.google.com");
```

You would typically use this call prior to, say, an outbound HTTP call
to retrieve the contents of a web page.

</details>

<details><summary>Make an outbound HTTP call</summary>

This code makes a get request to the given path.

```java
@Httpclient
public IHttpClient client;

String pageContent = client.get("/images",false);
```

Use this call after a prior call to `setURI` to establish the URI endpoint of your request.
The second parameter - a boolean - causes the function to retry as required if set to `true`.

</details>

<details><summary>Use streams to download a file</summary>
The following code is an example of one way to download a file using streams.

```java
@Httpclient
public IHttpClient client;

File f = new File("/tmp/dev.galasa_0.3.0.jar");

client.setURI(new URI("https://p2.galasa.dev"));
CloseableHttpResponse response = client.getFile("/plugins/dev.galasa_0.3.0.jar");
InputStream in = response.getEntity().getContent();
OutputStream out = new FileOutputStream(f);
int count;
byte data[] = new byte[2048];
while((count = in.read(data)) != -1) {
   out.write(data, 0, count);
}
out.flush();
out.close();
```

The snippet begins by declaring `client` as before and `f`, an instance of `File`. The client's URI is set and its `getFile` method called to return `response` - an instance of `CloseableHttpResponse`.

The two streams `in` and `out` are declared and initialized and the data transferred from one to the other in 2048 byte chunks, after which the output stream is flushed and then closed.

</details>
