/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;
public interface ITsqFactory {

      /**
       * Create a new ITsq object with recoverable status   
       * @param queueName TSQ name
       * @param isRecoverable true for recoverable and false for non-recoverable
       * @return ITsq object. The existence of the ITsq object does not have any correlation to whether 
       *         the queue actually exists underneath it. It is used to access or create the ITsq.
       * @throws TsqException if there is a problem in creating the ITsq object
       */
      public ITsq createQueue(@NotNull String queueName, boolean isRecoverable) throws TsqException;
    
      /**
       * Create a new ITsq object without recoverable status. Default recoverable status is NonRecoverable   
       * @param queueName TSQ name
       * @return ITsq object. The existence of the ITsq object does not have any correlation to whether 
       *         the queue actually exists underneath it. It is used to access or create the ITsq.
       * @throws TsqException if there is a problem in creating the ITsq object
       */      
      public ITsq createQueue(@NotNull String queueName) throws TsqException;   

}
