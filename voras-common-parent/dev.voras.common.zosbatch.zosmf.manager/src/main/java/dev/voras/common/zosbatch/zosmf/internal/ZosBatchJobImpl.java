package dev.voras.common.zosbatch.zosmf.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.voras.ICredentials;
import dev.voras.ICredentialsUsernamePassword;
import dev.voras.common.http.HttpClientException;
import dev.voras.common.http.HttpClientResponse;
import dev.voras.common.http.IHttpClient;
import dev.voras.common.zos.IZosImage;
import dev.voras.common.zos.ZosManagerException;
import dev.voras.common.zosbatch.IZosBatchJob;
import dev.voras.common.zosbatch.IZosBatchJobname;
import dev.voras.common.zosbatch.ZosBatchException;
import dev.voras.common.zosbatch.ZosBatchManagerException;

//TODO: Return code????
//TODO: getStatus(), getMaxcc()
//TODO: Use artifact manager
public class ZosBatchJobImpl implements IZosBatchJob {
	
	private IZosImage image;
	private IZosBatchJobname jobname;
	private String jcl;	
	private IHttpClient httpClient;
	private int defaultJobTimeout;
	private String zosmfHostname;
	private String zosmfPort;
	
	private String jobid;			
	private String status;			
	private String retcode;
	private String jobUrl;
	private String filesUrl;
	
	private static final Log logger = LogFactory.getLog(ZosBatchJobImpl.class);

	public ZosBatchJobImpl(IZosImage image, IZosBatchJobname jobname, String jcl) throws ZosBatchManagerException {
		this.image = image;
		this.jobname = jobname;
		this.jcl = jcl;
		this.httpClient = ZosBatchManagerImpl.httpManager.newHttpClient();	
		this.defaultJobTimeout = ZosBatchManagerImpl.zosBatchProperties.getDefaultJobWaitTimeout(image.getImageID());
		try {
			this.zosmfHostname = image.getDefaultHostname();
		} catch (ZosManagerException e) {
			throw new ZosBatchManagerException("Probelm getting default hostname for image " + image.getImageID(), e);
		}
		this.zosmfPort = ZosBatchManagerImpl.zosBatchProperties.getZosmfPort(image.getImageID());
	}
	
	public @NotNull IZosBatchJob submitJob() throws ZosBatchException {
		buildHttpClient();
		
		try {
			String url = "https://" + zosmfHostname + ":" + zosmfPort + "/zosmf/restjobs/jobs";
			HttpClientResponse<String> response = httpClient.putText(url, jclWithJobcard());
			logger.debug(response.getStatusLine() + " - " + url);
			if (response.getStatusCode() == 201) {
				JsonObject content = new JsonParser().parse(response.getContent()).getAsJsonObject();
				
				this.jobid = content.get("jobid").getAsString();
				this.jobUrl = content.get("url").getAsString();
				this.filesUrl = content.get("files-url").getAsString();
				
				String memberName = "retcode";
				if (content.get(memberName) != null && !content.get(memberName).isJsonNull()) {
					this.retcode = content.get(memberName).getAsString();
				}
			}
		} catch (HttpClientException e) {
			throw new ZosBatchException("Problem submitting job to z/OSMF", e);
		}
		
		return this;
	}
	
	
	@Override
	public int waitForJob() throws ZosBatchException {

		long timeoutTime = Calendar.getInstance().getTimeInMillis()	+ defaultJobTimeout;
		while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
			try {
				if (this.status == null || this.status.equals("OUTPUT")) {
					return -1;
				}
				updateJobStatus();
				
				Thread.sleep(500);
	        } catch (InterruptedException e) {
	        	logger.error("waitForJob Interrupted", e);
	        	Thread.currentThread().interrupt();
	        }
		}
		return 0;
	}
	
	@Override
	public List<String> retrieveOutput() throws ZosBatchException {
		
		List<String> output = new ArrayList<>();
		
		if (filesUrl == null) {
			return output;
		}		
		
		try {
			HttpClientResponse<String> filesUrlResponse = httpClient.getText(filesUrl);
			String statusLine = filesUrlResponse.getStatusLine();
			logger.debug(statusLine + " - " + filesUrl);
			//TODO: if (filesUrlResponse.getStatusCode() ...
			String content = filesUrlResponse.getContent();
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonFiles = jsonParser.parse(content);
			JsonArray jsonArray = jsonFiles.getAsJsonArray();

			for (JsonElement jsonElement : jsonArray) {
			    JsonObject jsonObject = jsonElement.getAsJsonObject();
			    String recordsUrl = jsonObject.get("records-url").getAsString();
			    HttpClientResponse<String> recordsUrlResponse = httpClient.getText(recordsUrl);
				logger.debug(recordsUrlResponse.getStatusLine() + " - " + recordsUrl);
				//TODO: if (recordsUrlResponse.getStatusCode() ...
				output.add(recordsUrlResponse.getContent());
			}
		} catch (HttpClientException e) {
			throw new ZosBatchException("Problem retrieving job output from z/OSMF", e);
		}
		return output;
	}
	
	@Override
	public String getJcl() throws ZosBatchException {
		return this.jcl;
	}
	
	@Override
	public IZosBatchJobname getJobname() throws ZosBatchException {
		return this.jobname;
	}
	
	@Override
	public String toString() {
		updateJobStatus();
		return "JOBID=" + this.jobid + " JOBNAME=" + this.jobname.getName() + " STATUS=" + this.status + " " + (this.retcode != null ? this.retcode : "");		
	}


	private void buildHttpClient() throws ZosBatchException {
		try {
			ICredentials creds = image.getDefaultCredentials();
			this.httpClient.setURI(new URI("https://" + zosmfHostname + ":" + zosmfPort));
			if (creds instanceof ICredentialsUsernamePassword) {
				this.httpClient.setAuthorisation(((ICredentialsUsernamePassword) creds).getUsername(), ((ICredentialsUsernamePassword) creds).getPassword());
			}
			this.httpClient.setTrustingSSLContext();
			this.httpClient.build();
		} catch (HttpClientException | ZosManagerException | URISyntaxException e) {
			throw new ZosBatchException("ERROR", e);
		}	
	}

	private String jclWithJobcard() {
		return "//" + jobname.getName() + " JOB \n" + jcl;
	}

	private void updateJobStatus() {
		if (jobUrl == null) {
			return;
		}
		try {
			HttpClientResponse<JsonObject> response = httpClient.getJson(jobUrl);
			logger.debug(response.getStatusLine() + " - " + jobUrl);
			//TODO: if (response.getStatusCode() ...
			JsonObject content = response.getContent();

			//TODO: if (response.getStatusCode() ...
			if (response.getStatusCode() == 200) {
				this.status = content.get("status").getAsString();
			}
		} catch (HttpClientException e) {
        	logger.error(e);
		}
	}

}
