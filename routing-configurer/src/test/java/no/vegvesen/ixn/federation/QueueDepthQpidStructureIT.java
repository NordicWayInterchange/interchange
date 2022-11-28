package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.DenmCapability;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, RoutingConfigurerProperties.class})
//@ContextConfiguration(initializers = {QueueDepthQpidStructureIT.Initializer.class})
@Testcontainers
@Disabled
public class QueueDepthQpidStructureIT extends QpidDockerBaseIT {

    private static Logger logger = LoggerFactory.getLogger(QueueDepthQpidStructureIT.class);

    private static Path testKeysPath = getFolderPath("target/test-keys" + ServiceProviderRouterIT.class.getSimpleName());

    private static KeysContainer keyContainer = getKeyContainer(testKeysPath, "my_ca", "localhost", "routing_configurer", "king_gustaf");

    //@Autowired
    SSLContext sslContext;

    //@Autowired
    QpidClient qpidClient;

    @Container
    public QpidContainer qpidContainer = getQpidTestContainer("queue-qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password", "localhost")
            .dependsOn(keyContainer);


    /*static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            qpidContainer.followOutput(new Slf4jLogConsumer(logger));
            String httpsUrl = qpidContainer.getHttpsUrl();
            String httpUrl = qpidContainer.getHttpUrl();
            logger.info("server url: " + httpsUrl);
            logger.info("server url: " + httpUrl);
            TestPropertyValues.of(
                    "routing-configurer.baseUrl=" + httpsUrl,
                    "routing-configurer.vhost=localhost",
                    "test.ssl.trust-store=" + testKeysPath.resolve("truststore.jks"),
                    "test.ssl.key-store=" + testKeysPath.resolve("routing_configurer.p12")
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }*/

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void maxingOutQueueSizeGets1003ErrorMessage() throws Exception{
        String queueName = "bi-queue";
        String exchangeName = "my-exchange";

        qpidClient.createTopicExchange(exchangeName);

        DenmCapability capability = new DenmCapability(
                "NO-123",
                "NO",
                "1.0",
                new HashSet<>(Arrays.asList("12","13")),
                new HashSet<>(Arrays.asList("5","6"))
        );
        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String selector = creator.makeSelector(capability);

        qpidClient.bindTopicExchange(selector, exchangeName, queueName);

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText = "{hei1}";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, "5"));
        }

        String logsBefore = qpidContainer.getLogs();
        assertThat(logsBefore).doesNotContain("EXH-1003");

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText = "{hei2}";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, "6"));
        }

        String logsAfter = qpidContainer.getLogs();
        assertThat(logsAfter).contains("EXH-1003");
    }

    @Test
    public void messageIsRoutedToOneQueueIfTheOtherIsFull() throws Exception {
        String queueOne = "queue-one";
        String queueTwo = "queue-two";
        String exchangeName = "test-exchange";

        qpidClient.createTopicExchange(exchangeName);

        DenmCapability capability = new DenmCapability(
                "NO-123",
                "NO",
                "1.0",
                new HashSet<>(Arrays.asList("12","13")),
                new HashSet<>(Arrays.asList("5","6"))
        );
        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String selector = creator.makeSelector(capability);

        qpidClient.bindTopicExchange(selector, exchangeName, queueOne);
        qpidClient.bindTopicExchange(selector, exchangeName, queueTwo);

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText1 = "{hei1}";
            byte[] bytemessage1 = messageText1.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage1, "5"));
        }

        String logsBefore = qpidContainer.getLogs();
        assertThat(logsBefore).doesNotContain("EXH-1003");

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText2 = "{hei2}";
            byte[] bytemessage2 = messageText2.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage2, "6"));
        }

        //assertThat(numMessages.get()).isEqualTo(2);

        String logsAfter = qpidContainer.getLogs();
        assertThat(logsAfter).contains("EXH-1003");

    }

    @Test
    public void unroutableMessageGets1003ErrorMessage() throws Exception{
        String queueName = "bi-queue";
        String exchangeName = "my-exchange";

        qpidClient.createTopicExchange(exchangeName);

        DenmCapability capability = new DenmCapability(
                "NO-123",
                "NO",
                "1.0",
                new HashSet<>(Arrays.asList("12","13")),
                new HashSet<>(Arrays.asList("5","6"))
        );
        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String selector = creator.makeSelector(capability);

        qpidClient.bindTopicExchange(selector, exchangeName, queueName);

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText = "{hei1}";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, "5"));
        }

        String logsBefore = qpidContainer.getLogs();
        assertThat(logsBefore).doesNotContain("EXH-1003");

        try (Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext)) {
            source.start();
            String messageText = "{hei2}";
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(createDenmMessage(source, bytemessage, "7"));
        }

        String logsAfter = qpidContainer.getLogs();
        assertThat(logsAfter).contains("EXH-1003");
    }

    private JmsMessage createDenmMessage(Source source, byte[] bytemessage, String causeCode) throws JMSException {
        return source.createMessageBuilder()
                .bytesMessage(bytemessage)
                .userId("anna")
                .messageType(Constants.DENM)
                .publisherId("NO-123")
                .originatingCountry("NO")
                .protocolVersion("1.0")
                .quadTreeTiles(",12003,")
                .causeCode(causeCode)
                .subCauseCode("76")
                .build();
    }
}
