<details><summary>Request a zOS TSO Command instance

The following snippet shows the code that is required to request a zOS TSO Command instance in a Galasa test:

```
@ZosImage(imageTag="A")
public IZosImage zosImageA;

@ZosTSO(imageTag="A")
public IZosTSO tso;
```

The code creates a zOS TSO Command instance associated with the zOS Image allocated in the *zosImageA* field.
</details>

<details><summary>Issue a zOS TSO Command and retrieve the immediate response

Issue the zOS TSO `TIME` Command and retrieve the response:

```
String tsoCommandString = "TIME";
IZosTSOCommand tsoCommand = tso.issueCommand(tsoCommandString);
String tsoResponse = tsoCommand.getResponse();
```

The String `response`  contains the value of the TSO TIME command, e.g. 

```
IKJ56650I TIME-04:17:14 PM. CPU-00:00:00 SERVICE-290 SESSION-00:00:00 APRIL 15,2020
```
</details>