### Create a Docker Container

The following snippet shows the minimum code that is required to request a Docker Container in a Galasa test:

```
@Dockercontainer(image="library/httpd:latest", tag="http", start=true)
public IDockercontainer container1;
```

The code creates a Docker Container with an Apache HTTP Server running on port 80. Although this does not provide much, it does give a known target HTTP Server that you can start and stop in order to test how your application responds in those circumstances.  By accessing the *container1* field, you can find the IP address and port that was used for the container. 


At the end of the test, the Docker Manager automatically stops and discards the Docker Container. If for some reason the test was not able to do this, the Docker Manager resource management routines perform the same clean up after the Galasa Ecosystem discovers the test has disappeared.

There is no limit in Galasa on how many Docker Containers can be used within a single test. The only limit is the number of Docker Containers that can be started in the Galasa Ecosystem. This limit is set by the Galasa Administrator and is typically set to the maximum number of containers that can be supported by the Docker Server or Swarm.  If there are not enough slots available for an automated run, the run is put back on the queue in *waiting* state to retry. Local test runs fail if there are not enough container slots available.


### Obtain the IP address and port of an exposed container port

Find the IP address and port by using the following code which provisions and starts an Apache HTTP server on port 80:

```
@Dockercontainer(image="library/httpd:latest")
public IDockercontainer httpcontainer;
...
InetSocketAddress port80 = httpContainer.getFirstSocketForExposedPort(80);
```


### Stop and Start a container

Stop and start your Apache HTTP Server to test how your application responds by using the following code:

```
@Dockercontainer(image="library/httpd:latest")
public IDockercontainer httpcontainer;
...
httpContainer.stop();

httpContainer.start();
```

### Run a command in the container

Use the following code to execute a command within the Docker Container and return the resulting output:
```
@Dockercontainer(image="library/httpd:latest")
public IDockercontainer httpcontainer;
...
IDockerExec exec = httpContainer.exec("ls","-l","/var/log");
exec.waitForExec();
String output = exec.getCurrentOutput();
```

### Retrieve the log of the container

Use the following code to retrieve the container log:

```
@Dockercontainer(image="library/httpd:latest")
public IDockercontainer httpcontainer;
...
String log = httpContainer.getStdOut();
```