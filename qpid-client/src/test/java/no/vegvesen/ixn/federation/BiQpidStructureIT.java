package no.vegvesen.ixn.federation;

import jakarta.jms.*;
import no.vegvesen.ixn.*;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.naming.Context;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class BiQpidStructureIT extends QpidDockerBaseIT {

    private static final Logger logger = LoggerFactory.getLogger(BiQpidStructureIT.class);
    public static final String HOST_NAME = getDockerHost();
    private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(BiQpidStructureIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");


    QpidClient qpidClient;

    @Container
    public QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("bi-qpid")
            ).withLogConsumer(new Slf4jLogConsumer(logger));
    private SSLContext jmsClientContext;

    @BeforeEach
    public void setUp() {
        QpidClientConfig config = new QpidClientConfig(sslClientContext(stores,"routing_configurer"));
        qpidClient = new QpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),config.qpidRestTemplate());
        jmsClientContext = sslClientContext(stores, "king_gustaf");
    }

    @Test
    public void messageGoesThroughWithOkTTL() throws Exception{
        String queueName = "bi-queue";

        String amqpsUrl = qpidContainer.getAmqpsUrl();
        Source source = new Source(amqpsUrl,queueName, jmsClientContext);
        source.start();

        String messageText = "{FISK}";
        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 3000));

        ConnectionCreator connectionCreator = new SimpleConnectionCreator(jmsClientContext);
        try (Connection connection = connectionCreator.createConnection(qpidContainer.getAmqpsUrl())) {
            connection.start();
            try (Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE)) {
                Destination destination = session.createQueue(queueName);
                try (MessageConsumer consumer = session.createConsumer(destination)) {
                    Message receive = consumer.receive(1000);
                    assertThat(receive).isNotNull();
                }
            }


        }
    }

    /*
    Testing message inherits TTL from queue when queue TTL is shorter than message TTL
     */
    @Test
    public void messageInheritsTTLFromQueue() throws Exception{
        String queueName = "bi-queue";

        Source source = new Source(qpidContainer.getAmqpsUrl(),queueName,jmsClientContext);
        source.start();

        String messageText = "{FISK}";
        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 10000));

        Thread.sleep(6000);

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, jmsClientContext)) {
            Optional<Message> receive = Optional.ofNullable(sink.createConsumer().receive(1000));
            assertThat(receive).isNotPresent();
        }
    }

    /*
    Testing message does not inherit TTL from queue when queue TTL is longer than message TTL
     */
    @Test
    public void messageDoesNotInheritTTLFromQueue() throws Exception{
        String queueName = "bi-queue";

        Source source = new Source(qpidContainer.getAmqpsUrl(),queueName,jmsClientContext);
        source.start();

        String messageText = "{FISK}";
        byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
        source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 3000));

        Thread.sleep(4000);

        try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, jmsClientContext)) {
            Optional<Message> receive = Optional.ofNullable(sink.createConsumer().receive(1000));
            assertThat(receive).isNotPresent();
        }
    }

    private JmsMessage createDenmMessage(Source source, byte[] bytemessage, long ttl) throws JMSException {
        return source.createMessageBuilder()
                .bytesMessage(bytemessage)
                .userId("anna")
                .messageType(Constants.DENM)
                .publisherId("NO-123")
                .publicationId("NO-123-pub")
                .originatingCountry("NO")
                .protocolVersion("1.0")
                .quadTreeTiles(",12003,")
                .shardId(1)
                .shardCount(1)
                .causeCode(5)
                .subCauseCode(76)
                .ttl(ttl)
                .build();
    }
}
