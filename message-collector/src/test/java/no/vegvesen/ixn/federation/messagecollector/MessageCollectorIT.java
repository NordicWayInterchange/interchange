package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageCollectorIT extends DockerBaseIT {

    //TODO this is going to be a test with two nodes (probably qpid), Consumer and Producer.
    //Set the two nodes on a Network, and a Collector inbetween (also on the network?),
    //then set up a Source and a Sink, and send messages through.

    private Network network = Network.newNetwork();

    @Rule
    public GenericContainer consumerContainer = getQpidContainer("docker/consumer",
            "jks",
            "my_ca.crt",
            "localhost.crt",
            "localhost.key");
            //.withNetwork(network)
            //.withNetworkAliases("consumer");

    @Rule
    public GenericContainer producerContainer = getQpidContainer("docker/producer",
            "jks",
            "my_ca.crt",
            "localhost.crt",
            "localhost.key");

    public Sink createSink() {
        return new Sink("amqps://localhost:" + consumerContainer.getMappedPort(AMQPS_PORT),
                "sp_consumer",
                TestKeystoreHelper.sslContext("jks/sp_consumer.p12","jks/truststore.jks"));
    }

    public Source createSource() {
        return new Source("amqps://localhost:" + producerContainer.getMappedPort(AMQPS_PORT),
                "localhost",
                TestKeystoreHelper.sslContext("jks/sp_producer.p12","jks/truststore.jks"));
    }

    @Test
    public void temp_TestThatICanConnectToConsumer() throws JMSException, NamingException {
        Sink sink = createSink();
        MessageConsumer consumer = sink.createConsumer();

    }

    @Test
    public void temp_testThatICanConnectToProducer() throws JMSException, NamingException {
        Source source = createSource();
        source.start();
    }

    @Test
    public void temp_testReadDirectlyFromProducerQueue() throws JMSException, NamingException {
        Source source = createSource();
        source.start();
        source.send("fishy fishy");

        Sink sink = new Sink(String.format("amqps://localhost:%s",producerContainer.getMappedPort(AMQPS_PORT).toString()),
                "localhost",
                TestKeystoreHelper.sslContext("jks/localhost.p12","jks/truststore.jks"));
        MessageConsumer consumer = sink.createConsumer();
        Message message = consumer.receive(1000);
        System.out.println(message.getBody(String.class));
    }

    @Test
    public void temp_testWriteDirectlyToConsumerFedEx() throws JMSException, NamingException {
        String url = "amqps://localhost:" + consumerContainer.getMappedPort(AMQPS_PORT);
        Sink sink = new Sink(url,"sp_consumer",
                TestKeystoreHelper.sslContext("jks/sp_consumer.p12","jks/truststore.jks"));
        Source source = new Source(url, "fedEx",
                TestKeystoreHelper.sslContext("jks/localhost.p12","jks/truststore.jks"));

        source.start();
        source.send("fishy business");
        Message message = sink.createConsumer().receive(1000);
        System.out.println(message.getBody(String.class));

    }

    @Test
    public void testMessagesCollected() throws NamingException, JMSException {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("localhost");
        String producerPort = producerContainer.getMappedPort(AMQPS_PORT).toString();
        neighbour.setMessageChannelPort(producerPort);

        NeighbourFetcher neighbourFetcher = mock(NeighbourFetcher.class);
        when(neighbourFetcher.listNeighboursToConsumeFrom()).thenReturn(Arrays.asList(neighbour));

        ForwarderProperties properties = new ForwarderProperties();
        properties.setLocalIxnDomainName("localhost");
        properties.setLocalIxnFederationPort(consumerContainer.getMappedPort(AMQPS_PORT).toString());
        ForwardingCreator forwardingCreator = new ForwardingCreator(properties,
                TestKeystoreHelper.sslContext("jks/localhost.p12","jks/truststore.jks"));
        MessageForwarder forwarder = new MessageForwarder(neighbourFetcher,forwardingCreator);
        forwarder.runSchedule();

        Source source = createSource();
        source.start();
        source.send("fishy fishy");
        System.out.println(String.format("HTTP port is %s",producerContainer.getMappedPort(8080).toString()));

        Sink sink = createSink();
        MessageConsumer consumer = sink.createConsumer();
        Message message = consumer.receive(1000);
        assertThat(message).withFailMessage("Expected message is not routed").isNotNull();



    }

}
