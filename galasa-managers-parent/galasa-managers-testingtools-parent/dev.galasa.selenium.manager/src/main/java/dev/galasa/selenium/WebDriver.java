/*
* Copyright contributors to the Galasa project 
*/
package dev.galasa.selenium;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@SeleniumManagerField
@ValidAnnotatedFields({ IWebDriver.class })
public @interface WebDriver {
    public Browser browser() default Browser.NOTSPECIFIED;
}