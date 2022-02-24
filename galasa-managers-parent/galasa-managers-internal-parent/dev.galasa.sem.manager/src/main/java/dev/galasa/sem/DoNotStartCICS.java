/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.sem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will inform the provisioning system (SEM)
 * not to start any CICS regions.
 *  
 * @author Ross Henderson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DoNotStartCICS {

}
