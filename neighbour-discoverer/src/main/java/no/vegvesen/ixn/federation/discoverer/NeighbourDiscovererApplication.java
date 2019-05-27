package no.vegvesen.ixn.federation.discoverer;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn.federation")
@EnableScheduling
@EnableJpaRepositories(basePackages={"no.vegvesen.ixn.federation.repository"})
@EntityScan(basePackages={"no.vegvesen.ixn.federation.model"})
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

	public static void main(String[] args){
		SpringApplication.run(NeighbourDiscovererApplication.class);
	}
}
