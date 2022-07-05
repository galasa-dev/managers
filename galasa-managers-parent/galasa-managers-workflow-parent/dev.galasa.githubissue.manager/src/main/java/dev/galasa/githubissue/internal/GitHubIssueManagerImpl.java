/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.githubissue.internal;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.githubissue.GitHubIssue;

public class GitHubIssueManagerImpl extends AbstractManager {
	
	private static final Log logger = LogFactory.getLog(GitHubIssueManagerImpl.class);
	
	private boolean required;
	
	private GitHubIssue gitHubIssue;
	
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);
		logger.info("In the GitHubIssueManager");
		// Check to see if GitHubIssue annotation is present in the test class
		// If it is, need to activate
		
		if (!required) {
			if (galasaTest.isJava()) {
				Class<?> testClass = galasaTest.getJavaTestClass();
				
				this.gitHubIssue = testClass.getAnnotation(GitHubIssue.class);
				if (this.gitHubIssue == null) {
					return; // Not required
				}
			} else {
				return; // Only support Java
			}
		}
		
		logger.info("Annotation found");
		
		
		youAreRequired(allManagers, activeManagers, galasaTest);
	
	}
	
	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) {
		
	}
	
	
	
}