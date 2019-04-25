package no.vegvesen.ixn.federation.discoverer;

import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages={"no.vegvesen.ixn.federation.repository"})
@EntityScan(basePackages={"no.vegvesen.ixn.federation.model"})
public class NeighbourDiscovererApplication {

	@Bean
	public RestTemplate restTemplate() {
		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
		return new RestTemplate(requestFactory);
	}




	public static void main(String[] args){
		SpringApplication.run(NeighbourDiscovererApplication.class);
	}
}
