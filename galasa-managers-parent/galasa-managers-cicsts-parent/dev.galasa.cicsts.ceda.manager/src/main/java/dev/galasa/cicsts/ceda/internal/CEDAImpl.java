package dev.galasa.cicsts.ceda.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.ceda.CEDAException;
import dev.galasa.cicsts.ceda.ICEDA;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

public class CEDAImpl implements ICEDA{
   
   private ITerminal terminal;

   @Override
   public void createResource(@NotNull ITerminal cedaTerminal, @NotNull String resourceType,
         @NotNull String resourceName, @NotNull String groupName, @NotNull String resourceParameters) throws CEDAException{
      
      this.terminal = cedaTerminal;
      
      try {
         this.terminal.waitForKeyboard();
         
         this.terminal.type("CEOT TRANIDONLY").enter().waitForKeyboard();
         this.terminal.type("CEDA DEFINE " + resourceType + "(" + resourceName +
               ") GROUP(" + groupName + ") " + resourceParameters).enter().waitForKeyboard();
         
      }catch(TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
         throw new CEDAException("Problem with starting the CEDA transaction", e);
      }
      
      try {
         if(this.terminal.retrieveScreen().contains("DEFINE SUCCESSFUL")){
            if(terminal.retrieveScreen().contains("MESSAGES:")) {
               terminal.pf9();
            }else {
               terminal.pf9();
            }
         }
      }catch (Exception e) {
         throw new CEDAException("Problem determining the result from the CEDA command", e);
      }
      
      try {
         this.terminal.pf3();
         this.terminal.clear();
         this.terminal.waitForKeyboard();
         this.terminal.type("CEDA").enter().waitForKeyboard();
      }catch(Exception e) {
         throw new CEDAException("Unable to return terminal back into reset state", e);
      }
      
   }
   
   @Override
   public void installGroup(@NotNull ITerminal cedaTerminal, @NotNull String groupName) throws CEDAException {
      this.terminal = cedaTerminal;
      try{
         this.terminal.waitForKeyboard();
      }catch(TimeoutException | KeyboardLockedException | TerminalInterruptedException e){
         throw new CEDAException(
            "Unable to prepare for the CEDA install group", e);
      }
      
      try {
         terminal.type("CEDA INSTALL GROUP(" + groupName + ")").enter().waitForKeyboard();
         
      }catch(Exception e) {
         throw new CEDAException("Problem with starting the CEDA transaction");
      }
      
      try {
         if(!this.terminal.retrieveScreen().contains("INSTALL SUCCESSFUL")) {
            this.terminal.pf9();
            this.terminal.waitForKeyboard();
            throw new CEDAException("Errors detected whilst installing group");
         }
      }catch(Exception e) {
         throw new CEDAException("Problem determining the result from the CEDA command", e);
      }
      
      try {
         this.terminal.pf3();
         this.terminal.clear();
         this.terminal.waitForKeyboard();
         this.terminal.type("CEDA").enter().waitForKeyboard();
      }catch(Exception e) {
         throw new CEDAException("Unable to return terminal back into reset state", e);
      }
   }
   
   @Override
   public void installResource(@NotNull ITerminal cedaTerminal, @NotNull String resourceType, @NotNull String resourceName, @NotNull String cedaGroup)
         throws CEDAException {
      
     this.terminal = cedaTerminal;
     
     try {
       
        this.terminal.type("CEDA INSTALL " + resourceType + "(" + resourceName + ") GROUP(" +
                             cedaGroup + ")");
        
     }catch(Exception e) {
        throw new CEDAException("Problem with starting the CEDA transaction", e);
     }
     
     try {
        boolean error = false;
        try {
           if (this.terminal.retrieveScreen().contains("USE P9 FOR S MSGS")) {
              error = true;
              
              //if the terminal contains the error then error = true elseif it contains
              //the success then error = false
           }else if(!this.terminal.retrieveScreen().contains("INSTALL SUCCESSFUL")) {
              error = true;
           }
           
           if(error) {
              this.terminal.pf9();
              this.terminal.waitForKeyboard();
              throw new CEDAException("Errors detected whilst installing group");
           }
        }catch(Exception e) {
           error = true;
        }
     }catch(Exception e) {
        throw new CEDAException("Problem determining the result from the CEDA command");
     }
     
     try {
        this.terminal.pf3();
        this.terminal.clear();
        this.terminal.waitForKeyboard();
        this.terminal.type("CEDA").enter().waitForKeyboard();
     }catch(Exception e) {
        throw new CEDAException("Unable to return terminal back into reset state", e);
     }
   
   }
   
   @Override
   public void deleteGroup(@NotNull ITerminal cedaTerminal, @NotNull String groupName) throws CEDAException {
      this.terminal = cedaTerminal;
      
      try {
         this.terminal.type("CEDA DELETE GROUP(" + groupName + ") ALL").enter().waitForKeyboard();
      }catch(Exception e) {
         throw new CEDAException("Problem with starting the CEDA transaction");
      }
      
      try {
         if(!this.terminal.retrieveScreen().contains("DELETE SUCCESSFUL")) {
           this.terminal.pf9();
           this.terminal.waitForKeyboard();
           
           throw new CEDAException("Errors detected whilst discarding group");
         }
      }catch(Exception e) {
        throw new CEDAException("Problem determining the result from the CEDA command", e);
      }
      
      try {
         this.terminal.pf3();
         this.terminal.clear();
         this.terminal.waitForKeyboard();
         this.terminal.type("CEDA").enter().waitForKeyboard();
      }catch(Exception e) {
         throw new CEDAException("Unable to return terminal back into reset state", e);
      }
   }
   
   @Override
   public void deleteResource(@NotNull ITerminal cedaTerminal, @NotNull String resourceType, @NotNull String resourceName, @NotNull String groupName)
         throws CEDAException {
      
      this.terminal = cedaTerminal;
      
      try {
         
         this.terminal.waitForKeyboard();
         this.terminal.type("CEDA DELETE " + resourceType + "(" +
                              resourceName + groupName + ")").enter();
         this.terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CEDAException("Problem with starting the CEDA transaction", e);
      }
      
      try {
         if(!this.terminal.retrieveScreen().contains("DELETE SUCCESSFUL")) {
            this.terminal.pf9().waitForKeyboard();
            throw new CEDAException("Errors detected whilst discarding group");
         }
      }catch(Exception e) {
         throw new CEDAException("Problem determinign the result from the CEDA command)", e);
         
      }
      try {
         this.terminal.pf3();
         this.terminal.clear();
         this.terminal.waitForKeyboard();
         this.terminal.type("CEDA").enter().waitForKeyboard();
      }catch(Exception e) {
         throw new CEDAException("Unable to return terminal back into reset state", e);
      }
      
   }
   
   
}
