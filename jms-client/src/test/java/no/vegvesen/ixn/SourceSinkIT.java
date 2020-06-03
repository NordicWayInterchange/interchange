package no.vegvesen.ixn;

/*-
 * #%L
 * jms-client
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class SourceSinkIT extends QpidDockerBaseIT {

	@SuppressWarnings("rawtypes")
	@Container
	public static final GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");
	private static String URL;
	private static SSLContext KING_HARALD_SSL_CONTEXT;

	@BeforeAll
	public static void setUp() {
		Integer mappedPort = qpidContainer.getMappedPort(5671);
		URL = String.format("amqps://localhost:%s/", mappedPort);
		KING_HARALD_SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/king_harald.p12", "jks/truststore.jks");
	}

	@Test
	public void explicitExpiryIsReceived() throws JMSException, NamingException {
		Source kingHaraldTestQueueSource = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		kingHaraldTestQueueSource.start();
		JmsTextMessage fisk = kingHaraldTestQueueSource.createTextMessage("fisk");
		kingHaraldTestQueueSource.sendTextMessage(fisk, 2000);

		Sink kingHaraldTestQueueSink = new Sink(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		MessageConsumer testQueueConsumer = kingHaraldTestQueueSink.createConsumer();
		Message receive = testQueueConsumer.receive(1000);
		assertThat(receive).isNotNull();
		assertThat(receive.getJMSExpiration()).isNotNull().isGreaterThan(0);
	}

	@Test
	public void expiredMessageIsNotDelivered() throws JMSException, NamingException, InterruptedException {
		Source kingHaraldTestQueueSource = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		kingHaraldTestQueueSource.start();
		JmsTextMessage fisk = kingHaraldTestQueueSource.createTextMessage("fisk");
		kingHaraldTestQueueSource.sendTextMessage(fisk, 200);

		Thread.sleep(1000);

		Sink kingHaraldTestQueueSink = new Sink(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		MessageConsumer testQueueConsumer = kingHaraldTestQueueSink.createConsumer();
		Message receive = testQueueConsumer.receive(1000);
		assertThat(receive).isNull();
	}

	@Test
	public void queueMaxTtlIsRespected() throws JMSException, NamingException, InterruptedException {
		Source kingHaraldTestQueueSource = new Source(URL, "expiry-queue", KING_HARALD_SSL_CONTEXT);
		kingHaraldTestQueueSource.start();
		kingHaraldTestQueueSource.send("fisk"); //send with default expiry (0)

		Thread.sleep(2000); // let the message expire on the queue with queue declaration "maximumMessageTtl": 1000

		Sink kingHaraldTestQueueSink = new Sink(URL, "expiry-queue", KING_HARALD_SSL_CONTEXT);
		MessageConsumer testQueueConsumer = kingHaraldTestQueueSink.createConsumer();
		Message receive = testQueueConsumer.receive(1000);
		assertThat(receive).isNull();
	}

	@Test
	public void sourceCloseIsClosed() throws JMSException, NamingException {
		Source source = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		source.start();
		assertThat(source.isConnected()).isTrue();
		source.close();
		assertThat(source.isConnected()).isFalse();
	}
}
