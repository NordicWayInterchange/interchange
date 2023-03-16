package no.vegvesen.ixn.federation.ssl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

//@Configuration
public class DefaultSSLContextConfig {

	//@Bean
	public SSLContext defaultSslContext() throws NoSuchAlgorithmException {
		return SSLContext.getDefault();
	}
}
