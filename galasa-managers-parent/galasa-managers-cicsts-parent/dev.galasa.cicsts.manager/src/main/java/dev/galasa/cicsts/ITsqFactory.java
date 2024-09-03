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
       * @param recoverable true for recoverable and false for non-recoverable
       * @return ITsq object
       * @throws TsqException if there is a problem in creating the ITsq object
       */
      public ITsq createQueue(@NotNull String queueName, @NotNull boolean recoverable) throws TsqException;
    
      /**
       * Create a new ITsq object without recoverable status. Default recoverable status is NonRecoverable   
       * @param queueName TSQ name
       * @return ITsq object
       * @throws TsqException if there is a problem in creating the ITsq object
       */      
      public ITsq createQueue(@NotNull String queueName) throws TsqException;   

}
