/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
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
@ValidAnnotatedFields({ IDockerVolume.class })
@DockerManagerField
public @interface DockerVolume {

    /**
     * If a volume name is passed, the mount will be read only. If no name is passed then a volume name will be generated
     * This generated volume will be labeled and monitored after the test has finished and cleaned up by a user defined
     * wait duration.
     * 
     * @return
     */
    public String volumeName() default "";

    /**
     * Where to mount the volume on the container.
     * 
     * @return
     */
    public String mountPath();

    /**
     * The <code>dockerEngineTag</code> will be used in the future so that a volume can be allocated on a specific Docker Engine type.
     * You would not normally need to provide a Docker Engine tag.
     * 
     * @return
     */
    public String dockerEngineTag() default "PRIMARY";

    /**
     * The volume will not be removed by the end of test class, but later from resource management. Default will be 24 hours until removed 
     * but is user defined.
     * 
     * @return
     */
    public boolean persist() default false;


}