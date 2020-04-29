<details><summary>Request a zOS UNIX Command instance

The following snippet shows the code that is required to request a zOS UNIX Command instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosUNIX(imageTag="A")
public IZosUNIX unix;
```

The code creates a zOS UNIX Command instance associated with the zOS Image allocated in the *zosImageA* field.
</details>

<details><summary>Issue a zOS UNIX Command and retrieve the immediate response

Issue the zOS UNIX `date` Command and retrieve the response:

```
String unixCommandString = "date";
IZosUNIXCommand unixCommand = unix.issueCommand(unixCommandString);
String unixResponse = unixCommand.getResponse();
```

The String `response`  contains the value of the UNIX TIME command, e.g. 

```
Wed Apr 15 16:17:14 BST 2020
```
</details>