/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation allows the user to set a single SIT parm to be used by one
 * or all the regions in their SEM environment. 
 * 
 * To set multiple SITs see {@link @Sits}.
 * <p>
 * Usage:
 * <br>
 * 	{@link @Sit}(parameter="SITPARM1",value="1",cicsTag="A")<br>
 * or<br>
 * 	{@link @Sit}(parameter="SITPARM2",value="TWO")<br>
 * </p>
 * 
 * @author Michael Baylis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Sit {

	public String cicsTag() default "UNTAGGED";
	public String parameter();
	public String value();

}
