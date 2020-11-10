package dev.galasa.cicsts.cemt.internal;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.cemt.CEMTException;
import dev.galasa.cicsts.cemt.ICEMT;
import dev.galasa.zos3270.ITerminal;

public class CEMTImpl implements ICEMT {

   private ITerminal terminal;
   
   protected HashMap<String, String> getAttributes(String string, String resourceName, HashMap<String, String> map) throws Exception {
	      Pattern pattern = Pattern.compile("\\w*\\(\\s*[a-zA-z0-9]*\\s*\\)");
	      Matcher matcher = pattern.matcher(string);
	      try {
	         while(matcher.find()) {
	            String matchedString = matcher.group();
	            if(!matchedString.contains("INQUIRE")) {
	               String newString = matchedString.substring(0, matchedString.length() -1);
	               String[] parts = newString.split("\\(");
	               if(parts.length < 2 && !map.containsKey(parts[0])) {
	                  map.put(parts[0], "");
	               }else if(map.containsKey(parts[0]) && parts.length == 2) {
	                  if(!map.get(parts[0]).equals(parts[1].trim())) {
	                     String value = map.get(parts[0]);
	                     map.put(parts[0], (value + parts[1]).trim());
	                  }
	               }else if(parts.length == 2){
	                  map.put(parts[0], parts[1].trim());
	               }
	            }
	         }
	      }catch(Exception e) {
	         throw new Exception("Error creating map", e);
	      }
	      return map;
	   }
   
   @Override
   public HashMap<String, String> inquireResource(@NotNull ITerminal cemtTerminal,
                                              @NotNull String resourceType,
                                              @NotNull String resourceName) throws CEMTException{
      
      this.terminal = cemtTerminal;
      HashMap<String, String> returnMap = new HashMap<String, String>();
      
      try {
         this.terminal.clear();
         this.terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CEMTException("Problem starting transaction", e);
      }
      
      try {
         terminal.type("CEMT INQUIRE " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();
         terminal.waitForTextInField("STATUS:");
      }catch(Exception e) {
         throw new CEMTException("Problem with starting CEMT transaction");
      }
      
      try {
//    	  Thread.sleep(1000);
         if(!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
            terminal.pf9();
            terminal.waitForKeyboard();
            terminal.pf3();
            terminal.clear();
            terminal.waitForKeyboard();
            return null;  
         }
      }catch(Exception e){
         throw new CEMTException("Problem determining the result of the CEMT command", e);
      }
      
      try {
         terminal.tab().enter().waitForKeyboard();
         if(!terminal.retrieveScreen().contains("Program(" + resourceName.toUpperCase() + ")")) {
            throw new CEMTException("Problem finding properties");
         }
      }catch(Exception e) {
         throw new CEMTException("Problem retrieving properties for resource", e);
      }
      
      try {
        
         String terminalString = terminal.retrieveScreen();
         
         returnMap = getAttributes(terminalString, resourceName, returnMap);
         
         boolean pageDown = terminalString.contains("+");
         
         while(pageDown) {
            
            terminal.pf8().waitForKeyboard();
            terminalString = terminal.retrieveScreen();
            returnMap = getAttributes(terminalString, resourceName, returnMap);
            
            if(terminalString.indexOf("+") == terminalString.lastIndexOf("+")) {
               pageDown = false;
            }
            
         }
        
      }catch(Exception e) {
         throw new CEMTException("Problem whilst adding resource properties", e);
      }
      
      
      try {
         terminal.pf3();
         terminal.clear();
         terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CEMTException("Unable to return terminal back into reset state", e);
      }
      
      return returnMap;
      
   }
   

   @Override
   public void setResource(@NotNull ITerminal cemtTerminal, @NotNull String resourceType, String resourceName,
         @NotNull String action, @NotNull String searchText) throws CEMTException {
      
      this.terminal = cemtTerminal;
      
      try {
         this.terminal.clear();
         this.terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CEMTException("Problem starting transaction", e);
      }
      
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
   public void discardResource(@NotNull ITerminal cemtTerminal, @NotNull String resourceType,
         @NotNull String resourceName, @NotNull String searchText) throws CEMTException {
      this.terminal = cemtTerminal;
         try {
            this.terminal.clear();
            this.terminal.waitForKeyboard();
         }catch(Exception e) {
            throw new CEMTException("Problem starting transaction", e);
         }
         try {
            if(resourceName == null) {
               terminal.type("CEMT DISCARD " + resourceType).enter().waitForKeyboard();
            }else {
               terminal.type("CEMT DISCARD " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();
            }
            terminal.waitForTextInField("STATUS:");
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
