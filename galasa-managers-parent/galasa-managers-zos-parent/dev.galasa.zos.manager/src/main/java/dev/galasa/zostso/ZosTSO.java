/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zostso;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * z/OS TSO 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosTSO</code> annotation requests the z/OS Manager to provide a
 * z/OS TSO instance associated with a z/OS image. 
 * The test can request multiple z/OS TSO instances, with the default being associated with the <b>primary</b> z/OS image.<br>
 * 
 * @galasa.examples 
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosTSO(imageTag="A")<br>
 * public IZosTSO zosTSOA;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosTSO</code> interface provides the method {@link IZosTSO#issueCommand(String)} to issue a command to z/OS TSO
 * and returns a <code>IZosTSOCommand</code> instance.<br><br>
 * See {@link ZosTSO}, {@link IZosTSO} and {@link IZosTSOCommand} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosTSOField
@ValidAnnotatedFields({ IZosTSO.class })
public @interface ZosTSO {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     */
    String imageTag() default "primary";
}
