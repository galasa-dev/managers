package dev.galasa.http.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public class FrameworkHttpClientImpl extends HttpClientImpl {
	
	private final HttpManagerImpl manager;
	private final boolean         archive;
    protected static final Log  logger = LogFactory.getLog(FrameworkHttpClientImpl.class);


	public FrameworkHttpClientImpl(int timeout, boolean archive, HttpManagerImpl manager) {
		super(timeout);
		this.manager = manager;
		this.archive = archive;
	}
	
	protected void archive(HttpUriRequest req) {
    	if(!archive) {
    		return;
    	}
    	tempHeaders = req.getAllHeaders();
    }
    
	protected void archive(CloseableHttpResponse resp) {
    	if(!archive) {
    		return;
    	}
    	archiveTheseHeaders(tempHeaders,resp.getAllHeaders());
    }
    
	private void archiveTheseHeaders(Header[] requestHeaders, Header[] responseHeaders) {
    	String requestData = createArchiveString(requestHeaders);
    	String responseData = createArchiveString(responseHeaders);
    	
    	Path folder = manager.getStoredArtifactRoot()
    			             .resolve(RAS_NAMESPACE)
    			             .resolve(RAS_HEADERS)
    			             .resolve(getCurrentMethod())
    			             .resolve("request:" + archiveIndex);
    	try {
    		Files.write(folder.resolve("RequestHeaders"), requestData.getBytes(), StandardOpenOption.CREATE);
    		Files.write(folder.resolve("ResponseHeaders"), responseData.getBytes(), StandardOpenOption.CREATE);
    	} catch(IOException e) {
    		logger.info("Unable to log headers for a request",e);
    	}
    	
    }
    
    private String createArchiveString(Header [] headers) {
    	StringBuilder sb = new StringBuilder();
    	for(Header h : headers) {
    		sb.append(h.getName());
    		sb.append(":");
    		sb.append(h.getValue());
    		sb.append("\n");
    	}
    	return sb.toString();
    }
    
    private String getCurrentMethod() {
    	String managerCurrentMethod = manager.getCurrentMethod();
    	if(this.currentMethod.contentEquals(managerCurrentMethod)) {
    		return this.currentMethod;
    	} else {
    		this.currentMethod = managerCurrentMethod;
    		this.archiveIndex = 1;
    	}
    	return this.currentMethod;
    }

}
