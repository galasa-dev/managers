/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.githubissue.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private HashMap<GalasaMethod, HashMap<String, Object>> results = new HashMap<GalasaMethod, HashMap<String, Object>>();
	
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
	public Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult,
			Throwable currentException) throws GitHubIssueManagerException {
		
		Result newResult = null;
		
		if (isTestMethod(galasaMethod)) {
			// If this test method is annotated with GitHubIssue, override it's result if needed
			GitHubIssue gitHubIssue = galasaMethod.getJavaExecutionMethod().getAnnotation(GitHubIssue.class);
			if (gitHubIssue != null) {
				newResult = overrideMethodResult(galasaMethod, currentResult, currentException);
			}
			
		}
		
		// Record method result for later
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (newResult != null) {
			result.put("result", newResult);
		} else {
			result.put("result", currentResult);
		}
		result.put("throwable", currentException);
		results.put(galasaMethod, result);					
		
		return newResult;
	}
	
	private Result overrideMethodResult(@NotNull GalasaMethod method, @NotNull Result currentResult, Throwable currentException) throws GitHubIssueManagerException {

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
		
		if (!checkRegexException(gitHubIssue.regex(), currentException)) {
			return null;
		}
		
		String repo = getRepo(gitHubIssue);
		Issue issue = getGitHubIssue(repo, gitHubIssue.issue());
		if (issue == null) {
			return null;
		}
		
		if (issue.isClosed()) {
			return null;
		}
		
		logger.info("Overriding 'Failed' to 'Failed With Defects' due to open GitHub issue " + gitHubIssue.issue() + ": " + issue.getTitle());
		
		return Result.custom("Failed With Defects", false, true, true, false, false, false, false, currentException.toString());
		
	}
	
	@Override
	public Result endOfTestClass(@NotNull Result currentResult, Throwable currentException) throws GitHubIssueManagerException {

		Result newResult = null;

		// If this test class is annotated with GitHubIssue, override it's result if needed
		if (this.gitHubIssue != null) {
			newResult = overrideClassResult(this.galasaTest, currentResult);
		}
		
		return newResult;
		
	}
	
	private Result overrideClassResult(@NotNull GalasaTest klass, @NotNull Result currentResult) throws GitHubIssueManagerException {
		
		if (!currentResult.isFailed()) {
			return null;
		}

		String repo = getRepo(gitHubIssue);
		Issue issue = getGitHubIssue(repo, this.gitHubIssue.issue());
		
		boolean failedNonDefectMethod = false;
		boolean failedDefectMethod    = false;
		boolean passedMethod          = false;
	
		for (Map.Entry<GalasaMethod, HashMap<String,Object>> method : results.entrySet()) {
			
			HashMap<String, Object> entry = method.getValue();
			Result result = (Result) entry.get("result");
			Throwable throwable = (Throwable) entry.get("throwable");
			
			switch(result.getName()) {
			case "Failed":
				
				if (this.gitHubIssue != null && this.gitHubIssue.regex() != null && this.gitHubIssue.regex().length > 0) {
					if (checkRegexException(this.gitHubIssue.regex(), throwable)) {
						failedNonDefectMethod = true;
					}
				} else {
					failedNonDefectMethod = true;
				}	
				break;
			
			case "Failed With Defects": 
				failedDefectMethod = true;
				break;
				
			case "Passed":
				passedMethod = true;
				break;
				
			default:
				break;
			}
			
			
		}
		
		if (issue != null && !issue.isClosed()) {
			if (failedDefectMethod || failedNonDefectMethod) {
				logger.info("Overriding 'Failed' to 'Failed With Defects' due to open GitHub issue " + this.gitHubIssue.issue() + ": " + issue.getTitle());
				return Result.custom("Failed With Defects", false, true, true, false, false, false, false, "");
			}
		}
		
		if (failedNonDefectMethod || !failedDefectMethod) {
			return null;
		}
		
		if (passedMethod) {
			logger.info("Overriding 'Passed' to 'Passed With Defects' due to at least one method passing and at least one 'Failed With Defects' method");
			return Result.custom("Passed With Defects", true, false, true, false, false, false, false, "");
		}
 		
	
		logger.info("Overriding 'Failed' to 'Failed With Defects' due to no passing methods");
		return Result.custom("Failed With Defects", false, true, true, false, false, false, false, "");
	}
	
	private boolean checkRegexException(String[] regexs, Throwable exception) {
		
		if (regexs == null || regexs.length == 0) {
			return true;
		}
		
		String exceptionStackTrace = convertException(exception);
		
		for (String r : regexs) {
			Pattern pattern = Pattern.compile(r);
			Matcher matcher = pattern.matcher(exceptionStackTrace);
			
			if (matcher.find()) {
				return true;
			}
		}
		
		return false;
		
	}
	
	private String convertException(Throwable exception) {
		
		if (exception == null) {
			return "";
		}
		
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			return sw.toString();
		} catch(Exception e) { 
			logger.error("Unable to create stacktrace of exception",e);
		}
		
		return null;
		
	}
	
	private Issue getGitHubIssue(String repo, String issueNumber) throws GitHubIssueManagerException {
		
		String fullUrl = getUrl(repo, issueNumber);
		
		if (this.httpClient == null) {
			
			this.httpClient = this.httpManager.newHttpClient();
			
			String uriString = fullUrl.substring(0, fullUrl.indexOf("/repos"));
			URI uri = null;
			try {
				uri = new URI(uriString);
			} catch (URISyntaxException e) {
				throw new GitHubIssueManagerException("Badly formed URI", e);
			}
			this.httpClient.setURI(uri);
        	
        	// Authenticate as might be GitHub Enterprise 
        	ICredentials creds = null;
			try {
				creds = getCreds();
			} catch (CredentialsException e) {
				throw new GitHubIssueManagerException("Unable to get credentials for the GitHub instance", e);
			}
			if (creds instanceof ICredentialsUsernamePassword) {
				String username = ((ICredentialsUsername) creds).getUsername();
				String password = ((ICredentialsUsernamePassword) creds).getPassword();
				this.httpClient = this.httpClient.setAuthorisation(username, password);
			}
        }
		
		String url = fullUrl.substring(fullUrl.indexOf("/repos"));
		
		try {
			
			HttpClientResponse<JsonObject> response = this.httpClient.getJson(url);
			
			if (response.getStatusCode() == 200) {
            	JsonObject json = response.getContent();
            	Issue issue = getIssuePojo(json, issueNumber);
            	logger.info("Issue '" + issue.getIssue() + "' is " + issue.getState() + " - " + issue.getUrl());
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
	
	private String getUrl(String repo, String issueNumber) throws GitHubIssueManagerException {
		
		String gitHubInstance = GitHubIssueInstanceUrl.get().toString();
		gitHubInstance = gitHubInstance.substring(8);
		
		String url = "https://api." + gitHubInstance + "/repos/" + repo + "/issues/" + issueNumber;
		
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
	
	private Issue getIssuePojo(JsonObject json, String issueNumber) {
		
		String state = json.get("state").getAsString();
		String htmlUrl = json.get("html_url").getAsString();
		String title = json.get("title").getAsString();
		
		return new Issue(issueNumber, state, htmlUrl, title);
	}
	
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
			// TO DO - Check if this is meant to be a property in the CPS
			return true;
		} catch(Exception e) {
			logger.error("Unable to determine if defect check enabled", e);
			return false;
		}
	}
	
	
}