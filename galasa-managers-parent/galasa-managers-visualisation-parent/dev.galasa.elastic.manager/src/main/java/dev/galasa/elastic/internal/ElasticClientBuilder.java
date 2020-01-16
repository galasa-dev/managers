package dev.galasa.elastic.internal;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;

public class ElasticClientBuilder {
    public static RestHighLevelClient buildClient(String elasticEndpoint) {
		RestHighLevelClient highlevel = null;
		highlevel = new RestHighLevelClient(RestClient.builder(
            // Setting the endpoint address for elasticsearch
            HttpHost.create(elasticEndpoint))
            .setHttpClientConfigCallback(new HttpClientConfigCallback() {
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder;
                }
            }).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                public RequestConfig.Builder customizeRequestConfig(
                        RequestConfig.Builder requestConfigBuilder) {
                    return requestConfigBuilder
                            // Setting default timeouts for requests
                            .setConnectTimeout(5000).setSocketTimeout(120000);
                }
            }));
		return highlevel;
	}
}