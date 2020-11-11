package dev.galasa.cicsts.cemt;

import dev.galasa.zos.ZosManagerException;

public class CEMTManagerException extends ZosManagerException {

   private static final long serialVersionUID = 1L;
   
   public CEMTManagerException() {}
   
   public CEMTManagerException(String message) {
        super(message);
   }
   
   public CEMTManagerException(Throwable cause) {
      super(cause);
   }
   
   public CEMTManagerException(String message, Throwable cause) {
      super(message, cause);
   }
   
   public CEMTManagerException(String message, Throwable cause, boolean enableSuppression,
         boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
   
   
}
