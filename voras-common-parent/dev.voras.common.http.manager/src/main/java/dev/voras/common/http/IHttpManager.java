package dev.voras.common.http;

import javax.validation.constraints.NotNull;

/**
 * Provides test code access to HTTP Manager to execute HTTP requests
 * 
 * @author Will Yates
 */

 public interface IHttpManager {

		@NotNull
		IHttpClient newHttpClient();
 }