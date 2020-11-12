<<<<<<< HEAD
=======
/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
>>>>>>> 0fa6c3b7872389baee9ca409ab206d077fbe612a
package dev.galasa.cicsts.ceda;

import dev.galasa.zos.ZosManagerException;

public class CEDAManagerException extends ZosManagerException{
   
   private static final long serialVersionUID = 1L;
   
   public CEDAManagerException() {}
   
   public CEDAManagerException(String message) {
        super(message);
   }
   
   public CEDAManagerException(Throwable cause) {
      super(cause);
   }
   
   public CEDAManagerException(String message, Throwable cause) {
      super(message, cause);
   }
   
   public CEDAManagerException(String message, Throwable cause, boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
   
   
   
}
