package no.vegvesen.ixn;

import org.amqphub.spring.boot.jms.autoconfigure.AMQP10JMSConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Uses a configuration annotation to define an AMQP10JMSConnectionFactoryCustomizer
 * override that configures the Qpid JMS ConnectionFactory used by the starter to match
 * requirements of the user.  In this case the login credentials are set, this could be
 * done in cases where credentials are retrieved from some external resource etc.
 */
@Configuration
class CustomAMQP10JMSClientConfiguration {

	@Bean
	public AMQP10JMSConnectionFactoryCustomizer myAMQP10Configuration() {
		return (factory) -> {
			factory.setPopulateJMSXUserID(true);

			// Other options such as custom SSLContext can be applied here
			// where they might otherwise be difficult to set via properties
			// file or URI.
		};
	}
}
