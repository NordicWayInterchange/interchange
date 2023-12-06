package no.vegvesen.ixn.federation.discoverer;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
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

	CloseableHttpClient createHttpClient() {
		return HttpClients.custom().setSSLContextFactory(new SSLConnectionSocketFactory(sslContext)).build();
	}

	@Bean
	public RestTemplate neighbourRestTemplate() {

		// TODO: set custom timeout on rest template

		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(createHttpClient()));
	}

}
