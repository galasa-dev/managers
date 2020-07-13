package dev.galasa.http.internal.gherkin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.internal.HttpManagerImpl;

public class GherkinPostText {

    public final static GherkinKeyword keyword = GherkinKeyword.GIVEN;

    public final static Pattern pattern = Pattern
            .compile("The Http Client posts text (.+) to URI (.+) at endpoint (.+)");

    public final static Class<?>[] dependencies = {};

    private final static Pattern patternVariable = Pattern.compile("(<(\\w+)>)");

    public static void execute(Matcher match, HttpManagerImpl manager, Map<String, Object> testVariables)
            throws URISyntaxException, HttpClientException {
        IHttpClient client = manager.newHttpClient();
        client.setURI(new URI(getVariables(match.group(2), testVariables)));
        client.build();

        String text = getVariables(match.group(1), testVariables);

        client.postText(getVariables(match.group(3), testVariables), text);
    }

    private static String getVariables(String group, Map<String, Object> testVariables) {
        Matcher matcherVariable = patternVariable.matcher(group);
        while(matcherVariable.find()) {
            String variableName = matcherVariable.group(2);
            String variableValue = (String) testVariables.get(variableName);
            group = group.replace(matcherVariable.group(1), variableValue);
            matcherVariable = patternVariable.matcher(group);
        }
        return group;
    }
}