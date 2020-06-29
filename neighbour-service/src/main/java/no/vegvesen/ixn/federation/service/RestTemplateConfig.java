package no.vegvesen.ixn.federation.service;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Component
public class RestTemplateConfig {


	private SSLContext sslContext;

	@Autowired
	public RestTemplateConfig(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	HttpClient createHttpClient() {
		return HttpClients.custom().setSSLContext(sslContext).build();
	}

	@Bean
	public RestTemplate neighbourRestTemplate() {

		// TODO: set custom timeout on rest template

		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(createHttpClient()));
	}

}
