package dev.galasa.cicsts.cemt.manager.ivt;

public class IVTException extends Exception {
   
   private static final long serialVersionUID = 1L;

   public IVTException() {
   }

   public IVTException(String message) {
       super(message);
   }

   public IVTException(Throwable cause) {
       super(cause);
   }

   public IVTException(String message, Throwable cause) {
       super(message, cause);
   }

   public IVTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
       super(message, cause, enableSuppression, writableStackTrace);
   }
   
}
