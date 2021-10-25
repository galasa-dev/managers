/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.mq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.mq.internal.MqManagerField;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@MqManagerField
@ValidAnnotatedFields({ IMessageQueueManager.class })
public @interface QueueManager {

    String queueMgrTag();

}
