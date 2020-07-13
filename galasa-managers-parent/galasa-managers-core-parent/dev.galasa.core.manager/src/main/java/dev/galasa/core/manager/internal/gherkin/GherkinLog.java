package dev.galasa.core.manager.internal.gherkin;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;

public class GherkinLog {

    public final static GherkinKeyword keyword = GherkinKeyword.THEN;

    public final static Pattern pattern = Pattern.compile("Write to log \"(.*)\"");

    public final static Class<?>[] dependencies = {};

    private final static Pattern patternVariable = Pattern.compile("(<(\\w+)>)");

    public static void execute(Matcher matcherLog, Map<String, Object> testVariables, Log logger) {
        String output = matcherLog.group(1);
        Matcher matcherVariable = patternVariable.matcher(output);
        while(matcherVariable.find()) {
            String variableName = matcherVariable.group(2);
            String variableValue = (String) testVariables.get(variableName);
            output = output.replace(matcherVariable.group(1), variableValue);
            matcherVariable = patternVariable.matcher(output);
        }

        logger.info(output);
    }
    
}