package dev.galasa.docker.manager.ivt;

import org.apache.commons.logging.Log;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import dev.galasa.Test;
import dev.galasa.artifact.ArtifactManager;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.core.manager.Logger;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerExec;
import dev.galasa.http.HttpClient;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.IHttpClient;

/**
 * IVT for the Docker Manager
 * 
 * This IVT will use the standard Apache httpd Docker Image to ensure the 
 * basic functionality of the Docker Manager is working correctly.<br>
 * <br>
 * Outline of the IVT is:- <br>
 *  1) recycle the container to ensure it can be restarted <br>
 *  2) Store a test html file into the container<br>
 *  3) Issue a command to ensure the file is on the filesystem<br>
 *  4) Attempt to use http to retrieve the html file<br>
 *  5) Retrieve the console logs to ensure the html file was accessed<br>
 *  6) retrieve the stored file to ensure it comes back 
 * 
 * @author Michael Baylis
 *
 */
public class DockerManagerIVT {

    @Logger
    public Log logger;

    @DockerContainer(image = "library/httpd:latest", dockerContainerTag = "a", start=true)
    public IDockerContainer container;
    
    @ArtifactManager
    public IArtifactManager artifactManager;
    
    @HttpClient
    public IHttpClient httpClient;

    /**
     * Ensure a container was provisioned
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     */
    @Test
    public void checkDockerContainerNotNull() throws DockerManagerException {
        assertThat(container).as("Docker Container").isNotNull();
    }

    /**
     * Recycle the container to ensure it will restart after being bounced
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     */
    @Test
    public void startAndStopContainer() throws DockerManagerException {
        logger.info("Stopping the docker container");
        container.stop();
        logger.info("Starting the docker container");
        container.start();
    }
    
    /**
     * Store a test html file in the container for httpd to provide.  issue a ls command
     * to esnure it exists on the filesystem
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     * @throws TestBundleResourceException if the test html file cant be found
     */
    @Test
    public void storeFilesInContainer() throws DockerManagerException, TestBundleResourceException {
        IBundleResources bundleResources = artifactManager.getBundleResources(getClass());
        
        //  Retrieve the test html file
        InputStream isHtml =  bundleResources.retrieveFile("/test1.html");

        // Store it in the container 
        container.storeFile("/usr/local/apache2/htdocs/test1.html", isHtml);     
        
        // Check it is there via ls command
        IDockerExec exec = container.exec("/bin/ls","-l","/usr/local/apache2/htdocs/test1.html");
        assertThat(exec.waitForExec()).as("The waitForExec finished true").isTrue();
        
        String cmdResult = exec.getCurrentOutput();
        logger.info("Result from ls:-\n" + cmdResult);
        assertThat(cmdResult).contains("-rw");
    }
    
    /**
     * Retrieve the file from httpd and ensure the contents are correct
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     * @throws HttpClientException if there is a problem retrieving the file
     * @throws URISyntaxException if there is a problem with the host name of the exposed port
     */
    @Test
    public void retrieveHtml() throws DockerManagerException, HttpClientException, URISyntaxException {
       InetSocketAddress exposedPort = container.getFirstSocketForExposedPort("80/tcp");
       assertThat(exposedPort).as("Correctly retrieved the exposed port").isNotNull();
       
       URI uri = new URI("http://" + exposedPort.getHostName() + ":" + exposedPort.getPort());
       
       httpClient.setURI(uri);
       
       String html = httpClient.get("/test1.html");
       
       assertThat(html).as("Checking the HTML container the JAT constant text").contains("JAT Docker Test");
    }
    
    /**
     * Check to container logs to ensure the file was retrieved
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     */
    @Test
    public void retrieveContainerLog() throws DockerManagerException {
        String log = container.retrieveStdOut();
        logger.info("Container Log:-\n" + log);
        
        assertThat(log).as("checking that the test1.html was retrieved and logged").contains("\"GET /test1.html HTTP/1.1\" 200");
    }
    
    
    /**
     * Pull back the test file and see if it contains the test ext
     * 
     * @throws DockerManagerException if there is a problem with the docker manager
     */
    @Test
    public void retrieveFile() throws DockerManagerException {
       String htmlTest1 = container.retrieveFile("/usr/local/apache2/htdocs/test1.html");
        
       assertThat(htmlTest1).as("check we can pull back the file").contains("<h1>JAT Docker Test</h1>");
    }   
}