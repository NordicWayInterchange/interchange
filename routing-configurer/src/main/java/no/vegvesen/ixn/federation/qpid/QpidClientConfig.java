package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Configuration
public class QpidClientConfig {

	private final SSLContext sslContext;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	public QpidClientConfig(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	private HttpClient httpsClient() {
		return HttpClients.custom()
				.setSSLContext(sslContext)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.build();
	}

	@Bean
	public RestTemplate qpidRestTemplate() throws SSLContextFactory.InvalidSSLConfig {
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpsClient()));
	}
}
