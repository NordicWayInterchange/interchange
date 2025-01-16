package no.vegvesen.ixn.federation.ssl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

@Configuration
public class DefaultSSLContextConfig {

	@Bean
	@Primary
	public SSLContext defaultSslContext() throws NoSuchAlgorithmException {
		return SSLContext.getDefault();
	}
}
