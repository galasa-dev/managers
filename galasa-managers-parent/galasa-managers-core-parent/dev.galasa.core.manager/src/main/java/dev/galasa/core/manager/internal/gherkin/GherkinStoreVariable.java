package dev.galasa.core.manager.internal.gherkin;

import java.util.Map;
import java.util.regex.Matcher;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class GherkinStoreVariable {

    public static void execute(Matcher matcherStoreVariable, IConfigurationPropertyStoreService cpsTest, Map<String, Object> testVariables)
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
}