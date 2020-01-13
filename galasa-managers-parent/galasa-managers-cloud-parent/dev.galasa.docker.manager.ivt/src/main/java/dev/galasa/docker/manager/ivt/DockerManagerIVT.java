package dev.galasa.docker.manager.ivt;

import org.apache.commons.logging.Log;

import static org.assertj.core.api.Assertions.assertThat;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerExec;

public class DockerManagerIVT {

    @Logger
    public Log logger;

    @DockerContainer(image = "httpd:latest", dockerContainerTag = "a", start=false)
    public IDockerContainer container;
    @DockerContainer(image = "httpd:latest", dockerContainerTag = "b")
    public IDockerContainer container2;
    @DockerContainer(image = "httpd:latest", dockerContainerTag = "c")
    public IDockerContainer container3;
    
    @Test
    public void checkDockerContainerNotNull() throws DockerManagerException, IvtException {
        assertThat(container).as("Docker Container").isNotNull();
        assertThat(container2).as("Docker Container 2").isNotNull();
        assertThat(container3).as("Docker Container 3").isNotNull();
    }

    @Test
    public void startAndStopContainer() throws DockerManagerException {
        logger.info("Starting the docker container");
        container.start();
        logger.info("Stopping the docker container");
        container.stop();
        logger.info("Stopped with exit code: " + container.getExitCode());
    }

    @Test
    public void startExecCommandsOnContainerThenStop() throws DockerManagerException {
        logger.info("Starting the docker container");
        container.start();

        String[] cmds = {
            "mkdir /temp",
            "cd /temp",
            "touch test.txt",
            "cd /",
            "rm -rf /temp"
        };
        logger.info("Executing commands on container");
        IDockerExec commandExec = container.exec("ls");
        try {
            if (!commandExec.isFinished()) {
                logger.info("Still executing commands");
                Thread.sleep(10);
            }
            long ec = container.getExitCode();
            logger.info("Finished Exec with exit code: " + ec);
            logger.info("Containers stdout: " + container.retrieveStdOut());
        } catch (InterruptedException e) {
            throw new DockerManagerException("Failed during exec", e);
        }
        
        logger.info("Stopping the docker container");
        container.stop();
        logger.info("Cleanly stopped.");
    }

    @Test
    public void startMultipleContainers() throws DockerManagerException {
        logger.info("Starting 3 hello world containers");
        container.start();
        logger.info("Allowing framework to clean up. Please run `docker ps` to confirm that containers have been shutdown");
    }
}