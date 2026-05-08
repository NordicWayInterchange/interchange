package no.vegvesen.ixn.federation.adminserver.qpid;

import no.vegvesen.ixn.ssl.InvalidSSLConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Configuration
public class AdminQpidClientConfig {


	private final SslBundles sslBundles;

	@Autowired
	public AdminQpidClientConfig(SslBundles sslBundles) {
		this.sslBundles = sslBundles;
	}

	private CloseableHttpClient httpsClient() {
		SSLContext context = sslBundles.getBundle("qpid").createSslContext();
		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
				.create()
				.setTlsSocketStrategy(new ClientTlsStrategyBuilder().setSslContext(context).buildClassic())
				.setMaxConnTotal(5)
				.setMaxConnPerRoute(2)
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
