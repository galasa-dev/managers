/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */

 package dev.galasa.jmeter.internal;

 import java.io.File;
 import java.util.Map;

 import dev.galasa.jmeter.IJMeterSession;

 public class JMeterSessionImpl implements IJMeterSession {

     
    
    @Override
     public void applyProperties(Map<String, String> properties) {
         // TODO Auto-generated method stub

     }

     @Override
     public void startJmeter() {
         // TODO Auto-generated method stub

     }

     @Override
     public void waitForJMeter() {
         // TODO Auto-generated method stub

     }

     @Override
     public void waitForJMeter(long timeout) {
         // TODO Auto-generated method stub

     }

     @Override
     public void setJmxFile(String path) {
         // TODO Auto-generated method stub

     }

     @Override
     public String getJmxFile() {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public String getLogFile(File logFile) {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public String getConsoleOutput() {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public String getListenerFile(String fileName) {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public void stopTest(long timeout) {
         // TODO Auto-generated method stub

     }

     @Override
     public long getExitCode() {
         // TODO Auto-generated method stub
         return 0;
     }

}