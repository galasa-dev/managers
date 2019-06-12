package dev.voras.common.zosbatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requests a unique provisioned Jobname for running Jobs or STCs on a zOS Image.
 * 
 * <p>Used to populate a {@link IJobname} field</p>
 * 
 * @author Michael Baylis
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Jobname {
}
