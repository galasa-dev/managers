/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@MqManagerField
@ValidAnnotatedFields({ IMessageQueueManager.class })
public @interface QueueManager {

    String tag() default "PRIMARY";

}
