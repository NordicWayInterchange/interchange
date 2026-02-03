package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.shared.Constants;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.jms.JMSException;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class QueueDepthQpidStructureIT extends QpidDockerBaseIT {


    public static final String HOST_NAME = getDockerHost();
    private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(QueueDepthQpidStructureIT.class),"my_ca", HOST_NAME,"routing_configurer","king_gustaf");

    SSLContext sslContext;

    QpidClient qpidClient;

    @Container
    public QpidContainer qpidContainer = getQpidTestContainer(
            stores,
            HOST_NAME,
            HOST_NAME,
            Path.of("queue-qpid")
            );

    @BeforeEach
    public void setUp() {
        sslContext = sslClientContext(stores,"routing_configurer");
        QpidClientConfig config = new QpidClientConfig(sslContext);
        qpidClient = new QpidClient(qpidContainer.getHttpsUrl(),qpidContainer.getvHostName(),config.qpidRestTemplate());
    }

    /*
        This test documents behavior of a queue with a maximum message queue depth and
        overflow policy REJECT
     */
    @Test
    public void maxingOutQueueSizeWithRejectGets1003ErrorMessage() throws Exception{
        String queueName = "queue-one";
        String exchangeName = "my-exchange";

        qpidClient.createHeadersExchange(exchangeName);

        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueName, null));

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText = "{hei1}";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 5));
        }

        String logsBefore = qpidContainer.getLogs();
        assertThat(logsBefore).doesNotContain("EXH-1003");

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText = "{hei2}";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 6));
        }

        String logsAfter = qpidContainer.getLogs();
        assertThat(logsAfter).contains("EXH-1003");
    }

    /*
        This is a test of what happens if we put a message on an exchange that
        is bound to two queues, and one of the queues is full.
        (queue-one has maximum depth of 1)
        This test documents two things:
            1. The default overflow policy on a queue that has maximumQueueDepthMessages is REJECT,
                This is despite the broker book saying it is NONE
            2. If a message arrives on an exchange that ends up in multiple queues, and one of them
                reaches its max queue depth, the sender is rejected, and the message
                will not end up in either queue

     */
    @Test
    public void messageIsRejectedIfOneTargetQueueIsFull() throws Exception {
        System.out.println(qpidContainer.getHttpUrl());
        String queueOne = "queue-one";
        String queueTwo = "queue-two";
        String exchangeName = "test-exchange";

        qpidClient.createHeadersExchange(exchangeName);

        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueOne, null));
        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueTwo, null));

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText1 = "{hei1}";
            byte[] bytemessage1 = messageText1.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage1, 5));
        }

        String logsBefore = qpidContainer.getLogs();
        assertThat(logsBefore).doesNotContain("EXH-1003");

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText2 = "{hei2}";
            byte[] bytemessage2 = messageText2.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage2, 6));
        }


        String logsAfter = qpidContainer.getLogs();
        assertThat(logsAfter).contains("EXH-1003");

    }

    @Test
    public void unroutableMessageGets1003ErrorMessage() throws Exception{
        String queueName = "bi-denm";
        String exchangeName = "my-exchange";

        qpidClient.createHeadersExchange(exchangeName);

        Capability capability = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "1.0",
                        List.of("12", "13"),
                        List.of(5, 6)
                ),
                new Metadata()
        );
        String selector = MessageValidatingSelectorCreator.makeSelector(capability, null);

        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText = "{hei1}";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 5));
        }

        String logsBefore = qpidContainer.getLogs();
        assertThat(logsBefore).doesNotContain("EXH-1003");

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText = "{hei2}";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 7));
        }

        String logsAfter = qpidContainer.getLogs();
        assertThat(logsAfter).contains("EXH-1003");
    }

    private JmsMessage createDenmMessage(Source source, byte[] bytemessage, Integer causeCode) throws JMSException {
        return source.createMessageBuilder()
                .bytesMessage(bytemessage)
                .userId("anna")
                .messageType(Constants.DENM)
                .publisherId("NO-123")
                .publicationId("pub-1")
                .originatingCountry("NO")
                .protocolVersion("1.0")
                .quadTreeTiles(",12003,")
                .shardId(1)
                .shardCount(1)
                .causeCode(causeCode)
                .subCauseCode(76)
                .build();
    }
}
