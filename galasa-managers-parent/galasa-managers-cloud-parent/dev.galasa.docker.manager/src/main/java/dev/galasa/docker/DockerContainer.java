package dev.galasa.docker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.docker.internal.DockerManagerField;

/**
 * Docker Container
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}DockerContainer</code> annotation will request the Docker Manager allocate a slot and start a container 
 * on the infrastructure Docker Engines.  The test can request as many Containers as required that 
 * can be supported simultaneously by the Docker Manager configuration.
 * 
 * @galasa.examples 
 * <code>{@literal @}DockerContainer(image="library/httpd:latest")<br>
 * public IDockerContainer httpdContainer;<br>
 * {@literal @}DockerContainer(image="privateimage", start=false)<br>
 * public IDockerContainer container1;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IDockerContainer</code> interface gives the test access to the IPv4/6 address and the exposed port numbers of the Docker Container. 
 * The interface also enables the test to execute commands and retrieve the log and transfer files that are sent to 
 * and from the Container.<br><br>
 * See {@link DockerContainer} and {@link IDockerContainer} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IDockerContainer.class })
@DockerManagerField
public @interface DockerContainer {

    /**
     * The <code>tag</code> is used to identify the Docker Container to other Managers or Shared Environments.  If a test is using multiple 
     * Docker Containers, each separate Docker Container must have a unique tag.  If two DockerContainers use the same tag, they will refer to the 
     * same actual Docker Container.
     */
    public String dockerContainerTag() default "a";

    /**
     * The <code>image</code> attribute provides the Docker Image that is used to create the Docker Container.  It image name must not 
     * include the Docker Registry as this is provided in the CPS.   If using a public official image from DockerHub,  then the 
     * image name must be prefixed with <code>library/</code>, for example <code>library/httpd:latest</code>, the Docker Manager will
     * not default to the library namespace like the Docker commands do.
     */
    public String image();

    /**
     * The <code>start</code> attribute indicates whether the Docker Container should be started automatically.   If the 
     * Test needs to perform some work before the Container is started, then <code>start=false</code> should be used, after which 
     * <code>IDockerContainer.start()</code> can be called to start the Container.
     */
    public boolean start() default true;

    /**
     * Dont think we need this attribute
     */
    public String DockerEngineTag() default "default";

}