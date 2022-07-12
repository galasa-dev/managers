/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.githubissue.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ManagerException;
import dev.galasa.Test;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.githubissue.GitHubIssue;
import dev.galasa.githubissue.GitHubIssueManagerException;
import dev.galasa.githubissue.Issue;
import dev.galasa.githubissue.internal.properties.GitHubCredentials;
import dev.galasa.githubissue.internal.properties.GitHubIssueInstanceRepository;
import dev.galasa.githubissue.internal.properties.GitHubIssueInstanceUrl;
import dev.galasa.githubissue.internal.properties.GitHubIssuePropertiesSingleton;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;

@Component(service = { IManager.class })
public class GitHubIssueManagerImpl extends AbstractManager {
	
	private static final Log logger = LogFactory.getLog(GitHubIssueManagerImpl.class);
	
	private IFramework framework;
	
	protected static final String NAMESPACE = "githubissue";
	
	private boolean required;
	
	private GalasaTest galasaTest;
	
	private GitHubIssue gitHubIssue;
	
	private IHttpManagerSpi  httpManager;
	private IHttpClient      httpClient;
	
	private ICredentialsService credService;
	
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);
		
		// Check to see if GitHubIssue annotation is present in the test class or on any of the test methods
		// If it is, activate this manager
		
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
		
		this.galasaTest = galasaTest;
		
		this.framework = framework;
		
		try {
			this.credService = framework.getCredentialsService();
		} catch (CredentialsException e) {
			logger.error("Could not access credential store from framework.", e);
			throw new GitHubIssueManagerException("Could not access credential store from framework.", e);
		}
		
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
	public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws GitHubIssueManagerException {
		logger.info("endOfTestClass");
		logger.info("Current result for class: " + currentResult);
		String newResult = null;

		// If this test class is annotated with GitHubIssue, override it's result if needed
		if (this.gitHubIssue != null) {
			logger.info("Issue: " + this.gitHubIssue.issue());
			// TO DO - Change currentResult to Result type 
			try {
				newResult = overrideClassResult(this.galasaTest, Result.failed("Failed"));
			} catch (CredentialsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return newResult;
		
	}
	
	private String overrideClassResult(@NotNull GalasaTest klass, @NotNull Result currentResult) throws GitHubIssueManagerException, CredentialsException {
		logger.info("overrideClassResult");
		
		if (!currentResult.isFailed()) {
			return null;
		}

		logger.info("About to get issue");
		String repo = getRepo(gitHubIssue);
		Issue issue = getGitHubIssue(repo, this.gitHubIssue.issue());
		
		logger.info("Issue '" + gitHubIssue.issue() + "' is " + issue.getState() + ". URL:");
		
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
				try {
					newResult = overrideMethodResult(galasaMethod, Result.failed("Failed"));
				} catch (CredentialsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return newResult;
	}
	
	private String overrideMethodResult(GalasaMethod method, Result currentResult) throws GitHubIssueManagerException, CredentialsException {
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
		String repo = getRepo(gitHubIssue);
		Issue issue = getGitHubIssue(repo, gitHubIssue.issue());
		if (issue == null) {
			return null;
		}
		
		logger.info("Issue '" + gitHubIssue.issue() + "' is " + issue.getState() + ". URL:");
		
		if (issue.isClosed()) {
			return null;
		}
		
		logger.info("Overriding FAILED to FAILED WITH DEFECTS due to open GitHub issue " + gitHubIssue.issue() + ": " + issue.getTitle());
		
		return currentResult.toString();
		// CHANGE TO FAILED_DEFECTS
//		return Result.passed().toString();
		
	}
	
	private Issue getGitHubIssue(String repo, String issueNumber) throws GitHubIssueManagerException, CredentialsException {
		
		if (this.httpClient == null) {
			
			this.httpClient = this.httpManager.newHttpClient();
			
			String instanceUrl = GitHubIssueInstanceUrl.get().toString();
			instanceUrl = instanceUrl.substring(8);
			URI uri = null;
			try {
				uri = new URI("https://api." + instanceUrl);
			} catch (URISyntaxException e) {
				throw new GitHubIssueManagerException("Badly formed URI", e);
			}
			logger.info(uri.toString());
	    	this.httpClient.setURI(uri);
        	
        	// Authenticate 
        	ICredentials creds = getCreds();
			setHttpClientAuth(creds);
        }
		
		String url = getUrl(repo, issueNumber);
		// TO DO - Remove
		logger.info(url);
		
		String fullUrl = "https://api." + GitHubIssueInstanceUrl.get() + url;
		
		try {
			HttpClientResponse<JsonObject> response = this.httpClient.getJson(url);
			logger.info(response.getheaders());
			
			if (response.getStatusCode() == 200) {
            	JsonObject json = response.getContent();
            	Issue issue = getIssue(json, issueNumber);
            	return issue;
        	} else {
        		throw new GitHubIssueManagerException("Unable to read GitHub issue '" + issueNumber + "' from url " + fullUrl + " - " + response.getStatusLine());
        	}
		} catch (HttpClientException e) {
        	throw new GitHubIssueManagerException("Unable to read GitHub issue '" + issueNumber + "' from url " + fullUrl, e);
		}
        
	}
	
	private ICredentials getCreds() throws GitHubIssueManagerException, CredentialsException {
		String credKey = GitHubCredentials.get(this);
		return credService.getCredentials(credKey);
	}
	
	private void setHttpClientAuth(ICredentials creds) {
		
		if (creds instanceof ICredentialsUsernamePassword) {

			String username = ((ICredentialsUsername) creds).getUsername();
			String password = ((ICredentialsUsernamePassword) creds).getPassword();

			this.httpClient = this.httpClient.setAuthorisation(username, password);
		}
		
//		if (creds instanceof ICredentialsToken) {
//			
//			String token = ((ICredentialsToken) creds).getToken();
//			
//			this.httpClient = this.httpClient.setau
//		}
		
	}
	
	private String getUrl(String repo, String issueNumber) throws GitHubIssueManagerException {
		
		String gitHubInstance = GitHubIssueInstanceUrl.get().toString();
		gitHubInstance = gitHubInstance.substring(8);
		
//		String url = "https://api." + gitHubInstance + "/repos/" + repo + "/issues/" + issueNumber;
		String url = "/repos/" + repo + "/issues/" + issueNumber;
		
		return url;
		
	}
	
	private String getRepo(GitHubIssue gitHubIssue) throws GitHubIssueManagerException {
		String repo = null;
		if (!gitHubIssue.repository().equals("")) {
			repo = gitHubIssue.repository();
		} else {
			repo = GitHubIssueInstanceRepository.get();
		}
		
		if (repo != null) { 
			return repo;
		} else {
			throw new GitHubIssueManagerException("GitHub repository not provided in annotation or CPS");
		}
	}
	
	private Issue getIssue(JsonObject json, String issueNumber) {
		
		String state = json.get("state").getAsString();
		String htmlUrl = json.get("html_url").getAsString();
		String title = json.get("title").getAsString();
		
		return new Issue(issueNumber, state, htmlUrl, title);
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