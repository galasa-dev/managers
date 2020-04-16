/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosunix;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * z/OS UNIX 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosUNIX</code> annotation requests the z/OS Manager to provide a
 * z/OS UNIX instance associated with a z/OS image. 
 * The test can request multiple z/OS UNIX instances, with the default being associated with the <b>primary</b> z/OS image.<br>
 * 
 * @galasa.examples 
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosUNIX(imageTag="A")<br>
 * public IZosUNIX zosUNIXA;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosUNIX</code> interface provides the method {@link IZosUNIX#issueCommand(String)} to issue a command to z/OS UNIX
 * and returns a <code>IZosUNIXCommand</code> instance.<br><br>
 * See {@link ZosUNIX}, {@link IZosUNIX} and {@link IZosUNIXCommand} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosUNIXField
@ValidAnnotatedFields({ IZosUNIX.class })
public @interface ZosUNIX {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     */
    String imageTag() default "primary";
}
