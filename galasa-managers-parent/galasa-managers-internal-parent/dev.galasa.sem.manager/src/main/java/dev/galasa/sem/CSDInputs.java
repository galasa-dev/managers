/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.sem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CSDInputs {

	public CSDInput[] value();
}
