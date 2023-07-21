/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.sem.internal;

import java.util.HashMap;

import sem.Environment;
import sem.SIT;
import sem.SITGroup;
import sem.SemFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.sem.SemManagerException;
import dev.galasa.sem.Sit;
import dev.galasa.sem.Sits;

public class SitGenerator {

    private static final Log logger = LogFactory.getLog(CsdInputGenerator.class);
    private final SemManagerImpl semManager;
    
    private HashMap<String, SITGroup> sitGroups = new HashMap<String, SITGroup>();


    public SitGenerator(SemManagerImpl semManager) {
        this.semManager = semManager;
    }

    public void generate(Environment environment, Class<?> testClass) throws SemManagerException {
        Sits sits = testClass.getAnnotation(Sits.class);
        if (sits != null) {
            processSits(environment, sits, testClass);
        }

        Sit sit = testClass.getAnnotation(Sit.class);
        if (sit != null) {
            processSit(environment, sit, testClass);
        }
    }

    private void processSits(Environment environment, Sits sits, Class<?> testClass) throws SemManagerException {
        for(Sit sit : sits.value()) {
            processSit(environment, sit, testClass);
        }
    }

    private void processSit(Environment environment, Sit sit, Class<?> testClass) throws SemManagerException {

        String tag       = sit.cicsTag().trim().toUpperCase();
        String parameter = sit.parameter().trim().toUpperCase();
        String value     = sit.value().trim();
        
        String className = testClass.getName();

        if (parameter.equals("GRPLIST")) {
            throw new SemManagerException("GRPLIST is not allowed to be specified on @Sit annotations");
        }
        if (parameter.equals("APPLID")) {
            throw new SemManagerException("APPLID is not allowed to be specified on @Sit annotations");
        }
        
        // if there is no sitGroup for the current tag we want to create one
        if(!sitGroups.containsKey(tag)) {
            SITGroup sitGroup = SemFactory.eINSTANCE.createSITGroup();

            sitGroup.setName("SITs " + className + "(" + tag + ")");

            // if the sit has the default tag "UNTAGGED" it will apply to all cics'
            // otherwise we want to create a condition to apply it to the cics with the
            // corresponding tag
            if (!tag.equals("UNTAGGED")) {
                sitGroup.setCondition("&TAG(" + tag + ") = 'YES'");
            }

            sitGroups.put(tag, sitGroup); 
            
            sitGroup.setPARENT(environment);
        }

        // create the sit itself and add it to the correct group
        SIT generatedSit = SemFactory.eINSTANCE.createSIT();
        generatedSit.setParm(parameter);
        generatedSit.setValue("#" + value);
        generatedSit.setPARENT(sitGroups.get(tag));
    }

}
