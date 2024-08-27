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

<details><summary>Issue a TSQ WRITEQ command</summary>

The following snippet shows the code required to issue the a TSQ WRITEQ command. 

In this case, the test will write a message to the TSQ named GALASAQ from the variable writeMessage:

```
tsq.setName("GALASAQ");

String writeMessage = "Write this message to TSQ name GALASAQ";
tsq.writeQ(writeMessage);

```
</details>

<details><summary>Issue a TSQ READQ command</summary>

The following snippet shows the code required to issue the a TSQ READQ command. 

In this case, the test will read a message from the TSQ named GALASAQ based in the item number passed into the variable readMessage:

```
tsq.setName("GALASAQ");

int itemNum = 1;
String readMessage = tsq.readQ(itemNum);

```
</details>

<details><summary>Issue a TSQ DELETEQ command</summary>

The following snippet shows the code required to issue the a TSQ DELETEQ command. 

In this case, the test will delete the TSQ named GALASAQ:

```
tsq.setName("GALASAQ");

String readMessage = tsq.deleteQ();

```
</details>

<details><summary>Make a TSQ recoverable</summary>

The following snippet shows the code required to make a TSQ recoverable. 

In this case, the test will make the TSQ named GALASAQ recoverable:

```
tsq.setName("GALASAQ");

String readMessage = tsq.makeRecoverable();

```
</details>

