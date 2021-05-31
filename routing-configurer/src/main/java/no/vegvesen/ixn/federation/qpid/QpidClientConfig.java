package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;

@Configuration
public class QpidClientConfig {

    private static final class QpidClientRetryLogger extends DefaultHttpRequestRetryHandler {
		private final Logger logger = LoggerFactory.getLogger(QpidClientRetryLogger.class);


		public QpidClientRetryLogger() {
			super(3,false);
		}

		@Override
		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
			logger.info("Retry request, count {}, exception {}",executionCount,exception);
			return super.retryRequest(exception, executionCount, context);
		}
	}


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
				.setMaxConnTotal(5)
				.setMaxConnPerRoute(2)
				.setRetryHandler(new QpidClientRetryLogger())
				.build();
	}

	@Bean
	public RestTemplate qpidRestTemplate() throws SSLContextFactory.InvalidSSLConfig {
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpsClient()));
	}
}
