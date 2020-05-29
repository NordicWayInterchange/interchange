package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.DockerBaseIT;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.MessageConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Verifies access control lists where username comes from the common name (CN) of the user certificate.
 */
@Testcontainers
public class AccessControlIT extends DockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(AccessControlIT.class);

	// Keystore and trust store files for integration testing.
	private static final String JKS_KING_HARALD_P_12 = "jks/king_harald.p12";
	private static final String JKS_KING_GUSTAF_P_12 = "jks/king_gustaf.p12";
	private static final String JKS_IMPOSTER_KING_HARALD_P_12 = "jks/imposter_king_harald.p12";
	private static final String TRUSTSTORE_JKS = "jks/truststore.jks";

	//Queues used in testing
	private static final String SE_OUT = "SE-out";
	private static final String NO_OUT = "NO-out";
	private static final String ONRAMP = "onramp";
	private static final String NW_EX = "nwEx";
	private static final String TEST_OUT = "test-out";

	@SuppressWarnings("rawtypes")
	@Container
	public static final GenericContainer localContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	private String getQpidURI() {
		String url = "amqps://localhost:" + localContainer.getMappedPort(AMQPS_PORT);
		logger.info("connection string to local message broker {}", url);
		return url;
	}

	@Test
	public void testKingHaraldCanNotConsumeSE_OUT(){
		Sink seOut = new Sink(getQpidURI(), SE_OUT, TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		assertThatExceptionOfType(JMSSecurityException.class).isThrownBy(seOut::start);
	}

	@Test
	public void testKingGustafCanNotConsumeNO_OUT() throws Exception {
		Sink noOut = new Sink(getQpidURI(), NO_OUT, TestKeystoreHelper.sslContext(JKS_KING_GUSTAF_P_12, TRUSTSTORE_JKS));
		assertThatExceptionOfType(JMSSecurityException.class).isThrownBy(noOut::start);
	}

	@Test
	public void KingHaraldCanNotConsumeFromOnramp() throws Exception {
		Sink onramp = new Sink(getQpidURI(), ONRAMP, TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		assertThatExceptionOfType(JMSSecurityException.class).isThrownBy(onramp::start);
	}

	@Test
	public void KingHaraldCanNotSendToNwEx() throws Exception {
		Source nwEx = new Source(getQpidURI(), NW_EX, TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		nwEx.start();
		assertThatExceptionOfType(JMSException.class).isThrownBy(() -> {nwEx.send("Not allowed"); });
	}

	@Test
	public void userWithInvalidCertificateCannotConnect() throws Exception {
		Sink testOut = new Sink(getQpidURI(), TEST_OUT, TestKeystoreHelper.sslContext(JKS_IMPOSTER_KING_HARALD_P_12, TRUSTSTORE_JKS));
		assertThatExceptionOfType(JMSException.class).isThrownBy(testOut::start);
	}

	@Test
	public void userWithValidCertificateCanConnect() throws Exception {
		Sink noOut = new Sink(getQpidURI(), NO_OUT, TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		noOut.start();
		MessageConsumer consumer = noOut.createConsumer();
		assertThat(consumer).isNotNull();
	}
}
