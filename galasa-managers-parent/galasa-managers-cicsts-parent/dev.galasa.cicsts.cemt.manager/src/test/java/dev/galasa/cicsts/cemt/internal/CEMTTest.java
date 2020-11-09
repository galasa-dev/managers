package dev.galasa.cicsts.cemt.internal;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import dev.galasa.cicsts.cemt.internal.CEMTImpl;

public class CEMTTest {

   @Test
   public void testMultipleSameParameter() throws Exception {
      
      CEMTImpl cemtImpl = new CEMTImpl();
      
      HashMap<String, String> map = new HashMap<String, String>();
      
      String inputString = " Test() " + "\n" + " Test(1) " + "\n" + " Test(2) " + "\n" + " Test() " + "\n" + " Test(3) " + "\n" + " Test() ";
      
      map = cemtImpl.getAttributes(inputString, "CHAMP", map);
      
      assertTrue(map.get("Test").equals("123"));
   }
   
   @Test
   public void testMulitpleDifferentParameter() throws Exception{
     
      CEMTImpl cemtImpl = new CEMTImpl();
      
      HashMap<String, String> map = new HashMap<String, String>();
      
      String inputString = " Test() " + "\n" + " Test(1) " + "\n" + " Testing(2) " + "\n" + " Test() " + "\n" + " Tester(3) " + "\n" + " Test() ";
      
      map = cemtImpl.getAttributes(inputString, "CHAMP", map);
      
      assertTrue(map.get("Test").equals("1"));
      assertTrue(map.get("Testing").equals("2"));
      assertTrue(map.get("Tester").equals("3"));
      
   }
   
}
