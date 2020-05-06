<details><summary>Instantiate an HTTP Client</summary>

This code instantiates an HTTP Client.

```
@Httpclient
public IHttpClient client;
```

You can just as simply instantiate multiple HTTP Clients.

```
@Httpclient
public IHttpClient client1;

@Httpclient
public IHttpClient client2;
```

</details>

<details><summary>Set the target URI for an HTTP Client</summary>

This code sets an HTTP Client's target URI.

```
client.setURI("http://www.google.com");
```

You would typically use this call prior to, say, an outbound HTTP call
to retrieve the contents of a web page.

</details>

<details><summary>Make an outbound HTTP call</summary>

This code makes a get request to the given path.

```
String pageContent = client.get("/images",false);
```

Use this call after a prior call to `setURI` to establish the URI endpoint of your request.
The second parameter - a boolean - causes the function to retry as required if set to `true`.

</details>
