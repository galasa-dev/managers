
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.cicsts.ceda;

public class CEDAException extends CEDAManagerException {
   private static final long serialVersionUID = 1L;
   
   public CEDAException() {
   }

   public CEDAException(String message) {
       super(message);
   }

   public CEDAException(Throwable cause) {
       super(cause);
   }

   public CEDAException(String message, Throwable cause) {
       super(message, cause);
   }

   public CEDAException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
       super(message, cause, enableSuppression, writableStackTrace);
   }

   
   
}
