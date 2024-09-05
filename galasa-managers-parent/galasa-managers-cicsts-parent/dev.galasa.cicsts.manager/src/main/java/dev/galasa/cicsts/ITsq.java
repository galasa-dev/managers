/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;
public interface ITsq {

      /**
       * Check the existence of a TSQ. 
       * @throws TsqException if there is a problem in checking the TSQ existence
       * @return boolean based on if TSQ is existing or not
       */
      public boolean exists() throws TsqException;  

      /**
       * Check if a TSQ is recoverable. 
       * @throws TsqException if there is a problem in checking if the TSQ is recoverable or not
       * @return boolean based on if TSQ is recoverable or not
       */      
      public boolean isRecoverable() throws TsqException;

      /**
       * Read Data from TSQ based on item number.
       * @param item Item number of the TSQ to be read
       * @return Data read from TSQ as String 
       * @throws TsqException if there is a problem in reading from the TSQ
       */    
      public String readQueue(int item) throws TsqException;

      /**
       * Read next from TSQ. 
       * @return Data read from TSQ as String 
       * @throws TsqException if there is a problem in reading next from the TSQ
       */    
      public String readQueueNext() throws TsqException;

      /**
       * Write data to TSQ.  
       * @param data The data to be written to the TSQ
       * @throws TsqException if there is a problem in writing to the TSQ
       */
      public void writeQueue(@NotNull String data) throws TsqException;   
     
      /**
       * Delete non-recoverable TSQ. 
       * @throws TsqException if there is a problem in deleting the TSQ
       */
      public void deleteQueue() throws TsqException;  

}
