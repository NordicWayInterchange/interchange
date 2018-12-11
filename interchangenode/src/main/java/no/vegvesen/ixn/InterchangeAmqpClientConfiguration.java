package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
class InterchangeAmqpClientConfiguration {

	@Bean
	public JmsTemplate interchangeTemplate(@Autowired @Qualifier("interchangeFactory") JmsConnectionFactory connectionFactory) {
		return new JmsTemplate(connectionFactory);
	}

	@Bean
	public  JmsConnectionFactory interchangeFactory(@Value("${amqphub.amqp10jms.remote-url}") String url,
													@Value("${amqphub.amqp10jms.username}") String username,
													@Value("${amqphub.amqp10jms.password}") String password) {
		JmsConnectionFactory connectionFactory = new JmsConnectionFactory(username, password, url);
		connectionFactory.setPopulateJMSXUserID(true);
		return connectionFactory;
	}
}
