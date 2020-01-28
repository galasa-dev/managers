/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.docker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.docker.internal.DockerManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IDockerEngine.class })
@DockerManagerField
public @interface DockerEngine {

    public String dockerEngineTag() default "PRIMARY";

}