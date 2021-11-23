/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.http.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.http.IHttpClient;
import dev.galasa.http.IHttpManager;

public interface IHttpManagerSpi extends IHttpManager {

    @NotNull
    IHttpClient newHttpClient();

    @NotNull
    IHttpClient newHttpClient(int timeout);
    
    @NotNull
    IHttpClient newHttpClient(boolean archiveHeaders);
    
    @NotNull
    IHttpClient newHttpClient(int timeout, boolean archiveHeaders);

}
