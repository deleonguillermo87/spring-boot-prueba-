package com.helpflow.supportticket.infrastructure.adapter.out.client.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(DispatchClientProperties.class)
public class DispatchClientConfig {

	@Bean
	public RestClient dispatchRestClient(RestClient.Builder builder, DispatchClientProperties properties) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(properties.getConnectTimeout());
		requestFactory.setReadTimeout(properties.getReadTimeout());

		return builder
				.baseUrl(properties.getUrl())
				.requestFactory(requestFactory)
				.build();
	}
}

