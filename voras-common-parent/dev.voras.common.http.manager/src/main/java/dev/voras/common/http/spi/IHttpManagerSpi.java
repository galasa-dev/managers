package dev.voras.common.http.spi;

import javax.validation.constraints.NotNull;

import dev.voras.common.http.IHttpClient;
import dev.voras.common.http.IHttpManager;

public interface IHttpManagerSpi extends IHttpManager {

	@NotNull
	IHttpClient newHttpClient();

}
