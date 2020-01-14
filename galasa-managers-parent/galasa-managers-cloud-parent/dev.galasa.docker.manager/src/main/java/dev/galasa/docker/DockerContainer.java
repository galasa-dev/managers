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

    public String dockerContainerTag() default "a";

    public String image();

    public boolean start() default true;

}