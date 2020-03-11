/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.jmeter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.jmeter.internal.JMeterManagerField;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@JMeterManagerField
@ValidAnnotatedFields({ IJMeterSession.class })
public @interface JMeterSession{
    /**
     * Provides the session with a specific jmxFile during provisioning
     */
    String jmxPath();
}