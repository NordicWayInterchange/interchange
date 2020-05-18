package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.service.NeighbourService;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
@SuppressWarnings("rawtypes")
public class MessageCollectorOldIT extends DockerBaseIT {
	private static Logger logger = LoggerFactory.getLogger(MessageCollectorOldIT.class);

	private RestTemplate restTemplate() {
		CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(localSslContext()).build();
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
	}

	@Rule
	public GenericContainer localContainer = getQpidContainer("docker/consumer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	@Rule
	public GenericContainer remoteContainer = getQpidContainer("docker/producer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	@Rule
	public GenericContainer remoteContainerTwo = getQpidContainer("docker/producer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	private SSLContext localSslContext() {
		return TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	}


	@Test
	public void reCreationOfForwardQueueWillForwardMessagesWhenNewQueueGetsAvailable() throws JMSException, NamingException {
		Integer localMessagePort = localContainer.getMappedPort(AMQPS_PORT);

		Neighbour localNeighbour = mockNeighbour(localContainer,"localhost");

		Neighbour remoteNeighbour = mockNeighbour(remoteContainer, "remote");
		NeighbourService neighbourService = mock(NeighbourService.class);
		//when(fetcher.listNeighbourCandidates()).thenReturn(Collections.singletonList(remoteNeighbour));
		when(neighbourService.listNeighboursToConsumeMessagesFrom()).thenReturn(Collections.singletonList(remoteNeighbour));

		//the forwarder runs on the "remote" node
		CollectorCreator collectorCreator = new CollectorCreator(localSslContext(), "localhost", localContainer.getMappedPort(AMQPS_PORT).toString(), "fedEx");
		MessageCollector messageForwarder = new MessageCollector(neighbourService, collectorCreator);
		messageForwarder.runSchedule();

		String sendUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT).toString());
		Source source = new Source(sendUrl, "localhost", localSslContext());
		source.start();
		source.send("fisk");

		Sink sink = new Sink(localNeighbour.getMessageChannelUrl(), "sp_consumer", localSslContext());
		MessageConsumer sinkConsumer = sink.createConsumer();
		Message receive1 = sinkConsumer.receive(1000);
		assertThat(receive1).withFailMessage("no messages are routed").isNotNull();

		logger.debug("Removing the queue 'remote' on local node, should give error on consumer");
		QpidClient qpidClient = new QpidClient(String.format("https://localhost:%s/", remoteContainer.getMappedPort(HTTPS_PORT)), "localhost", restTemplate());
		qpidClient.removeQueue("localhost");


		when(neighbourService.listNeighboursToConsumeMessagesFrom()).thenReturn(Collections.emptyList());
		messageForwarder.runSchedule();


		logger.debug("Recreating the message queue");
		when(remoteNeighbour.getSubscriptionRequest()).thenReturn(new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, Collections.emptySet()));
		qpidClient.createQueue(remoteNeighbour.getName());
		qpidClient.addReadAccess(remoteNeighbour.getName(), remoteNeighbour.getName());
		qpidClient.addMemberToGroup(remoteNeighbour.getName(), QpidClient.FEDERATED_GROUP_NAME);

		when(neighbourService.listNeighboursToConsumeMessagesFrom()).thenReturn(Collections.singletonList(remoteNeighbour));
		messageForwarder.runSchedule();

		source.start();
		source.send("mer fisk");
		Message receive2 = sinkConsumer.receive(1000);
		assertThat(receive2).withFailMessage("message sent after messagecollector queue is recreated is not forwarded").isNotNull();
	}

	@Test
	public void forwarderConnectsToTwoRemoteQpidsAndForwardsMessageSent() throws JMSException, NamingException {
		Integer localMessagePort = localContainer.getMappedPort(AMQPS_PORT);

		Neighbour remoteNeighbourOne = mockNeighbour(remoteContainer, "remote");
		Neighbour remoteNeighbourTwo = mockNeighbour(remoteContainerTwo, "remote-two");
		NeighbourService neighbourService = mock(NeighbourService.class);
		when(neighbourService.listNeighboursToConsumeMessagesFrom()).thenReturn(Arrays.asList(remoteNeighbourOne, remoteNeighbourTwo));
		CollectorCreator collectorCreator = new CollectorCreator(localSslContext(), "localhost", localMessagePort.toString(), "fedEx");
		MessageCollector messageForwarder = new MessageCollector(neighbourService, collectorCreator);
		messageForwarder.runSchedule();

		String remoteMessagingUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT).toString());
		Source sourceRemoteOutQueueToLocal = new Source(remoteMessagingUrl, "localhost", localSslContext());
		sourceRemoteOutQueueToLocal.start();
		sourceRemoteOutQueueToLocal.send("fish");

		Sink remoteSink = new Sink(remoteNeighbourOne.getMessageChannelUrl(), "se-out", localSslContext());
		MessageConsumer remoteConsumer = remoteSink.createConsumer();
		Message receive1 = remoteConsumer.receive(1000);
		assertThat(receive1).withFailMessage("first messages is not routed to remote").isNotNull();

		sourceRemoteOutQueueToLocal.send("more fish");
		Message receive2 = remoteConsumer.receive(1000);
		assertThat(receive2).withFailMessage("first messages is not routed").isNotNull();



		Source sourceLocalOutQueueToRemoteTwo = new Source(remoteMessagingUrl, "remote-two", localSslContext());
		sourceLocalOutQueueToRemoteTwo.start();
		sourceLocalOutQueueToRemoteTwo.send("fishy stuff");

		Sink remoteTwoSink = new Sink(remoteNeighbourTwo.getMessageChannelUrl(), "se-out", localSslContext());
		MessageConsumer remoteTwoConsumer = remoteTwoSink.createConsumer();
		Message remoteTwoReceivedMessage = remoteTwoConsumer.receive(1000);
		assertThat(remoteTwoReceivedMessage).withFailMessage("first messages is not routed").isNotNull();

		sourceLocalOutQueueToRemoteTwo.send("more fishy stuff");
		Message remoteTwoReceivedSecondMessage = remoteTwoConsumer.receive(1000);
		assertThat(remoteTwoReceivedSecondMessage).withFailMessage("first messages is not routed").isNotNull();
	}

	private Neighbour mockNeighbour(GenericContainer container, String neighbourName) {
		String messagePort = "" + container.getMappedPort(AMQPS_PORT);
		String remoteControlChannelPort = "" + container.getMappedPort(HTTPS_PORT);

		// All qpid servers runs on localhost, but different ports
		String remoteUrl = String.format("amqps://localhost:%s", messagePort);

		Neighbour remoteNeighbourOne = mock(Neighbour.class);
		when(remoteNeighbourOne.getName()).thenReturn(neighbourName);
		when(remoteNeighbourOne.getControlChannelPort()).thenReturn(remoteControlChannelPort);
		when(remoteNeighbourOne.getMessageChannelPort()).thenReturn(messagePort);
		when(remoteNeighbourOne.getMessageChannelUrl()).thenReturn(remoteUrl);
		return remoteNeighbourOne;
	}



}
