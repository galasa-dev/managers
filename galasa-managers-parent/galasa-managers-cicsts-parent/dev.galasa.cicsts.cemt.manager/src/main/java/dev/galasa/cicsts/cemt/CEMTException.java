/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.cicsts.cemt;

public class CEMTException extends CEMTManagerException {
   private static final long serialVersionUID = 1L;
   
   public CEMTException() {
   }

   public CEMTException(String message) {
       super(message);
   }

   public CEMTException(Throwable cause) {
       super(cause);
   }

   public CEMTException(String message, Throwable cause) {
       super(message, cause);
   }

   public CEMTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
       super(message, cause, enableSuppression, writableStackTrace);
   }
}
