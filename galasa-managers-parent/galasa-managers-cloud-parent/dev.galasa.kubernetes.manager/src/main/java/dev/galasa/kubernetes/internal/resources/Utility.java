package dev.galasa.kubernetes.internal.resources;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.galasa.kubernetes.KubernetesManagerException;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1LabelSelectorRequirement;

public class Utility {

    
    public static String convertLabelSelector(V1LabelSelector labelSelector) throws KubernetesManagerException {
        if (labelSelector == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        
        List<V1LabelSelectorRequirement> expressions = labelSelector.getMatchExpressions();
        if (expressions != null && !expressions.isEmpty()) {
            throw new KubernetesManagerException("The V1LabelSelector converter cannot support expressions at this time");
        }
        
        Map<String, String> matchLabels = labelSelector.getMatchLabels();
        if (matchLabels == null || matchLabels.isEmpty()) {
            return null;
        }
        
        for(Entry<String, String> entry : matchLabels.entrySet()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
        }
        
        return sb.toString();
    }
    
    
    
}
