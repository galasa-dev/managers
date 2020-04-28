/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosmf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zosmf.internal.ZosmfManagerField;

/**
 * z/OS MF 
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}Zosmf</code> annotation requests the z/OSMF Manager to provide a
 * z/OSMF instance associated with a z/OS image. 
 * The test can request multiple z/OSMF instances, with the default being associated with the <b>primary</b> zOS image.
 * 
 * @galasa.examples 
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}Zosmf(imageTag="A")<br>
 * public IZosmf zosmfA;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosmf</code> interface has a number of methods to issue requests to the zOSMF REST API.
 * See {@link Zosmf} and {@link IZosmf} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ZosmfManagerField
@ValidAnnotatedFields({ IZosmf.class })
public @interface Zosmf {
    
    /**
     * The tag of the zOS Image this variable is to be populated with
     */
    String imageTag() default "primary";

}
