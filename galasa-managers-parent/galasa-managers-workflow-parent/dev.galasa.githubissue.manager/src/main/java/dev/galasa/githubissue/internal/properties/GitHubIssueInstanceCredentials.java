/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.githubissue.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.githubissue.GitHubIssueManagerException;

public class GitHubIssueInstanceCredentials extends CpsProperties {
	
	public static String get() throws GitHubIssueManagerException  {
		return getStringWithDefault(GitHubIssuePropertiesSingleton.cps(), "GITHUB", "instance", "credentials");
		
	}
	
}
