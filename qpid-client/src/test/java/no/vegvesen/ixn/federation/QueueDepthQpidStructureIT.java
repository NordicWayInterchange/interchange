package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.qpid.*;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

    @Test
    public void maxingOutQueueSizeGets1003ErrorMessage() throws Exception{
        String queueName = "bi-queue";
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
        String selector = MessageValidatingSelectorCreator.makeSelector(capability);

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
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, 6));
        }

        String logsAfter = qpidContainer.getLogs();
        assertThat(logsAfter).contains("EXH-1003");
    }

    @Test
    @Disabled("Need to fix this")
    public void messageIsRoutedToOneQueueIfTheOtherIsFull() throws Exception {
        String queueOne = "queue-one";
        String queueTwo = "queue-two";
        String exchangeName = "test-exchange";

        qpidClient.createHeadersExchange(exchangeName);

        Capability capability = new Capability(
                new DenmApplication(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12", "13"),
                        List.of(5, 6)
                ),
                new Metadata()
        );
        String selector = MessageValidatingSelectorCreator.makeSelector(capability);

        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueOne, new Filter(selector)));
        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueTwo, new Filter(selector)));

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
        String queueName = "bi-queue";
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
        String selector = MessageValidatingSelectorCreator.makeSelector(capability);

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
