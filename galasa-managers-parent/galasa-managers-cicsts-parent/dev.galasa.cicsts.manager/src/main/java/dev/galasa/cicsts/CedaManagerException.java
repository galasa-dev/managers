/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts;

public class CedaManagerException extends CicstsManagerException {
   
   private static final long serialVersionUID = 1L;
   
   public CedaManagerException() {}
   
   public CedaManagerException(String message) {
        super(message);
   }
   
   public CedaManagerException(Throwable cause) {
      super(cause);
   }
   
   public CedaManagerException(String message, Throwable cause) {
      super(message, cause);
   }
   
   public CedaManagerException(String message, Throwable cause, boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
   
   
   
}
