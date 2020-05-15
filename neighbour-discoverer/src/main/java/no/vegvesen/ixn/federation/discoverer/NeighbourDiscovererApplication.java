package no.vegvesen.ixn.federation.discoverer;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Component
public class NeighbourDiscovererApplication {


	private SSLContext sslContext;

	@Autowired
	public NeighbourDiscovererApplication(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	@Bean
	HttpClient createHttpClient() {
		return HttpClients.custom().setSSLContext(sslContext).build();
	}

	@Bean
	public RestTemplate restTemplate() {

		// TODO: set custom timeout on rest template

		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(createHttpClient()));
	}

}
