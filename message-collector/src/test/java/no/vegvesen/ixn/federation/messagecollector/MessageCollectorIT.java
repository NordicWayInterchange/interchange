package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.Connection;
import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorIT extends QpidDockerBaseIT {

	@Container
	static KeysContainer keysContainer = getKeysContainer(MessageCollectorIT.class,"my_ca","localhost","sp_producer","sp_consumer");

	@Container
	public QpidContainer consumerContainer = getQpidTestContainer("docker/consumer",
			keysContainer.getKeyFolderOnHost(),
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost")
			.dependsOn(keysContainer);

	@Container
	public QpidContainer producerContainer = getQpidTestContainer("docker/producer",
			keysContainer.getKeyFolderOnHost(),
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost")
			.dependsOn(keysContainer);

	public Sink createSink(String url, String queueName, String keyStore) {
		return new Sink(url,queueName,TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(),keyStore,"truststore.jks"));
	}

	public Source createSource(String url, String queue, String keyStore) {
		return new Source(url,queue,TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(),keyStore,"truststore.jks"));
	}

	@Test
	public void testMessagesCollected() throws NamingException, JMSException {
		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", producerContainer.getAmqpsUrl(), "localhost", new Connection());

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getMappedPort(AMQPS_PORT).toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(),"localhost.p12", "truststore.jks"),
				"localhost",
				localIxnFederationPort,
				"fedEx");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

		Source source = createSource(producerContainer.getAmqpsUrl(), "localhost", "sp_producer.p12");
		source.start();

		Sink sink = createSink(consumerContainer.getAmqpsUrl(), "sp_consumer", "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();

		source.sendNonPersistent("fishy fishy", "SE", 8000L);

		Message message = consumer.receive(2000);
		assertThat(message).withFailMessage("Expected message is not routed").isNotNull();
		assertThat(message.getJMSExpiration()).withFailMessage("Routed message has noe expiry specified").isNotEqualTo(0L);
	}

	@Test
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", producerContainer.getAmqpsUrl(), "localhost", new Connection());

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getMappedPort(AMQPS_PORT).toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(),"localhost.p12", "truststore.jks"),
				"localhost",
				localIxnFederationPort,
				"fedEx");
		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

		Source source = createSource(producerContainer.getAmqpsUrl(), "localhost", "sp_producer.p12");
		source.start();
		source.sendNonPersistent("fishy fishy", "SE", 1000L);

		Thread.sleep(2000); // wait for the message to expire with extra margin

		Sink sink = createSink(consumerContainer.getAmqpsUrl(), "sp_consumer", "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();
		Message message = consumer.receive(1000);
		assertThat(message).withFailMessage("Expected message is not routed").isNull();
	}

}
