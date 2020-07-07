package dev.galasa.core.manager.internal;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class CoreGherkinExecution {

    private final static Pattern patternVariable = Pattern.compile(".*(<\\w+>).*");

    //GIVEN

    public static void storeVariable(Matcher matcherStoreVariable, IConfigurationPropertyStoreService cpsTest, Map<String, Object> testVariables)
            throws ManagerException {
        String variableName = matcherStoreVariable.group(1);
        String cpsProp = matcherStoreVariable.group(2);
        String cpsPrefix = cpsProp.substring(0, cpsProp.indexOf("."));
        String cpsSuffix = cpsProp.substring(cpsProp.indexOf(".") + 1);
        try {
            String cpsValue = cpsTest.getProperty(cpsPrefix, cpsSuffix);
            testVariables.put(variableName, cpsValue);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ManagerException("Unable to access CPS", e);
        }
    }

    //THEN

    public static void log(Matcher matcherLog, Map<String, Object> testVariables, Log logger) {
        String output = matcherLog.group(1);
        Matcher matcherVariable = patternVariable.matcher(output);
        while(matcherVariable.matches()) {
            String variableName = matcherVariable.group(1).replaceAll("<|>", "");
            String variableValue = (String) testVariables.get(variableName);
            output = output.replace(matcherVariable.group(1), variableValue);
            matcherVariable = patternVariable.matcher(output);
        }

        logger.info(output);
    }
    
}