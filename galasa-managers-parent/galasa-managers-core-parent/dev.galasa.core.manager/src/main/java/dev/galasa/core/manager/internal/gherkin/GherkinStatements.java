package dev.galasa.core.manager.internal.gherkin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.galasa.ManagerException;
import dev.galasa.core.manager.internal.CoreManager;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IGherkinManager;
import dev.galasa.framework.spi.language.GalasaTest;

public class GherkinStatements {

    public static void register(GalasaTest galasaTest, CoreManager manager) throws ManagerException {
		for(IGherkinExecutable gherkinExecutable : galasaTest.getGherkinTest().getAllExecutables()) {
            switch (gherkinExecutable.getKeyword()) {
                case GIVEN:
                    match(GherkinStoreVariable.pattern, gherkinExecutable, manager);
                    break;

                case THEN:
                    match(GherkinLog.pattern, gherkinExecutable, manager);
                    break;
            
                default:
                    break;
            }
		}
    }
    
    private static void match(Pattern regexPattern, IGherkinExecutable gherkinExecutable, CoreManager manager) throws ManagerException {
        Matcher gherkinMatcher = regexPattern.matcher(gherkinExecutable.getValue());
        if(gherkinMatcher.matches()) {
            try {
                gherkinExecutable.registerManager((IGherkinManager) manager);
            } catch (TestRunException e) {
                throw new ManagerException("Unable to register Manager for Gherkin Statement", e);
            }
        }
    }
    
}