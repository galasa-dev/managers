<details><summary>Request a TSQ instance</summary>

The following snippet shows the code that is required to request a TSQ instance in a Galasa test:

```
@CicsRegion()
public ICicsRegion cicsRegion;

...

ITsq tsq = cicsRegion.tsq();
```

The code creates a CICS/TS TSQ instance. 

</details>

<details><summary>Create and write to TSQ</summary>

The following snippet shows the code required to create and write to a TSQ. 

In this case, the test will create a TSQ named GALASAQ and write data in the variable writeMessage.  

Response will be: 
- OK                : if new TSQ is created and data is written
- ALREADY_EXISTING  : if TSQ is already existing and data is written

```

String queueName = "GALASAQ";
String writeMessage = "Write this message to TSQ named GALASAQ";
String response = tsq.createQueue(queueName, writeMessage);

```

The following snippet shows the code required to create and write to a recoverable TSQ. 

In this case, the test will create a recoverable TSQ named GALASAQ (if not already existing) and write data from the variable writeMessage. 

Response will be: 
- OK                : if new TSQ is created and data is written
- ALREADY_EXISTING  : if TSQ is already exiting and data is written

```

String queueName = "GALASAQ";
String writeMessage = "Write this message to TSQ named GALASAQ";
boolean recoverable = true;
String response = tsq.createQueue(queueName, writeMessage, recoverable);

```
Note: If TSQ is already existing and is non-recoverable, then the TSQ needs to be deleted and then created with recoverable option to make it recoverable. Otherwise the TSQ will continue to be non-recoverable. 

</details>

<details><summary>Write to TSQ</summary>

The following snippet shows the code required to write data to a TSQ. 

In this case, the test will write the data contained in the variable named writeMessage to the TSQ named GALASAQ:

```

String queueName = "GALASAQ";
String writeMessage = "Write this message to TSQ named GALASAQ";
tsq.writeQueue(queueName, writeMessage);

```
</details>

<details><summary>Read from TSQ</summary>

The following snippet shows the code required to read data from a TSQ. 

In this case, the test will read a message into the variable named readMessage, from the TSQ named GALASAQ based on the item number passed :

```

String queueName = "GALASAQ";
int itemNum = 1;
String readMessage = tsq.readQueue(queueName, itemNum);

```

In this case, the test will read the next message from the TSQ named GALASAQ into the variable named readMessage:

```

String queueName = "GALASAQ";
String readMessage = tsq.readQueueNext(queueName);

```
</details>

<details><summary>Delete a TSQ</summary>

The following snippet shows the code required to delete a TSQ. 

In this case, the test will delete the TSQ named GALASAQ:

```

String queueName = "GALASAQ";
tsq.deleteQueue(queueName);

```
</details>
