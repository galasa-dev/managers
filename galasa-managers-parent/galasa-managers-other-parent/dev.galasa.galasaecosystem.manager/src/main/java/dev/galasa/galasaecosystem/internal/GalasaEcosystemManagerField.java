package dev.galasa.galasaecosystem.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to identify manager controlled annotated fields
 * 
 * @author Michael Baylis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GalasaEcosystemManagerField {

}