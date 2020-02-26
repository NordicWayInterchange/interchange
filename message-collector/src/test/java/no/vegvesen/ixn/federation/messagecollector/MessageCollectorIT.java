package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageCollectorIT extends DockerBaseIT {


    @Rule
    public GenericContainer consumerContainer = getQpidContainer("docker/consumer",
            "jks",
            "my_ca.crt",
            "localhost.crt",
            "localhost.key");

    @Rule
    public GenericContainer producerContainer = getQpidContainer("docker/producer",
            "jks",
            "my_ca.crt",
            "localhost.crt",
            "localhost.key");

    public Sink createSink(Integer containerPort, String queueName, String keyStore) {
        return new Sink("amqps://localhost:" + containerPort,
                queueName,
                TestKeystoreHelper.sslContext(keyStore,"jks/truststore.jks"));
    }

    public Source createSource(Integer containerPort, String queue, String keystore) {
        return new Source("amqps://localhost:" + containerPort,
                queue,
                TestKeystoreHelper.sslContext(keystore,"jks/truststore.jks"));
    }

    @Test
    public void testMessagesCollected() throws NamingException, JMSException {
        Neighbour neighbour = new Neighbour();
        neighbour.setName("localhost");
        String producerPort = producerContainer.getMappedPort(AMQPS_PORT).toString();
        neighbour.setMessageChannelPort(producerPort);

        NeighbourFetcher neighbourFetcher = mock(NeighbourFetcher.class);
        when(neighbourFetcher.listNeighboursToConsumeFrom()).thenReturn(Arrays.asList(neighbour));

        CollectorProperties properties = new CollectorProperties();
        properties.setLocalIxnDomainName("localhost");
        properties.setWritequeue("fedEx");
        properties.setLocalIxnFederationPort(consumerContainer.getMappedPort(AMQPS_PORT).toString());
        CollectorCreator collectorCreator = new CollectorCreator(properties,
                TestKeystoreHelper.sslContext("jks/localhost.p12","jks/truststore.jks"));
        MessageCollector forwarder = new MessageCollector(neighbourFetcher, collectorCreator);
        forwarder.runSchedule();

        Source source = createSource(producerContainer.getMappedPort(AMQPS_PORT), "localhost", "jks/sp_producer.p12");
        source.start();
        source.send("fishy fishy");
        System.out.println(String.format("HTTP port is %s",producerContainer.getMappedPort(8080).toString()));

        Sink sink = createSink(consumerContainer.getMappedPort(AMQPS_PORT), "sp_consumer", "jks/sp_consumer.p12");
        MessageConsumer consumer = sink.createConsumer();
        Message message = consumer.receive(1000);
        assertThat(message).withFailMessage("Expected message is not routed").isNotNull();
    }


}
