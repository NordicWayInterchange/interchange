package no.vegvesen.ixn.federation.qpid;

/*-
 * #%L
 * routing-configurer
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

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = {QuadTreeFilteringIT.Initializer.class})
@Testcontainers
public class QuadTreeFilteringIT extends QpidDockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(QuadTreeFilteringIT.class);

	@Container
	public static final GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	private static String AMQPS_URL;

	static class Initializer  implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			String httpsUrl = "https://localhost:" + qpidContainer.getMappedPort(HTTPS_PORT);
			String httpUrl = "http://localhost:" + qpidContainer.getMappedPort(8080);
			logger.info("server url: " + httpsUrl);
			logger.info("server url: " + httpUrl);
			AMQPS_URL = "amqps://localhost:" + qpidContainer.getMappedPort(AMQPS_PORT);
			TestPropertyValues.of(
					"qpid.rest.api.baseUrl=" + httpsUrl,
					"qpid.rest.api.vhost=localhost"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@Autowired
	private QpidClient qpidClient;

	@BeforeEach
	public void setUp() {
		//It is not normal for a service provider to be administrator - just to avoid setting up InterchangeApp by letting service provider send to nwEx
		List<String> administrators = qpidClient.getGroupMemberNames("administrators");
		if (!administrators.contains("king_gustaf")) {
			qpidClient.addMemberToGroup("king_gustaf", "administrators");
		}
	}

	@Test
	public void matchingFilterAndQuadTreeGetsRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'NO') and (quadTree like '%,abcdefgh%')");
		assertThat(message).isNotNull();
	}

	@Test
	public void matchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'NO') and (quadTree like '%,cdefghij%')");
		assertThat(message).isNull();
	}

	@Test
	public void nonMatchingFilterAndMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'SE') and (quadTree like '%,abcdefgh%')");
		assertThat(message).isNull();
	}

	@Test
	public void nonMatchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'SE') and (quadTree like '%,cdefghij%')");
		assertThat(message).isNull();
	}

	private Message sendReceiveMessageNeighbour(String messageQuadTreeTiles, String selector) throws Exception {
		Subscription subscription = new Subscription(selector, SubscriptionStatus.REQUESTED);
		Neighbour king_gustaf = new Neighbour("king_gustaf", null, new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newLinkedHashSet(subscription)), null);
		qpidClient.createQueue(king_gustaf.getName());
		qpidClient.addReadAccess(king_gustaf.getName(), king_gustaf.getName());
		qpidClient.addBinding(subscription.getSelector(), king_gustaf.getName(), subscription.bindKey(), "nwEx");

		SSLContext sslContext = TestKeystoreHelper.sslContext("jks/king_gustaf.p12", "jks/truststore.jks");

		Sink sink = new Sink(AMQPS_URL, "king_gustaf", sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(AMQPS_URL, "nwEx", sslContext);
		source.start();
		source.send("fisk", "NO", messageQuadTreeTiles);

		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}

	@Test
	public void sendMessageOverlappingQuadAndOriginatingCountry() throws Exception {
		Map<String, String> props = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		props.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		props.put(MessageProperty.QUAD_TREE.getName(), "abcdef");
		DataType datexNoAbcdef = new DataType(props);
		Message recievedMsg = sendReceiveMessageServiceProvider(",abcdefghijklmno,cdefghijklmnop", datexNoAbcdef);
		assertThat(recievedMsg).isNotNull();
	}

	private Message sendReceiveMessageServiceProvider(String messageQuadTreeTiles, DataType subscription) throws Exception {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		king_gustaf.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,subscription));
		qpidClient.createQueue(king_gustaf.getName());
		qpidClient.addReadAccess(king_gustaf.getName(), king_gustaf.getName());
		qpidClient.addBinding(subscription.toSelector(), king_gustaf.getName(), ""+subscription.toSelector().hashCode(), "nwEx");

		SSLContext sslContext = TestKeystoreHelper.sslContext("jks/king_gustaf.p12", "jks/truststore.jks");

		Sink sink = new Sink(AMQPS_URL, "king_gustaf", sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(AMQPS_URL, "nwEx", sslContext);
		source.start();
		source.send("fisk", "NO", messageQuadTreeTiles);

		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}

}
