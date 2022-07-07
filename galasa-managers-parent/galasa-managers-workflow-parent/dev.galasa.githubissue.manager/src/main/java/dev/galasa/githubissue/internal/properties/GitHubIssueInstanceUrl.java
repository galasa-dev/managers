/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.githubissue.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.githubissue.GitHubIssueManagerException;

public class GitHubIssueInstanceUrl extends CpsProperties {
	
	public static URL get() throws GitHubIssueManagerException {
		String url = getStringWithDefault(GitHubIssuePropertiesSingleton.cps(), "https://github.com", "instance", "url");
		try {
			return new URL(url);
		} catch(MalformedURLException e) {
			throw new GitHubIssueManagerException("Unable to parse the githubissue.instance.url '" + url + "'", e);
		}	
		
	}
	
}
