<<<<<<< HEAD
=======
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
package dev.galasa.cicsts.cemt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

<<<<<<< HEAD
import dev.galasa.cicsts.cemt.internal.CEMTManagerField;
=======
import dev.galasa.cicsts.cemt.CEMTManagerField;
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
import dev.galasa.framework.spi.ValidAnnotatedFields;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@CEMTManagerField
@ValidAnnotatedFields({ ICEMT.class })
public @interface CEMT {

}
