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
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class QpidClientConfig {

    private static final class QpidClientRetryLogger extends DefaultHttpRequestRetryHandler {
		private final Logger logger = LoggerFactory.getLogger(QpidClientRetryLogger.class);


		/**
		 * We keep getting intermittent SSLHandshake exceptions. Take that specific exception out of the list of
		 * non-retryable exceptions, which means that we need to explicitly list all the subclasses of SSLException,
		 * except the one we want retryable (SSLHandshakeException)
		 */
		public QpidClientRetryLogger() {
			super(3,false, Arrays.asList(
					InterruptedIOException.class,
					UnknownHostException.class,
					ConnectException.class,
					SSLKeyException.class,
					SSLPeerUnverifiedException.class,
					SSLProtocolException.class));
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
