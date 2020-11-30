/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

package dev.galasa.cicsts.cemt.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.CemtException;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.ICemt;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;

public class CemtImpl implements ICemt {

   private ICicsTerminal terminal;
   private ICicsRegion cicsRegion;
   
   public CemtImpl(ICicsRegion cicsRegion) {
      this.cicsRegion = cicsRegion;
   }
   
   protected CicstsHashMap getAttributes(String string, String resourceName, CicstsHashMap map) throws Exception {
     
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
   public CicstsHashMap inquireResource(@NotNull ICicsTerminal cemtTerminal,
                                              @NotNull String resourceType,
                                              @NotNull String resourceName) throws CemtException{
      
      if(cicsRegion != this.terminal.getCicsRegion()) {
         throw new CemtException("CICS Version Mismatch");
      }
      
      this.terminal = cemtTerminal;
      CicstsHashMap returnMap = new CicstsHashMap();
      
      try {
         this.terminal.clear();
         this.terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CemtException("Problem starting transaction", e);
      }
      
      try {
         terminal.type("CEMT INQUIRE " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();
         terminal.waitForTextInField("STATUS: ");
      }catch(Exception e) {
         throw new CemtException("Problem with starting CEMT transaction");
      }
      
      try {
         if(!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
            terminal.pf9();
            terminal.waitForKeyboard();
            terminal.pf3();
            terminal.waitForKeyboard();
            terminal.clear();
            terminal.waitForKeyboard();
            return null;  
         }
      }catch(Exception e){
         throw new CemtException("Problem determining the result of the CEMT command", e);
      }
      
      try {
         terminal.tab().waitForKeyboard().enter().waitForKeyboard();
         
         if(!terminal.retrieveScreen().contains("+")) {
            throw new CemtException("Problem finding properties");
         }
      }catch(Exception e) {
         throw new CemtException("Problem retrieving properties for resource", e);
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
         throw new CemtException("Problem whilst adding resource properties", e);
      }
      
      
      try {
         terminal.pf3();
         terminal.waitForKeyboard();
         terminal.clear();
         terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CemtException("Unable to return terminal back into reset state", e);
      }
      
      return null;
      
   }
   

   @Override
   public CicstsHashMap setResource(@NotNull ICicsTerminal cemtTerminal, @NotNull String resourceType, String resourceName,
         @NotNull String action) throws CemtException {
      
      if(cicsRegion != this.terminal.getCicsRegion()) {
         throw new CemtException("CICS Version Mismatch");
      }
      
      this.terminal = cemtTerminal;
      CicstsHashMap returnMap = new CicstsHashMap();
      
      try {
         this.terminal.clear();
         this.terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CemtException("Problem starting transaction", e);
      }
      
      try {
         if(resourceName == null) {
            terminal.type("CEMT SET ALL " + resourceType + " " + action);
            terminal.enter();
            terminal.waitForKeyboard();
         }else {
            terminal.type("CEMT SET " + resourceType + "(" + resourceName + ") " + action);
            terminal.enter();
            terminal.waitForKeyboard();
         }
         
         terminal.waitForTextInField("STATUS: ");
      }catch(Exception e) {
         throw new CemtException("Problem with starting the CEMT transaction", e);
      }
      
      try {
         if(!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
            terminal.pf9().waitForKeyboard();
            throw new CemtException("Errors detected whilst setting resource");
         }
      }catch(Exception e) {
         throw new CemtException("Problem determining the result from the CEMT command", e);
      }
      
      try {
         terminal.tab().waitForKeyboard().enter().waitForKeyboard();
         
         if(!terminal.retrieveScreen().contains("+")) {
            throw new CemtException("Problem finding properties");
         }
      }catch(Exception e) {
         throw new CemtException("Problem retrieving properties for resource", e);
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
         throw new CemtException("Problem whilst adding resource properties", e);
      }
      
      try {
         terminal.pf3();
         terminal.waitForKeyboard();
         terminal.clear();
         terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CemtException("Unable to return terminal back into reset state", e);
      }
      
      return null;
      
   }


   @Override
   public void discardResource(@NotNull ICicsTerminal cemtTerminal, @NotNull String resourceType,
         @NotNull String resourceName) throws CemtException {
      
      if(cicsRegion != this.terminal.getCicsRegion()) {
         throw new CemtException("CICS Version Mismatch");
      }
      
      this.terminal = cemtTerminal;
      
         try {
            this.terminal.clear();
            this.terminal.waitForKeyboard();
         }catch(Exception e) {
            throw new CemtException("Problem starting transaction", e);
         }
         
         try {
            if(resourceName == null) {
               terminal.type("CEMT DISCARD " + resourceType).enter().waitForKeyboard();
            }else {
               terminal.type("CEMT DISCARD " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();
            }
            
            terminal.waitForTextInField("STATUS: ");
         }catch(Exception e) {
            throw new CemtException("Problem with starting the CEMT transaction", e);
         }
         
         try {
            
            if(!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
               terminal.pf9();
               terminal.pf3();
               terminal.waitForKeyboard();
               terminal.clear();
               terminal.waitForKeyboard();
               throw new CemtException("Errors detected whilst setting resource");
            }
         }catch(Exception e) {
            throw new CemtException("Problem determining the result from the CEMT command");
         }
         
         try {
            terminal.pf3();
            terminal.waitForKeyboard();
            terminal.clear();
            terminal.waitForKeyboard();
         }catch(Exception e) {
            throw new CemtException("Unable to return terminal back into reset state", e);
         }
         
      
   }

   @Override
   public boolean performSystemProperty(@NotNull ICicsTerminal cemtTerminal, @NotNull String systemArea,
         @NotNull String setRequest, @NotNull String expectedResponse) throws CemtException {
      
      if(cicsRegion != this.terminal.getCicsRegion()) {
         throw new CemtException("CICS Version Mismatch");
      }
      
      this.terminal = cemtTerminal;
      
      try {
         this.terminal.clear();
         this.terminal.waitForKeyboard();
      }catch(Exception e) {
         throw new CemtException("Problem starting transaction", e);
      }
      
      String cemtCmd = "CEMT PERFORM " + systemArea + " ";
      cemtCmd += setRequest;
      
      try {
         terminal.type(cemtCmd).enter().waitForKeyboard();
         boolean success = terminal.retrieveScreen().contains(expectedResponse);
         if(!success) {
            throw new CemtException("Expected Response from CEMT PERFORM not found. Expected: "
         + expectedResponse);
         }else { 

            try {
               terminal.pf3();
               terminal.waitForKeyboard();
               terminal.clear();
               terminal.waitForKeyboard();
            }catch(Exception e) {
               throw new CemtException("Unable to return terminal back into reset state", e);
            }
            
            return success;
            
         }
         
      }catch(Exception e) {
         throw new CemtException(e);
      }
      
     
   }





   
   
}
