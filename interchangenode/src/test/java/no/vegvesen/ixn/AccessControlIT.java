package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.MessageConsumer;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Verifies access control lists where username comes from the common name (CN) of the user certificate.
 */
@Testcontainers
public class AccessControlIT extends QpidDockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(AccessControlIT.class);
	private static Path testKeysPath = generateKeys(AccessControlIT.class, "my_ca", "localhost", "king_harald", "king_gustaf");

	class AccessControlNegativeTestClass { }
	private static Path testKeysPathOtherCa = generateKeys(AccessControlNegativeTestClass.class, "my_ca", "king_harald");

	// Keystore and trust store files for integration testing.
	private static final String JKS_KING_HARALD_P_12 = "king_harald.p12";
	private static final String JKS_KING_GUSTAF_P_12 = "king_gustaf.p12";
	private static final String JKS_IMPOSTER_KING_HARALD_P_12 = "king_harald.p12";
	private static final String TRUSTSTORE_JKS = "truststore.jks";

	//Queues used in testing
	private static final String SE_OUT = "SE-out";
	private static final String NO_OUT = "NO-out";
	private static final String ONRAMP = "onramp";
	private static final String NW_EX = "nwEx";
	private static final String TEST_OUT = "test-out";

	@SuppressWarnings("rawtypes")
	@Container
	public static final GenericContainer localContainer = getQpidTestContainer("qpid",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");

	private String getQpidURI() {
		String url = "amqps://localhost:" + localContainer.getMappedPort(AMQPS_PORT);
		logger.info("connection string to local message broker {}", url);
		return url;
	}

	@Test
	public void testKingHaraldCanNotConsumeSE_OUT(){
		Sink seOut = new Sink(getQpidURI(), SE_OUT, TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		assertThatExceptionOfType(JMSSecurityException.class).isThrownBy(seOut::start);
	}

	@Test
	public void testKingGustafCanNotConsumeNO_OUT() {
		Sink noOut = new Sink(getQpidURI(), NO_OUT, TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_GUSTAF_P_12, TRUSTSTORE_JKS));
		assertThatExceptionOfType(JMSSecurityException.class).isThrownBy(noOut::start);
	}

	@Test
	public void KingHaraldCanNotConsumeFromOnramp() {
		Sink onramp = new Sink(getQpidURI(), ONRAMP, TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		assertThatExceptionOfType(JMSSecurityException.class).isThrownBy(onramp::start);
	}

	@Test
	public void KingHaraldCanNotSendToNwEx() throws Exception {
		Source nwEx = new Source(getQpidURI(), NW_EX, TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		nwEx.start();
		assertThatExceptionOfType(JMSException.class).isThrownBy(() -> {
			JmsTextMessage message = nwEx.createTextMessage();
			message.setText("Not Allowed");
			message.setStringProperty(MessageProperty.ORIGINATING_COUNTRY.getName(),"SE");
			nwEx.sendTextMessage(message);

		});


	}


	@Test
	public void userWithInvalidCertificateCannotConnect() {
		Sink testOut = new Sink(getQpidURI(), TEST_OUT, TestKeystoreHelper.sslContext(testKeysPathOtherCa, JKS_IMPOSTER_KING_HARALD_P_12, TRUSTSTORE_JKS));
		assertThatExceptionOfType(JMSException.class).isThrownBy(testOut::start);
	}


	@Test
	public void userWithValidCertificateCanConnect() throws Exception {
		Sink noOut = new Sink(getQpidURI(), NO_OUT, TestKeystoreHelper.sslContext(testKeysPath, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		noOut.start();
		MessageConsumer consumer = noOut.createConsumer();
		assertThat(consumer).isNotNull();
	}
}
