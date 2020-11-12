<<<<<<< HEAD
=======
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
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
