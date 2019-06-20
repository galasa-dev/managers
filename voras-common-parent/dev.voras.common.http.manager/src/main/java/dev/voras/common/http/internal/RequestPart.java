package dev.voras.http.internal;

import org.apache.http.entity.mime.content.ContentBody;

public interface RequestPart {

	ContentBody getBody();
	
	String getType();
}
