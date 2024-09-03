<details><summary>Request a TSQ Factory instance</summary>

The following snippet shows the code that is required to request a TSQ Factory instance in a Galasa test:

```
@CicsRegion()
public ICicsRegion cicsRegion;
public ITsqFactory tsqFactory; 

...

this.tsqFactory = cicsRegion.getTSQFactory();

```

</details>

<details><summary>Create a new ITsq object with a queue name</summary>

The following snippets show the code required to create a new ITsq object. 

In this case, the test will create an ITsq object with queue named GALASAR. The TSQ created using this object will be a recoverable queue.  

```
public ITsq tsqRecoverable;

...

// Create ITsq object for recoverable TSQ
String queueName = "GALASAR";
boolean recoverable = true;
tsqRecoverable = this.tsqFactoryA.createQueue(queueName, recoverable);

```

In this case, the test will create an ITsq object with queue named GALASAN. The TSQ created using this object will be a non-recoverable queue. 

```
public ITsq tsqNonRecoverable;

...

// Create ITsq object for non-recoverable TSQ
String queueName = "GALASAN";
boolean recoverable = false;
tsqNonRecoverable = this.tsqFactoryA.createQueue(queueName, recoverable);

```
Note: If TSQ is already existing then the recoverable status will not change. 

</details>

<details><summary>Write to TSQ</summary>

The following snippet shows the code required to write data to a TSQ. 

In this case, the test will write the data contained in the variable named writeMessage to the TSQ named GALASAN:

```
// Note: Here ITsq object - tsqNonRecoverable is created with queueName = "GALASAN"

String writeMessage = "Write this message to TSQ named GALASAN.";
tsqNonRecoverable.writeQueue(writeMessage);

```

</details>

<details><summary>Read from TSQ</summary>

The following snippet shows the code required to read data from a TSQ. 

In this case, the test will read a message into the variable named readMessage, from the TSQ named GALASAN based on the item number passed :

```
// Note: Here ITsq object - tsqNonRecoverable is created with queueName = "GALASAN"
int itemNumber = 1;
String readMessage =tsqNonRecoverable.readQueue(itemNumber);

```

In this case, the test will read the next message from the TSQ named GALASAN.  Here the variable - readMessage2 will have item number 2 and variable - readMessage3 will have item number 3  :

```
String readMessage2 = tsqNonRecoverable.readQueueNext();
String readMessage3 = tsqNonRecoverable.readQueueNext();

```
</details>

<details><summary>Delete a TSQ</summary>

The following snippet shows the code required to delete a non-recoverable TSQ. 

In this case, the test will delete the TSQ named GALASAN:

```
// Note: Here ITsq object - tsqNonRecoverable is created with queueName = "GALASAN"
tsqNonRecoverable.deleteQueue();

```
</details>

<details><summary>Check if a TSQ is existing</summary>

The following snippet shows the code required to check the existence of a TSQ. 

In this case, the test will check the existence of the TSQ named GALASAN. The variable - response will be true if the TSQ is existing else it will be false:

```
// Note: Here ITsq object - tsqNonRecoverable is created with queueName = "GALASAN"
boolean response = tsqNonRecoverable.exists();

```
</details>


<details><summary>Check if a TSQ is recoverable</summary>

The following snippet shows the code required to check if a TSQ is recoverable. 

In this case, the test will check the TSQ named GALASAN if it is recoverable or not. The variable - response will be true if the TSQ is recoverable else it will be false:

```
// Note: Here ITsq object - tsqNonRecoverable is created with queueName = "GALASAN"
boolean response = tsqNonRecoverable.isRecoverable();

```
</details>