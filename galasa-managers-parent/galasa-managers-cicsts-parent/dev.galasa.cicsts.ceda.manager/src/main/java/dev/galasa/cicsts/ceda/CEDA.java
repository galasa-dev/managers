<<<<<<< HEAD
=======
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
package dev.galasa.cicsts.ceda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
<<<<<<< HEAD

import dev.galasa.cicsts.ceda.internal.CEDAManagerField;
=======
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zos3270.ITerminal;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@CEDAManagerField
@ValidAnnotatedFields({ ICEDA.class })
public @interface CEDA {
    
}