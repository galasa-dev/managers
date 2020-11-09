package dev.galasa.cicsts.cemt.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;

import dev.galasa.Before;
import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.ceda.CEDA;
import dev.galasa.cicsts.ceda.CEDAException;
import dev.galasa.cicsts.ceda.ICEDA;
import dev.galasa.cicsts.cemt.CEMT;
import dev.galasa.cicsts.cemt.CEMTException;
import dev.galasa.cicsts.cemt.ICEMT;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class CEMTManagerIVT {

  
   @CicsRegion
   public ICicsRegion cics;
   
   @CicsTerminal
   public ICicsTerminal cemtTerminal;
   
   @CicsTerminal 
   public ICicsTerminal cedaTerminal;
   
   @CicsTerminal
   public ICicsTerminal beforeTerminal;
   
   @CEMT
   public ICEMT cemt;
   
   @CEDA
   public ICEDA ceda;
   
   @BeforeClass
   public void login() throws TerminalInterruptedException, KeyboardLockedException, NetworkException, TimeoutException, FieldNotFoundException {
      
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
      
      cedaTerminal.clear();
      cedaTerminal.waitForKeyboard();
      
      beforeTerminal.clear();
      beforeTerminal.waitForKeyboard();
      
   }
   
   @Before
   public void clearResources() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException, InterruptedException {
      
      beforeTerminal.type("CEMT DISCARD PROGRAM(EXAMPLE)").enter().waitForKeyboard();
      beforeTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      beforeTerminal.type("CEDA DELETE PROGRAM(EXAMPLE) GROUP(exGroup)").enter().waitForKeyboard();
      beforeTerminal.pf3().waitForKeyboard().clear().waitForKeyboard();
      
      
   }
   
   @Test
   public void testCEMTIsNotNull() {
      assertThat(cemt).isNotNull();
      assertThat(cics).isNotNull();
      assertThat(ceda).isNotNull();
      assertThat(cedaTerminal).isNotNull();
      assertThat(cemtTerminal).isNotNull();
      assertThat(beforeTerminal).isNotNull();
   }
   
   
   @Test
   public void testInquireResource() throws CEMTException, CEDAException, InterruptedException{
      
      if(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null) {
         ceda.createResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup", null);
         ceda.installResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
      }

      
      assertThat(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE").get("Program").equals("EXAMPLE"));
      
   }
   
   @Test
   public void testInquireResourceThatDoesntExist() throws CEMTException, InterruptedException, CEDAException {
      
      if(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null) {
         ceda.createResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup", null);
         ceda.installResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
      }
      
      assertThat(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") != null);
      
      ceda.deleteResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
      cemt.discardResource(cemtTerminal, "PROGRAM", "EXAMPLE", "RESPONSE: NORMAL");
      
      assertThat(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null);
   }
  
                          
   @Test
   public void testResourceIsRetrievingProperties() throws CEMTException, CEDAException{
            
      if(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null) {
         ceda.createResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup", null);
         ceda.installResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
      }
      
      HashMap<String, String> resource = cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE");
      
      assertThat(resource.get("Program") == "EXAMPLE");
      assertThat(resource.get("Length") == "0000000000");
      assertThat(resource.get("Language") == "Notdefined");
      assertThat(resource.get("Progtype") == "Program");
      assertThat(resource.get("Status") == "Enabled");
      assertThat(resource.get("Sharestatus") == "Private");

     
   }
   
   @Test
   public void testDiscardResource() throws CEMTException, CEDAException, InterruptedException {
      
      if(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null) {
         ceda.createResource(cedaTerminal, "PROGRAM","EXAMPLE", "exGroup", null);
         ceda.installResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
      }else {
         ceda.deleteResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
         ceda.createResource(cedaTerminal, "PROGRAM","EXAMPLE", "exGroup", null);
         ceda.installResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
      }
      
      cemt.discardResource(cemtTerminal, "PROGRAM", "EXAMPLE", "RESPONSE: NORMAL");

      assertThat(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null);
   }
   
   @Test
   public void testSetResource() throws CEMTException, CEDAException{
      
      if(cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE") == null) {
         ceda.createResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup", null);
         ceda.installResource(cedaTerminal, "PROGRAM", "EXAMPLE", "exGroup");
      }
      
      HashMap<String, String> resource = cemt.inquireResource(cemtTerminal, "PROGRAM", "EXAMPLE");
      
      if(!resource.get("Status").equals("Disabled")) {
         cemt.setResource(cemtTerminal, "PROGRAM", "EXAMPLE", "DISABLED");
         assertThat(resource.get("Status").equals("Disabled"));
      }else {
         cemt.setResource(cemtTerminal, "PROGRAM", "EXAMPLE", "ENABLED");
         assertThat(resource.get("Status").equals("Enabled"));
      }
   
      
   }
   
   
   @Test
   public void testDiscardResourceThatDoesntExist() throws CEMTException, InterruptedException {
      
      assertThatThrownBy(() -> {
         cemt.discardResource(cemtTerminal, "PROGRAM", "Jam", "RESPONSE: NORMAL");
      }).isInstanceOf(CEMTException.class).hasMessageContaining("Problem determining the result from the CEMT command");
 
   }
   
   @Test
   public void testPerformSystemProperty() throws CEMTException, InterruptedException {

      assertThat(cemt.performSystemProperty(cemtTerminal, "DUMP", "DUMPCODE(TEST) TITLE(TESTING)", "RESPONSE: NORMAL"));
      
   }
   
 
   


}
