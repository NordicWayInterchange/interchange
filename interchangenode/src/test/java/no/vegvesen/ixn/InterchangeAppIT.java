package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.DockerBaseIT;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import javax.jms.Message;
import javax.jms.MessageConsumer;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {InterchangeAppIT.InterchangeInitializer.class})
public class InterchangeAppIT extends DockerBaseIT {

    private static Logger logger = LoggerFactory.getLogger(InterchangeAppIT.class);

    private static final String JKS_KING_HARALD_P_12 = "jks/king_harald.p12";
    private static final String TRUSTSTORE_JKS = "jks/truststore.jks";


	static class InterchangeInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    String.format("amqphub.amqp10jms.remote-url=amqp://localhost:%d",qpidContainer.getMappedPort(AMQP_PORT)),
                    "amqphub.amqp10jms.username=interchange",
                    "amqphub.amqp10jms.password=12345678"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    //InterchangeApp consumes messages sent to the queue onramp and is not triggered explicitly from the tests
    @Autowired
    private InterchangeApp interchangeApp;

    @SuppressWarnings("rawtypes")
	@ClassRule
    public static GenericContainer qpidContainer = getQpidContainer(
            "qpid",
            "jks",
            "localhost.crt",
            "localhost.crt",
            "localhost.key"
    );

	private String getQpidURI() {
		String url = "amqps://localhost:" + qpidContainer.getMappedPort(AMQPS_PORT);
		logger.info("connection string to local message broker {}", url);
		return url;
	}

	@Test
    public void messageIsRoutedViaInterchangeAppWithExpiration() throws Exception {
	    String sendUrl = getQpidURI();

	    try (Source source = new Source(sendUrl,"onramp", TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS))) {
            source.start();
            logger.debug("Sending message");
            JmsTextMessage textMessage = source.createTextMessage("Yo!");
            textMessage.setStringProperty("messageType", "DATEX2");
            textMessage.setStringProperty("publisherName","SVV");
            textMessage.setStringProperty("originatingCountry","NO");
            textMessage.setStringProperty("protocolVersion","DATEX2:1.0");
            textMessage.setStringProperty("latitude","10.0");
            textMessage.setStringProperty("longitude","63.0");
            textMessage.setStringProperty("publicationType","SituationPublication");

            try (Sink sink = new Sink(getQpidURI(), "NO-out", TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS))) {

                logger.debug("Creating consumer");
                MessageConsumer consumer = sink.createConsumer();
                logger.debug("Starting receive");

				long ttl = 2000L;
				source.sendTextMessage(textMessage, ttl);
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

	    try (Source source = new Source(sendUrl,"NO-private", TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS))) {
            source.start();
            logger.debug("Sending message");
            JmsTextMessage textMessage = source.createTextMessage("Yo!");
            textMessage.setStringProperty("messageType", "DATEX2");
            textMessage.setStringProperty("publisherName","SVV");
            textMessage.setStringProperty("originatingCountry","NO");
            textMessage.setStringProperty("protocolVersion","DATEX2:1.0");
            textMessage.setStringProperty("latitude","10.0");
            textMessage.setStringProperty("longitude","63.0");
            textMessage.setStringProperty("publicationType","SituationPublication");

            source.sendTextMessage(textMessage, 9999L);
            long estimateExpiry = System.currentTimeMillis() + 9999L;
            logger.debug("Message sendt");
            try (Sink sink = new Sink(getQpidURI(), "NO-private", TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS))) {
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
