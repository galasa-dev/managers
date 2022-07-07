/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.githubissue.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonObject;

import dev.galasa.ManagerException;
import dev.galasa.Test;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.githubissue.GitHubIssue;
import dev.galasa.githubissue.GitHubIssueManagerException;
import dev.galasa.githubissue.Issue;
import dev.galasa.githubissue.internal.properties.GitHubIssueInstanceUrl;
import dev.galasa.githubissue.internal.properties.GitHubIssuePropertiesSingleton;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.IHttpManager;
import dev.galasa.http.spi.IHttpManagerSpi;

@Component(service = { IManager.class })
public class GitHubIssueManagerImpl extends AbstractManager {
	
	private static final Log logger = LogFactory.getLog(GitHubIssueManagerImpl.class);
	
	protected static final String NAMESPACE = "githubissue";
	
	private boolean required;
	
	private GalasaTest galasaTest;
	
	private GitHubIssue gitHubIssue;
	
	private IHttpManagerSpi  httpManager;
	private IHttpClient      httpClient;
	
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);
		
		// Check to see if GitHubIssue annotation is present in the test class or on any of the test methods
		// If it is, activate this manager
		
		if (!required) {
			
			if (galasaTest.isJava()) {
				Class<?> testClass = galasaTest.getJavaTestClass();
				
				this.gitHubIssue = testClass.getAnnotation(GitHubIssue.class); // Set globally as might need later
				if (this.gitHubIssue == null) { 
					
					GitHubIssue gitHubIssue = null;
					for (Method testMethod : testClass.getMethods()) {
						gitHubIssue = testMethod.getAnnotation(GitHubIssue.class);
						if (gitHubIssue != null) {
							required = true;
							break;
						}
					}
					// Annotation not present on the class or any methods
					if (!required) {
						return;
					}
				}
			} else {
				return; // Only support Java
			}
		}
		
		logger.info("GitHubIssueManager needed");
		
		this.galasaTest = galasaTest;
		
		try {
            GitHubIssuePropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new GitHubIssueManagerException("Unable to request framework services", e);
        }	
		
		youAreRequired(allManagers, activeManagers, galasaTest);
	
	}
	
	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.youAreRequired(allManagers, activeManagers, galasaTest);
		
		if (activeManagers.contains(this)) {
			return;
		}
		
		this.required = true;
		activeManagers.add(this);	
		
		this.httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (this.httpManager == null) {
            throw new GitHubIssueManagerException("Unable to locate the Http Manager, required for the GitHub Issue Manager");
        }
	}
	
	@Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if (otherManager instanceof IHttpManager) {
            return true;
        }
        return false;
    }
	
	@Override
	public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws GitHubIssueManagerException {
		logger.info("endOfTestClass");
		logger.info("Current result for class: " + currentResult);
		String newResult = null;

		// If this test class is annotated with GitHubIssue, override it's result if needed
		if (this.gitHubIssue != null) {
			logger.info("Issue: " + this.gitHubIssue.issue());
			// TO DO - Change currentResult to Result type 
			newResult = overrideClassResult(this.galasaTest, Result.failed("Failed"));
		}
		
		return newResult;
		
	}
	
	private String overrideClassResult(@NotNull GalasaTest klass, @NotNull Result currentResult) throws GitHubIssueManagerException {
		logger.info("overrideClassResult");
		
		if (!currentResult.isFailed()) {
			return null;
		}

		logger.info("About to get issue");
		Issue issue = getGitHubIssue(this.gitHubIssue.issue());
		
		boolean failedNonDefectMethod = false;
		boolean failedDefectMethod    = false;
		boolean passedMethod          = false;
		
		// TO DO - Iterate through methods and determine if any are failed with defects
	
		return currentResult.toString();
		// TO DO - Failed with defects result
//		return Result.passed().toString();
		
	}
	
	@Override
	public String endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull String currentResult,
			Throwable currentException) throws GitHubIssueManagerException {
		logger.info("endOfTestMethod");
		logger.info("Current result for method: " + currentResult);
		
		String newResult = null;
		
		if (isTestMethod(galasaMethod)) {
			// If this test method is annotated with GitHubIssue, override it's result if needed
			GitHubIssue gitHubIssue = galasaMethod.getJavaExecutionMethod().getAnnotation(GitHubIssue.class);
			if (gitHubIssue != null) {
				logger.info("Issue: " + gitHubIssue.issue());
				// TO DO - Change currentResult to Result type 
				newResult = overrideMethodResult(galasaMethod, Result.failed("Failed"));
			}
		}
		
		return newResult;
	}
	
	private String overrideMethodResult(GalasaMethod method, Result currentResult) throws GitHubIssueManagerException {
		logger.info("overrideMethodResult");

		if (!currentResult.isFailed()) {
			return null;
		}
		
		if (!isDefectCheckEnabled()) {
			return null;
		}
		
		GitHubIssue gitHubIssue = method.getJavaExecutionMethod().getAnnotation(GitHubIssue.class);
		if (gitHubIssue == null) {
			return null;
		}
		
		if (Integer.parseInt(gitHubIssue.issue()) <= 0) {
			return null;
		}
		
		// TO DO - Check regex exception for more detailed search
		
		logger.info("About to get issue");
		Issue issue = getGitHubIssue(gitHubIssue.issue());
		if (issue == null) {
			return null;
		}
		
		if (issue.isClosed()) {
			return null;
		}
		
		logger.info("Overriding FAILED to FAILED WITH DEFECTS due to open GitHub issue " + gitHubIssue.issue() + ": " + issue.getTitle());
		
		return currentResult.toString();
		// CHANGE TO FAILED_DEFECTS
//		return Result.passed().toString();
		
	}
	
	private Issue getGitHubIssue(String issueNumber) throws GitHubIssueManagerException {
		
		if (this.httpClient == null) {
        	this.httpClient = this.httpManager.newHttpClient();
        	try {
				this.httpClient.setURI(GitHubIssueInstanceUrl.get().toURI());
			} catch (URISyntaxException e) {
				throw new GitHubIssueManagerException("Badly formed URI for the githubissue.instance.url", e);
			}
        }
		
		// TO DO - Make URL from CPS properties
		String url = "https://api.github.com/repos/galasa-dev/projectmanagement/issues/1030";
				
		logger.info(url);
		
		try {
			HttpClientResponse<JsonObject> response = this.httpClient.getJson(url);
			
			if (response.getStatusCode() == 200) {
            	logger.trace("Located GitHub issue '" + issueNumber + "' on website");
            	JsonObject json = response.getContent();
            	// TO DO - Use Gson for Json conversion and get the fields
                return new Issue("1030", "open", "https://github.com/galasa-dev/projectmanagement/issues/1030", "Convert RTCDefect annotation");
        	} else {
        		throw new GitHubIssueManagerException("Unable to read GitHub issue '" + issueNumber + "' from url " + url + " - " + response.getStatusLine());
        	}
		} catch (HttpClientException e) {
        	throw new GitHubIssueManagerException("Unable to read GitHub issue '" + issueNumber + "' from url " + url, e);
		}
        
	}
	
	/**
	 * Utility method that checks that the passed method is in fact
	 * a java method and has been annotated @Test
	 * @param method the method to test
	 * @return true if method is java and annotated @Test else false
	 */
	private boolean isTestMethod(GalasaMethod method) {
		if (!method.isJava()) {
			return false;
		}
		for (Annotation a : method.getJavaExecutionMethod().getAnnotations()) {
			if (a instanceof Test) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isDefectCheckEnabled() {
		try {
			// TO DO - Check if this will be a property in the CPS
			return true;
		} catch(Exception e) {
			logger.error("Unable to determine if defect check enabled", e);
			return false;
		}
	}
	
	
}