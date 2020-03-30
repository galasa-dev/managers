<details><summary>Create a JMeter session</summary>

The following snippet shows the minimum code that is required to request a Docker Container in a Galasa test:

```
@JMeterSession(jmxPath="test.jmx")
public IJMeterSession session;
```

This code will provision a container for you that comes installed with all the necessary JMeter binaries to perform a fully-fledged JMX-test. Within your test you will need to provide a JMX-file of your choosing to your test. The test-file is able to be provisioned through the use of the Artifact Manager and to point the Bundleresources with the specific inputstream at you JMX-file. 

The specific container with jmeter will be provisioned by the DockerManager and will be discarded when your tests end. 

The next snippet shows the possible addition you can make to the JMeterSession by adding a personal properties-file to the test. You can achieve this by again using the Artifactmanager and pointing it at your personal properties-file.

```
@JMeterSession(jmxPath="test.jmx", propPath="jmeter.properties")
public IJMeterSession session;
```


There is no limit in Galasa on how many JMeter sessions can be used within a single test. The only limit is the number of Containers that can be started in the Galasa Ecosystem. This limit is set by the Galasa Administrator and is typically set to the maximum number of containers that can be supported by the Docker Server or Swarm.  If there are not enough slots available for an automated run, the run is put back on the queue in *waiting* state to retry. **Local test runs fail if there are not enough container slots available.**
</details>

<details><summary>Setting your actual JMX-file in the session with artifact manager</summary>


```
    IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
    InputStream jmxStream = bundleResources.retrieveFile("/test.jmx");
    session2.setJmxFile(jmxStream);
```
</details>

<details><summary>Setting your actual properties-file in the session with artifact manager</summary>

This will kill of the session by simply sending a kill-signal towards the JMeter container and removing it from the running sessions.

```
    IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
    InputStream propStream = bundleResources.retrieveFile("/jmeter.properties");
    session.applyProperties(propStream);
```
</details>

<details><summary>Starting up the JMeter session</summary>

You are able to attach a specific timeout to the session or use the *default timeout of 60 seconds* towards the JMeter session. This command will only be succesfully executed if you have set your JMX-file properly using the `session.setJmxFile(inputStream)`-method. *Timeout is in milli-seconds.*

```
    session.startJmeter();
    ...
     session.startJmeter(60000);
```
</details>

<details><summary>Obtaining the JMX-file from the JMeter-execution as a String</summary>

The following snippet allows the tester to access the used JMX file in the JMeter session.

```
session.getJmxFile();
```
</details>

<details><summary>Obtaining the Logfile from the JMeter-execution as a String</summary>

The following snippet allows the tester to access the execution logFile that is created upon execution of the JMX-file inside the container.

```
session.getLogFile();
```
</details>

<details><summary>Obtaining the console output as a String</summary>

The following snippet allows the tester to see the console output of the JMeter container. In most cases there will be no output towards the console except when the JMX-file itself is corrupt or wrongly written. Otherwise if a correctly written JMX-file endures errors during execution this will be represented in the log files or the JTL-file.

```
session.getConsoleOutput();
```
</details>

<details><summary>Obtaining any specific generated file from the JMeter-execution as a String</summary>

The following snippet allows the tester to access the execution any file that is created upon execution of the JMX-file inside the container. In this example you can retrieve the JTL-file as a String containing the actual execution results made ready for a CSV (the name of this JTL-file has the same prefix as your JMX-file).

```
session.getListenerFile("test.jtl")
```
</details>


<details><summary>Obtaining a basic form of making sure your JMX-file executed correctly</summary>

The following snippet allows the tester to make sure his test actually got performed to a basic degree. Further analysis can be done personally by the tester by retrieving the logs  and/ or JTL-files. The boolean will return true if the JMX-file has performed its funciton properly, otherwise it would return false.

```
session.statusTest();
```
</details>

<details><summary>Killing of the session</summary>

This will kill of the session by simply sending a kill-signal towards the JMeter container and removing it from the running sessions.

```
session.stopTest();
```
</details>
