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
 * This annotation will allow the tester to provide multiple {@link @Sit} annotations.
 * <p>
 * Usage:
 * <br>
 * 	{@link @Sits}({<br>
 * 		{@link @Sit}(parameter="SITPARM1",value="1",tag="A"),<br>
 * 		{@link @Sit}(parameter="SITPARM2",value="TWO")<br>
 * 	})
 * </p>
 * 
 * @author Michael Baylis
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Sits {

	public Sit[] value();
}
