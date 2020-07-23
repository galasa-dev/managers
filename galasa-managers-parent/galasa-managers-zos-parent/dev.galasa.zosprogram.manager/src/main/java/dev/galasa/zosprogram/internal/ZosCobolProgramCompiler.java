/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosprogram.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dev.galasa.zosprogram.ZosProgramException;
import dev.galasa.zosprogram.ZosProgramManagerException;
import dev.galasa.zosprogram.internal.properties.CICSDatasetHlq;
import dev.galasa.zosprogram.internal.properties.LanguageEnvironmentDatasetHlq;
import dev.galasa.zosprogram.internal.properties.ProgramLanguageDatasetHlq;

public class ZosCobolProgramCompiler extends AbstractZosProgramCompiler {

    public ZosCobolProgramCompiler(ZosProgramImpl zosProgram) throws ZosProgramException {
        super(zosProgram);
    }

    @Override
    public Map<String, Object> buildParameters() throws ZosProgramException {
        HashMap<String, Object> parameters = new HashMap<>();
        try {
            parameters.put("PROGRAM", zosProgram.getName());
            List<String> languagePrefix = ProgramLanguageDatasetHlq.get(zosProgram.getImage().getImageID(), zosProgram.getLanguage());
            List<String> lePrefix = LanguageEnvironmentDatasetHlq.get(zosProgram.getImage().getImageID(), zosProgram.getLanguage());
            //TODO: Get these values. Handle copybooks in bundle?  
            List<String> compileSyslibs = Collections.emptyList();
            List<String> linkSyslibs = Collections.emptyList();
            if (zosProgram.isCics()) {
                List<String> cicsPrefix = CICSDatasetHlq.get(zosProgram.getImage().getImageID());
                parameters.put("COBOL.STEPLIB", buildSteplib(languagePrefix, lePrefix, cicsPrefix));
                parameters.put("COBOL.SYSLIB", buildCompileSyslib(compileSyslibs, cicsPrefix));
                parameters.put("LKED.SYSLIB", buildLinkSyslib(linkSyslibs, lePrefix, cicsPrefix));
                parameters.put("LKED.SYSIN", "  NAME " + zosProgram.getName() + "(R)");
            } else {
                parameters.put("COBOL.STEPLIB", buildSteplib(languagePrefix, lePrefix, Collections.emptyList()));
                parameters.put("COBOL.SYSLIB", buildCompileSyslib(compileSyslibs, Collections.emptyList()));
                parameters.put("LKED.SYSLIB", buildLinkSyslib(linkSyslibs, lePrefix, Collections.emptyList()));
                parameters.put("LKED.SYSIN", "  NAME " + zosProgram.getName() + "(R)");
            }
            parameters.put("SYSLMOD", zosProgram.getLoadlib().getName());
            parameters.put("SOURCE", zosProgram.getProgramSource());
        } catch (ZosProgramManagerException e) {
            throw new ZosProgramException("Problem building compile JCL for " + zosProgram.getLanguage() + " program " + zosProgram.getName(), e);
        }
        return parameters;        
    }
    
    private String buildSteplib(List<String> languagePrefix, List<String> lePrefix, List<String> cicsPrefix) {
        List<String> datasetList = new LinkedList<>();
        Iterator<String> it = languagePrefix.iterator();
        while (it.hasNext()) {
            datasetList.add(it.next() + ".SIGYCOMP");
        }
        it = lePrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SCEERUN");
            datasetList.add(pfx + ".SCEERUN2");
        }
        it = cicsPrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SDFHLOAD");
        }        
        return formatDatasetConcatenation(datasetList);
    }

    private String buildCompileSyslib(List<String> compileSyslibs, List<String> cicsPrefix) {
        List<String> datasetList = new LinkedList<>();
        datasetList.addAll(compileSyslibs);
        Iterator<String> it = cicsPrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SDFHCOB");
            datasetList.add(pfx + ".SDFHMAC");
            datasetList.add(pfx + ".SDFHSAMP");
        }
        return formatDatasetConcatenation(datasetList);
    }

    private String buildLinkSyslib(List<String> linkSyslibs, List<String> lePrefix, List<String> cicsPrefix) {
        List<String> datasetList = new LinkedList<>();
        datasetList.addAll(linkSyslibs);
        Iterator<String> it = cicsPrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SDFHLOAD");
        }
        it = lePrefix.iterator();
        while (it.hasNext()) {
            String pfx = it.next();
            datasetList.add(pfx + ".SCEELKEX");
            datasetList.add(pfx + ".SCEELKED");
        }
        return formatDatasetConcatenation(datasetList);
    }
    
    @Override
    protected String getSkelName() {
        if (zosProgram.isCics()) {
            return "cobolCICS.skel";
        }
        return "cobolBatch.skel";
    }
}