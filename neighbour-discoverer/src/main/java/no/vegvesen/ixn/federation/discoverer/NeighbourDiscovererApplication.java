package no.vegvesen.ixn.federation.discoverer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn.federation")
@EnableScheduling
@EnableJpaRepositories(basePackages={"no.vegvesen.ixn.federation.repository"})
@EntityScan(basePackages={"no.vegvesen.ixn.federation.model"})
public class NeighbourDiscovererApplication {


	// todo: move paths to application properties

	private static SSLContext createSSLContext() {

		String keystoreFilename = "test_keys/bouvet.p12";
		String truststoreFilename = "test_keys/truststore.jks";

		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(keystoreFilename, "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(truststoreFilename, "password", KeystoreType.JKS));
	}

	@Bean
	HttpClient createHttpClient() {
		return HttpClients.custom().setSSLContext(createSSLContext()).build();
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
