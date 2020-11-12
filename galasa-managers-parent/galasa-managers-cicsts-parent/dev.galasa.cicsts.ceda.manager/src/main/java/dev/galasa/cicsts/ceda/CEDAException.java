<<<<<<< HEAD
=======
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
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
