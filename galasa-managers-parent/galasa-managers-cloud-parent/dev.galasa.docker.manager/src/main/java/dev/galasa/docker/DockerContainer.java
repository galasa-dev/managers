package dev.galasa.docker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.docker.internal.DockerManagerField;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IDockerContainer.class })
@DockerManagerField
public @interface DockerContainer {

    public String dockerContainerTag() default "a";

    public String image();

    public boolean start() default true;

}