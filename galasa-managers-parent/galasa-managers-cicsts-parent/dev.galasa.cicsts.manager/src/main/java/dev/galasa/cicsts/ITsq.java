/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;
public interface ITsq {

      /**
       * Create a new TSQ and write data to the TSQ.   
       * @param queueName TSQ name
       * @param data data to be written to the queue
       * @param recoverable true for recoverable and false for non-recoverable
       * @return "OK" - for successful TSQ creation and data written to TSQ, 
       *         "ALREADY_EXISTING" - if TSQ already exiting and data written to existing TSQ
       * @throws TsqException if there is a problem in creating the TSQ name
       */
      public String createQueue(@NotNull String queueName, @NotNull String data, @NotNull boolean recoverable) throws TsqException;
    
      /**
       * Create a new TSQ and write data to the TSQ.   
       * @param queueName TSQ name
       * @param data data to be written to the queue
       * @return "OK" - for successful TSQ creation and data written to TSQ, 
       *         "ALREADY_EXISTING" - if TSQ already exiting and data written to existing TSQ
       * @throws TsqException if there is a problem in creating the TSQ name
       */      
      public String createQueue(@NotNull String queueName, @NotNull String data) throws TsqException;   

      /**
       * Read Data from TSQ based on item number.
       * @param queueName TSQ name
       * @param item Item number of the TSQ to be read
       * @return Data read from TSQ as String 
       * @throws TsqException if there is a problem in reading from the TSQ
       */    
      public String readQueue(@NotNull String queueName, @NotNull int item) throws TsqException;

      /**
       * Read next from TSQ. 
       * @param queueName TSQ name
       * @return Data read from TSQ as String 
       * @throws TsqException if there is a problem in reading from the TSQ
       */    
      public String readQueueNext(String queueName) throws TsqException;

      /**
       * Write data to TSQ.  
       * @param queueName TSQ name
       * @param data The data to be written to the TSQ
       * @throws TsqException if there is a problem in writing to the TSQ
       */
      public void writeQueue(@NotNull String queueName, @NotNull String data) throws TsqException;   
     
      /**
       * Delete TSQ. 
       * @param queueName TSQ name
       * @throws TsqException if there is a problem in deleting the TSQ
       */
      public void deleteQueue(@NotNull String queueName) throws TsqException;  

}
