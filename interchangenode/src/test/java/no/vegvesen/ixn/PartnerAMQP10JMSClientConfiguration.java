package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Uses a configuration annotation to define an AMQP10JMSConnectionFactoryCustomizer
 * override that configures the Qpid JMS ConnectionFactory used by the starter to match
 * requirements of the user.  In this case the login credentials are set, this could be
 * done in cases where credentials are retrieved from some external resource etc.
 */
@Configuration
public class PartnerAMQP10JMSClientConfiguration {

	@Bean
	public JmsTemplate partnerJmsTemplate(JmsConnectionFactory cf){
		return new JmsTemplate(cf);
	}

	@Bean
	@Primary
	public JmsConnectionFactory partnerFactory(@Value("${partner.amqphub.amqp10jms.remote-url}") String url,
											   @Value("${partner.keystore.resource}") String keystore,
											   @Value("${partner.keystore.password}") String password) throws InvalidSSLConfig {
		JmsConnectionFactory factory = new JmsConnectionFactory(url);
		factory.setSslContext(sslContextFromKeystore(getFilePath(keystore), password.toCharArray()));
		return factory;
	}

	private static String getFilePath(String jksResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load jks resource " + jksResource);
	}

	private SSLContext sslContextFromKeystore(String keystoreResourcePath, char[] password) throws InvalidSSLConfig {
		KeyStore keystore = null;
		try {
			keystore = KeyStore.getInstance("PKCS12");
			keystore.load(new FileInputStream(keystoreResourcePath), password);
			return newSSLContext(keystore, password);
		} catch (Exception e) {
			throw new InvalidSSLConfig(e);
		}
	}


	private static SSLContext newSSLContext(final KeyStore ks, final char[] password) throws InvalidSSLConfig {
		try {
			// Get a KeyManager and initialize it
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password);

			// Get the SSLContext to help create SSLSocketFactory
			final SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), null, null);
			return sslContext;
		} catch (final GeneralSecurityException e) {
			throw new InvalidSSLConfig(e);
		}
	}

	private static class InvalidSSLConfig extends Throwable {
		InvalidSSLConfig(Exception e) {
			super(e);
		}
	}
}
