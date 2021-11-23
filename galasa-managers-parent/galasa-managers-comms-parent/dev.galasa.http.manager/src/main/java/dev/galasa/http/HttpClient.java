/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.http.internal.HttpManagerField;

/**
 * archiveHeaders attribute allows for the headers of 
 * a request to be logged in the RAS
 *
 * @author William Yates
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@HttpManagerField
@ValidAnnotatedFields({ IHttpClient.class })
public @interface HttpClient {
	boolean archiveHeaders() default false;

}
