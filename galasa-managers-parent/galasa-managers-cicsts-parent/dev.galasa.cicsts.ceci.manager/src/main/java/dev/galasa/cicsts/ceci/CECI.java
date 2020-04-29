/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.ceci;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.cicsts.ceci.internal.CECIManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zos3270.ITerminal;

/**
 * CICS/TS CECI Manager
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}CECI</code> annotation will request the CICS/TS CECI Manager to provide a CECI instance.
 * 
 * @galasa.examples 
 * {@literal @}CECI<br>
 * public ICECI ceci;<br>
 * 
 * @galasa.extra
 * Requests to the <code>ICECI</code> Manager interface requires a {@link ITerminal} object which is logged on to CICS and is at 
 * the CECI initial screen.<br><br>
 * If mixed case is required, the terminal should be presented with no upper case translate status. For example, the test could first issue
 * <code>CEOT TRANIDONLY</code> to the {@link ITerminal} before invoking {@link ICECI} methods.<br><br>
 * See {@link CECI}, {@link ICECI} and {@link ITerminal} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@CECIManagerField
@ValidAnnotatedFields({ ICECI.class })
public @interface CECI {
    
}
