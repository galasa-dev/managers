package dev.galasa.common.http.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.common.http.IHttpClient;
import dev.galasa.common.http.IHttpManager;

public interface IHttpManagerSpi extends IHttpManager {

	@NotNull
	IHttpClient newHttpClient();

}
