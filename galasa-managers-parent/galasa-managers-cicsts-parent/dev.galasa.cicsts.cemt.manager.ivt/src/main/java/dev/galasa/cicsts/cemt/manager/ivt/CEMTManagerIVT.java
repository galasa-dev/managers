package dev.galasa.cicsts.cemt.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
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
   
   @CEMT
   public ICEMT cemt;
   
   @BeforeClass
   public void login() throws TerminalInterruptedException, KeyboardLockedException, NetworkException, TimeoutException, FieldNotFoundException {
      
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
      
      
   }
   
   @Test
   public void testCEMTIsNotNull() {
      assertThat(cemt).isNotNull();
      assertThat(cics).isNotNull();
      assertThat(cemtTerminal).isNotNull();
   }
   
   @Test
   public void testInquireResource() throws CEMTException{
      assertThat(cemt.inquireResource(cemtTerminal, "PROGRAM", "CHARLIE", null));
   }
   
   @Test
   public void testDiscardResource() throws CEMTException {
      cemt.discardResource(cemtTerminal, "PROGRAM", "CHARLIE", "IDK");
   }
}
