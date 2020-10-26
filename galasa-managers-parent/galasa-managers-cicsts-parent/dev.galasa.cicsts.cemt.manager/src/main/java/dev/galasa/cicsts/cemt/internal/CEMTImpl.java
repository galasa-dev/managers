package dev.galasa.cicsts.cemt.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.cemt.CEMTException;
import dev.galasa.cicsts.cemt.ICEMT;
import dev.galasa.zos3270.ITerminal;

public class CEMTImpl implements ICEMT {

   private ITerminal terminal;
   
   @Override
   public boolean inquireResource(@NotNull ITerminal cemtTerminal, @NotNull String resourceType,
         @NotNull String resourceName, @NotNull String searchText) throws CEMTException {
      
      return inquireResource(cemtTerminal, resourceType, resourceName, searchText, 0);
   }

   @Override
   public boolean inquireResource(@NotNull ITerminal cemtTerminal, @NotNull String resourceType,
         @NotNull String resourceName, @NotNull String searchText, @NotNull long milliSecondTimeout) throws CEMTException {
      
     this.terminal = cemtTerminal;
      
     if(milliSecondTimeout > 0) {
        try {
           Thread.sleep(milliSecondTimeout);
        }catch(InterruptedException e) {
           throw new CEMTException("Unable to prepare for the CEMT inquire resource",e);
        }
     }
     
     try {
        terminal.type("CEMT INQUIRE " + resourceType + "(" + resourceName + ")").enter();
        terminal.waitForKeyboard();
     }catch(Exception e) {
        throw new CEMTException("Problem with starting the CEMT transaction",e);
     }
      
     try {
        if(!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
           terminal.pf9();
           terminal.waitForKeyboard();
           throw new CEMTException("Errors detected whilst inquiring resource");
        }
     }catch(Exception e) {
        throw new CEMTException("Problem determining the result from the CEMT command", e);
     }
     
     boolean found = false;
     
     try {
     
        if(searchText == null) {
           found = true;
        }
        
        terminal.tab();
        terminal.waitForKeyboard();
        terminal.enter();
        
        found = terminal.retrieveScreen().contains(searchText);
        
        boolean pageDown = terminal.retrieveScreen().contains("+ ");
        
        while(!found && pageDown) {
           terminal.pf8();
           found = terminal.retrieveScreen().contains(searchText);
           pageDown = terminal.retrieveScreen().contains("+ ");
        }
        
        if(!found) {
           throw new CEMTException("Unable to locate search string: " + searchText);
        }
        
     }catch(Exception e) {
        throw new CEMTException("An error occured whilst trying to seach for string", e);
     }
     
     try {
        this.terminal.pf3();
        this.terminal.clear();
        this.terminal.waitForKeyboard();
        this.terminal.type("CEDA").enter().waitForKeyboard();
     }catch(Exception e) {
        throw new CEMTException("Unable to return terminal back into reset state", e);
     }
     
     return found;
   }

   @Override
   public void setResource(@NotNull ITerminal cemtTerminal, @NotNull String resourceType, String resourceName,
         @NotNull String action, @NotNull String searchText) throws CEMTException {
      
      this.terminal = cemtTerminal;
      
      try {
         if(resourceName == null) {
            terminal.type("CEMT SET " + resourceType + " " + action);
         }else {
            terminal.type("CEMT SET " + resourceType + "(" + resourceName + ") " + action);
            terminal.enter();
            terminal.waitForKeyboard();
         }
      }catch(Exception e) {
         throw new CEMTException("Problem with starting the CEMT transaction", e);
      }
      
      try {
         if(!terminal.retrieveScreen().contains(searchText)) {
            terminal.pf9().waitForKeyboard();
            throw new CEMTException("Errors detected whilst setting resource");
         }
      }catch(Exception e) {
         throw new CEMTException("Problem determining the result from the CEMT command", e);
      }
      
      try {
         terminal.pf3();
         terminal.clear();
         terminal.waitForKeyboard();
         terminal.type("CEDA").enter().waitForKeyboard();
      }catch(Exception e) {
         throw new CEMTException("Unable to return terminal back into reset state", e);
      }
      
      
   }

   @Override
   public void setResource(@NotNull ITerminal cemtTerminal, @NotNull String resourceType, @NotNull String resourceName,
         @NotNull String action) throws CEMTException {
      this.terminal = cemtTerminal;
      
      setResource(cemtTerminal, resourceType, resourceName, action, "RESPONSE: NORMAL");
      
   }

   @Override
   public void discardResouce(@NotNull ITerminal cemtTerminal, @NotNull String resourceType,
         @NotNull String resourceName, @NotNull String searchText) throws CEMTException {
   
         try {
            if(resourceName == null) {
               terminal.type("CEMT DISCARD " + resourceType);
            }else {
               terminal.type("CEMT DISCARD " + resourceType + "(" + resourceName + ")");
            }
            
            terminal.enter();
            terminal.waitForKeyboard();
         }catch(Exception e) {
            throw new CEMTException("Problem with starting the CEMT transaction", e);
         }
         
         try {
            if(!terminal.retrieveScreen().contains(searchText)) {
               terminal.pf9();
               terminal.waitForKeyboard();
               throw new CEMTException("Errors detected whilst setting resource");
            }
         }catch(Exception e) {
            throw new CEMTException("Problem determining the result from the CEMT command");
         }
         
         try {
            terminal.pf3();
            terminal.clear();
            terminal.waitForKeyboard();
            terminal.type("CEDA").enter().waitForKeyboard();
         }catch(Exception e) {
            throw new CEMTException("Unable to return terminal back into reset state", e);
         }
      
   }

   @Override
   public boolean performSystemProperty(@NotNull ITerminal cemtTerminal, @NotNull String systemArea,
         @NotNull String setRequest, @NotNull String expectedResponse) throws CEMTException {
      
      this.terminal = cemtTerminal;
      
      String cemtCmd = "CEMT PERFORM " + systemArea + " ";
      cemtCmd += setRequest;
      
      try {
         terminal.type(cemtCmd);
         boolean success = terminal.retrieveScreen().contains(expectedResponse);
         if(!success) {
            throw new CEMTException("Expected Response from CEMT PERFORM not found. Expected: "
         + expectedResponse);
         }
         terminal.reportScreen();
         return success;
      }catch(Exception e) {
         throw new CEMTException(e);
      }
     
   }

   
   
}
