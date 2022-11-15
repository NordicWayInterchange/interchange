package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.messaging.IxnMessageConsumerCreator;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {InterchangeAppIT.InterchangeInitializer.class})
@Testcontainers
public class InterchangeAppIT extends QpidDockerBaseIT {

    private static Logger logger = LoggerFactory.getLogger(InterchangeAppIT.class);
    private static Path testKeysPath = generateKeys(InterchangeAppIT.class, "my_ca", "localhost", "king_harald");

    private static final String JKS_KING_HARALD_P_12 = "king_harald.p12";
    private static final String TRUSTSTORE_JKS = "truststore.jks";


	static class InterchangeInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    String.format("amqphub.amqp10jms.remote-url=%s",qpidContainer.getAmqpsUrl()),
                    "amqphub.amqp10jms.username=interchange",
                    "amqphub.amqp10jms.password=12345678"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
	private IxnMessageConsumerCreator consumerCreator;

	@Container
    public static final QpidContainer qpidContainer = getQpidTestContainer(
            "qpid",
            testKeysPath,
            "localhost.p12",
            "password",
            "truststore.jks",
			"password",
            "localhost");

	private String getQpidURI() {
		String url = qpidContainer.getAmqpsUrl();
		logger.info("connection string to local message broker {}", url);
		return url;
	}

	@Test
    public void messageIsRoutedViaInterchangeAppWithExpiration() throws Exception {
		consumerCreator.setupConsumer();
	    String sendUrl = getQpidURI();

	    try (Source source = new Source(sendUrl,"onramp", TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS))) {
            source.start();
            logger.debug("Sending message");
            JmsMessage textMessage = source.createMessageBuilder()
                    .textMessage("Yo!")
                    .messageType("DATEX2")
                    .publisherId("SVV")
                    .originatingCountry("NO")
                    .protocolVersion("DATEX2:1.0")
                    .quadTreeTiles("abc")
                    .latitude(10.0)
                    .longitude(63.0)
                    .publicationType("SituationPublication")
                    .build();


            try (Sink sink = new Sink(getQpidURI(), "NO-out", TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS))) {

                logger.debug("Creating consumer");
                MessageConsumer consumer = sink.createConsumer();
                logger.debug("Starting receive");

				long ttl = 2000L;
				source.sendNonPersistentMessage(textMessage, ttl);
				long expectedExpiration = System.currentTimeMillis() + ttl;
				logger.debug("Message sendt with expected expiry {}", expectedExpiration);

                Message message = consumer.receive(1000L);
                assertThat(message).isNotNull();
                assertThat(message.getJMSExpiration()).isNotNull();
                assertThat(message.getJMSExpiration()).isNotEqualTo(0L);
				logger.debug("estimated vs actual expiry: {} {}", expectedExpiration, message.getJMSExpiration());
            }
        }
    }

	@Test
    public void sendReceiveWithoutInterchangeAppKeepsJmsExpiration() throws Exception {
	    String sendUrl = getQpidURI();

	    try (Source source = new Source(sendUrl,"NO-private", TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS))) {
            source.start();
            logger.debug("Sending message");
            //JmsTextMessage textMessage = source.createTextMessage("Yo!");
            System.out.println(MessageProperty.mandatoryDatex2PropertyNames);
            JmsMessage textMessage = source.createMessageBuilder()
                    .textMessage("Yo!")
                    .messageType(Constants.DATEX_2)
                    .publisherId("SVV")
                    .originatingCountry("NO")
                    .protocolVersion("DATEX2:1.0")
                    .quadTreeTiles(",123,")
                    .publicationType("SituationPublication")
                    .build();

            source.sendNonPersistentMessage(textMessage, 9999L);
            long estimateExpiry = System.currentTimeMillis() + 9999L;
            logger.debug("Message sendt");
            try (Sink sink = new Sink(getQpidURI(), "NO-private", TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS))) {
                logger.debug("Creating consumer");
                MessageConsumer consumer = sink.createConsumer();
                logger.debug("Starting receive");
                Message message = consumer.receive(1000L);
                assertThat(message).isNotNull();
                assertThat(message.getJMSExpiration()).isNotNull();
                assertThat(message.getJMSExpiration()).isGreaterThan(0);
                logger.debug("estimated vs actual expiry: {} {}", estimateExpiry, message.getJMSExpiration());
            }
        }
    }


}
