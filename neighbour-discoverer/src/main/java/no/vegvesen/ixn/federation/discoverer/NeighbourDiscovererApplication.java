package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
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


	// WORK IN PROGRESS: ssl in communication between server and client

	private static SSLContext createSSLContext() {

		String keystoreFilename = "/Users/ida.berge/Documents/Borealis/interchange/routing-configurer/src/test/resources/jks/localhost.p12";
		String truststoreFilename = "/Users/ida.berge/Documents/Borealis/interchange/routing-configurer/src/test/resources/jks/truststore.jks";

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

		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(createHttpClient()));
	}





	public static void main(String[] args){
		SpringApplication.run(NeighbourDiscovererApplication.class);
	}
}
