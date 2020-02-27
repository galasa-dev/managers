/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosconsole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * z/OS Console Manager
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosConsole</code> annotation requests the z/OS Console Manager to provide a
 * z/OS Console instance associated with a z/OS image. 
 * The test can request multiple z/OS Console instances, with the default being associated with the <b>primary</b> z/OS image.<br>
 * 
 * @galasa.examples 
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosConsole(imageTag="A")<br>
 * public IZosConsole zosConsoleA;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosConsole</code> interface has two methods, {@link IZosConsole#issueCommand(String)} and
 * {@link IZosConsole#issueCommand(String, String)} to issue a command to the z/OS console and returns a <code>IZosConsoleCommand</code>
 * instance.<br><br>
 * See {@link ZosConsole}, {@link IZosConsole} and {@link IZosConsoleCommand} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosConsoleField
@ValidAnnotatedFields({ IZosConsole.class })
public @interface ZosConsole {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     */
    String imageTag() default "primary";
}
