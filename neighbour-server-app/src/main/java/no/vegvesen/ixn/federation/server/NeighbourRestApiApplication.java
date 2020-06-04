package no.vegvesen.ixn.federation.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class NeighbourRestApiApplication {

	@Bean
	SSLContext sslContext() throws NoSuchAlgorithmException {
		return SSLContext.getDefault();
	}

	public static void main(String[] args) {
		SpringApplication.run(NeighbourRestApiApplication.class, args);
	}

}

