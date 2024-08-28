/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;

public interface ITsqHandler {

      /**
       * Set TSQ name
       * @param queueName TSQ name
       * @throws TsqException if there is a problem in setting the TSQ name
       */
      public void setQName(@NotNull String queueName) throws TsqException;
      
      /**
       * Get TSQ name
       * @return TSQ name 
       * @throws TsqException if there is a problem in setting the TSQ name
       */
      public String getQName() throws TsqException;        

      /**
       * Read Data from TSQ. TSQ name is set using setName() method.
       * TSQ item number to be read is passed as parm to this method.
       * @param item Item number of the TSQ to be read
       * @return Data read from TSQ as String 
       * @throws TsqException if there is a problem in reading from the TSQ
       */    
      public String readQ(@NotNull int item) throws TsqException;

      /**
       * Write inputData to TSQ. TSQ name is set using setName() method.
       * @param inputData The string to be written to the TSQ
       * @throws TsqException if there is a problem in writing to the TSQ
       */
      public void writeQ(@NotNull String inputData) throws TsqException;   
     
      /**
       * Delete TSQ. TSQ name is set using setName() method.
       * @throws TsqException if there is a problem in deleting the TSQ
       */
      public void deleteQ() throws TsqException;  
 
      /**
       * Make TSQ Recoverable. TSQ name is set using setName() method.
       * @throws TsqException if there is a problem in making the TSQ recoverable
       */    
      public void makeRecoverable() throws TsqException;  

}
