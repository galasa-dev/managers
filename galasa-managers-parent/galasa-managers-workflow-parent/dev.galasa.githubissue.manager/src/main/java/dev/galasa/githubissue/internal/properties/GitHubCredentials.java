/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.githubissue.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.githubissue.GitHubIssueManagerException;
import dev.galasa.githubissue.internal.GitHubIssueManagerImpl;

public class GitHubCredentials extends CpsProperties {

	public static String get(GitHubIssueManagerImpl gitHubInstance) throws GitHubIssueManagerException {
        try {
            String credentialsKey = getStringNulled(GitHubIssuePropertiesSingleton.cps(), "instance", "credentials");
           
            // Default value
            if (credentialsKey == null) {
                return "GITHUB";
            }

            return credentialsKey;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GitHubIssueManagerException("Failed to access the CPS to find the credential");
        } 
    }
	
}
