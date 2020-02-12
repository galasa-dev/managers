/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zosbatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * zOS Batch Manager
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosBatch</code> annotation will request the zOS Batch Manager to provide a
 * zOS Batch instance associated with a zOS image. 
 * The test can request multiple zOS Batch instances with the default being associated with the <b>primary</b> zOS image.<br>
 * At test end, the manager will store job output with the test results archive and remove jobs from the JES queue. 
 * 
 * @galasa.examples 
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosBatch(imageTag="A")<br>
 * public IZosBatch zosBatchA;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosBatch</code> interface has a single method, {@link IZosBatch#submitJob(String, IZosBatchJobname)} to submit a JCL 
 * as a <code>String</code> and returns a <code>IZosBatchJob</code> instance.<br><br>
 * See {@link ZosBatch}, {@link IZosBatch} and {@link IZosBatchJob} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@ZosBatchField
@ValidAnnotatedFields({ IZosBatch.class })
public @interface ZosBatch {
    
    /**
     * The <code>imageTag</code> is used to identify the zOS image.
     */
    String imageTag() default "primary";
}
