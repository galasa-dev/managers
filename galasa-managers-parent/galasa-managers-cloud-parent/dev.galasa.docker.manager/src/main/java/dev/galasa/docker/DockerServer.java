package dev.galasa.docker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.docker.internal.DockerManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IDockerServer.class })
@DockerManagerField
public @interface DockerServer {

}