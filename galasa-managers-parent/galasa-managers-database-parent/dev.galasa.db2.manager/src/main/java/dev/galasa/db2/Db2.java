package dev.galasa.db2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Db2ManagerField
@ValidAnnotatedFields({ IDb2.class })
public @interface Db2 {
	String tag() default "PRIMARY";
}
