package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.ssl.InvalidSSLConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Configuration
public class QpidClientConfig {


	private final SSLContext sslContext;

	@Autowired
	public QpidClientConfig(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	private CloseableHttpClient httpsClient() {
		DefaultClientTlsStrategy strategy = new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
				.create()
				.setTlsSocketStrategy(strategy)
				.build();
		return HttpClients
				.custom()
				.setConnectionManager(connectionManager)
				.build();
	}

	@Bean
	public RestTemplate qpidRestTemplate() throws InvalidSSLConfig {
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpsClient()));
	}
}
