/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.zosprogram.internal.ZosProgramManagerField;

/**
 * z/OS Program
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}ZosProgram</code> annotation requests the z/OS Program Manager to Compile and Bind a
 * program on a z/OS image. 
 * The test can request multiple z/OS Program instances
 * 
 * @galasa.examples 
 * {@literal @}ZosImage(imageTag="A")<br>
 * public IZosImage zosImageA;<br>
 * {@literal @}ZosProgram(imageTag="A")<br>
 * public IZosProgram zosProgramA;<br></code>
 * 
 * @galasa.extra
 * The <code>IZosProgram</code> interface has a number of methods to manage the zOS Program.
 * See {@link ZosProgram} and {@link IZosProgram} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ZosProgramManagerField
@ValidAnnotatedFields({ IZosProgram.class })
public @interface ZosProgram {
    
    /**
     * The program name (without file extension)
     */
    String name();
    
    /**
     * The  programming language. See {@link ZosProgram.Language}
     */
    Language language();
    
    /**
     * The load module data set name
     */
    String loadlib() default "";
    
    /**
     * The <code>imageTag</code> is used to identify the z/OS image.
     */
    String imageTag() default "primary";

    /**
     * Enumeration of supported languages
     */
    public enum Language{
        /**
         * COBOL program with <code>.cbl</code> file extension 
         */
        COBOL(".cbl"),
//        /**
//         * C program with <code>.c</code> file extension 
//         */
//        C(".c"),
//        /**
//         * PL1 program with <code>.pl1</code> file extension 
//         */
//        PL1(".pl1"),
//        /**
//         * Assembler program with <code>.asm<\code> file extension 
//         */
//        ASSEMBLER(".asm"),
//        /**
//         * Assembler 64 program with <code>.asm<\code> file extension 
//         */
//        ASSEMBLER_64(".asm"), 
        /**
         * Not intended to be specified, used as a default value where the language should be inferred from a file extension
         */
        NONE("");
        
        private final String extension;
        
        private Language(String extension) {
            this.extension = extension;
        }
        
        public String getFileExtension(){
            return extension;
        }
        
        public static Language fromExtension(String extension) {
            
            for (Language l : values()) {
                if (l.extension.equalsIgnoreCase(extension)) {
                    return l;
                }
            }
            
            throw new IllegalArgumentException("Extension " + extension + " does not match supported languages");
        }
    }
}
