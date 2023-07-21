/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.sem.CSDInput;
import dev.galasa.sem.CSDInputs;
import dev.galasa.sem.SemManagerException;
import sem.Environment;
import sem.SemFactory;

public class CsdInputGenerator {
    
    private static final Log logger = LogFactory.getLog(CsdInputGenerator.class);
    private final SemManagerImpl semManager;

    public CsdInputGenerator(SemManagerImpl semManager) {
        this.semManager = semManager;
    }

    public void generate(Environment environment, Class<?> testClass) throws SemManagerException {
        CSDInputs csdInputs = testClass.getAnnotation(CSDInputs.class);
        if (csdInputs != null) {
            processCsdInputs(environment, csdInputs, testClass);
        }
        
        CSDInput csdInput = testClass.getAnnotation(CSDInput.class);
        if (csdInput != null) {
            processCsdInput(environment, csdInput, testClass);
        }
    }

    private void processCsdInputs(Environment environment, CSDInputs csdInputs, Class<?> testClass) throws SemManagerException {
        if (csdInputs.value() == null) {
            return;
        }
        
        for(CSDInput csdInput : csdInputs.value()) {
            processCsdInput(environment, csdInput, testClass);
        }
        
    }

    private void processCsdInput(Environment environment, CSDInput csdInput, Class<?> testClass) throws SemManagerException {
        String inputName = csdInput.file().trim();
        String groupName = csdInput.group().trim();
        String[] tags    = csdInput.cicsTag().split(",");
        boolean startup = csdInput.startup();
        String minVersion = csdInput.minimumRelease();
        String maxVersion = csdInput.maximumRelease();
        
        IArtifactManager artifactManager = this.semManager.getArtifactManager();
        IBundleResources bundleResources = artifactManager.getBundleResources(testClass);

        for (String tag : tags) {

            tag = tag.trim().toUpperCase();
            
            // Map<String, String> complexProperties = teamManager.getCicsComplexProperties();
            // Not sure why we need this,  but leaving the code commented as a reminder
            
            VelocityContext substitutions = new VelocityContext();
            //for(Entry<String, String> entry : complexProperties.entrySet()) {
            //    substitutions.put(entry.getKey(), entry.getValue());
            // }

            StringWriter writer = new StringWriter();
            try {
                InputStream stream = bundleResources.retrieveFile(inputName + ".csdinput");
                InputStreamReader reader = new InputStreamReader(stream);
                Velocity.evaluate(substitutions, writer, "CSDInputVelocity", reader);
            } catch(Exception e) {
                throw new SemManagerException("Unable to read CSDInput artifact '" + inputName + "'", e);
            }


            inputName = inputName.replaceAll(".*/", "");
            
            
            
            //*** Check that no lines exceed 71 chars
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new StringReader(writer.toString()));
                String line = null;
                int lineno = 0;
                while((line = br.readLine()) != null) {
                    lineno++;
                    if (line.isEmpty()) {
                        sb.append("\n");
                        continue;
                    }
                    
                    if (line.charAt(0) == '*') {
                        sb.append(line);
                        sb.append("\n");
                        continue;
                    }
                    
                    if (line.length() < 72) {
                        sb.append(line);
                        sb.append("\n");
                        continue;
                    }
                    
                    if (line.contains("&")) { // Let SEM deal with it
                        sb.append(line);
                        sb.append("\n");
                        continue;
                    }
                    
                    if (line.charAt(71) == '*' || line.charAt(71) == 'X') {
                        sb.append(line);
                        sb.append("\n");
                        continue;
                    }
                    
                    String remainder = line.substring(71).trim();
                    if (remainder.isEmpty()) {
                        sb.append(line);
                        sb.append("\n");
                        continue;
                    }
                    
                    if (!csdInput.continueLines()) {
                        throw new SemManagerException("Line " + lineno + " in CSDInput artifact '" + inputName + "' exceeds 72 bytes");
                    }
                    
                    String firstline = line.substring(0, 71);
                    String secondline = line.substring(71);
                    
                    sb.append(firstline);
                    sb.append("*\n");
                    sb.append(secondline);
                    sb.append("\n");
                    
                    logger.warn("Line " + lineno + " in CSDInput artifact '" + inputName + "' has been continued on a new line");
                }
                
                br.close();
            } catch(SemManagerException e) {
                throw e;
            } catch(Exception e) {
                throw new SemManagerException("Problem checking for continuations in  CSDInput artifact '" + inputName + "'", e);
            }
            
            


            sem.CSDInput semCSDInput = SemFactory.eINSTANCE.createCSDInput();
            semCSDInput.setName("Artifact " + inputName);
            
            
            StringBuilder condition = new StringBuilder();
            
            
            if (tag.length() > 0) {
                if (condition.length() > 0) {
                    condition.append(" AND ");
                }
                condition.append("&TAG(" + tag.toUpperCase() + ") = 'YES'");
            }
            
            
            if (!minVersion.isEmpty()) {
                if (condition.length() > 0) {
                    condition.append(" AND ");
                }
                condition.append("&CICSVERSION >= '" + minVersion + "'");
            }
            
            if (!maxVersion.isEmpty()) {
                if (condition.length() > 0) {
                    condition.append(" AND ");
                }
                condition.append("&CICSVERSION <= '" + maxVersion + "'");
            }
            
            

            if (condition.length() > 0) {
                semCSDInput.setCondition(condition.toString());
            }

            
            if (startup) {
                if (groupName.length() > 0) {
                    semCSDInput.setGroup(groupName.toUpperCase());
                } else {
                    String filename = inputName.replaceAll("(\\\\|\\/)", "\\" + System.getProperty("file.separator"));
                    File file = new File(filename);
                    String name = file.getName().toUpperCase();
                    semCSDInput.setGroup(name);
                }
            }

            semCSDInput.setInput(sb.toString());

            semCSDInput.setApplyonce(true);
            semCSDInput.setAllowAddGroup(true);

            environment.getCSDINPUTS().add(semCSDInput);
        }

    }

}
