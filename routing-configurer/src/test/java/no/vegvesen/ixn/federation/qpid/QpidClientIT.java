package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static no.vegvesen.ixn.federation.qpid.QpidClient.FEDERATED_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.MAX_TTL_8_DAYS;
import static no.vegvesen.ixn.federation.qpid.QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.SERVICE_PROVIDERS_GROUP_NAME;
import static org.assertj.core.api.Assertions.*;

/**
 * This is a test for some of the managing of Qpid through the HTTP(S) interface. This test uses a different name for the hostname for the qpid container. We use "testhost", but
 * the actual hostname would normally end up as something like "localhost".
 */
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {QpidClientIT.Initializer.class})
@Testcontainers
public class QpidClientIT extends QpidDockerBaseIT {


	@Container
	private static final KeysContainer keyContainer = getKeyContainer(QpidClientIT.class,"my_ca", "testhost", "routing_configurer");

	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", keyContainer.getKeyFolderOnHost(), "testhost.p12", "password", "truststore.jks", "password","localhost")
            .dependsOn(keyContainer);

	private static Logger logger = LoggerFactory.getLogger(QpidClientIT.class);

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			String httpsUrl = qpidContainer.getHttpsUrl();
			logger.info("server url: " + qpidContainer.getHttpUrl());
			TestPropertyValues.of(
					"routing-configurer.baseUrl=" + httpsUrl,
					"routing-configurer.vhost=localhost",
					"test.ssl.trust-store=" + keyContainer.getKeyFolderOnHost().resolve("truststore.jks"),
					"test.ssl.key-store=" +  keyContainer.getKeyFolderOnHost().resolve("routing_configurer.p12")
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@Autowired
	QpidClient client;


	@Test
	public void pingQpid() {
		assertThat(client.ping()).isEqualTo(200);
	}

	@Test
	public void createQueueThatAlreadyExistsResultsInException() {
		Queue queue = client._createQueue(new Queue("torsk", MAX_TTL_8_DAYS));

		assertThatExceptionOfType(HttpClientErrorException.Conflict.class).isThrownBy(() -> {
			client._createQueue(new Queue("torsk", MAX_TTL_8_DAYS)); //create some queue that already exists
		});
		client.removeQueueById(queue.getId());
	}
	@Test
	public void testGetQueue() {
		String name = "test-get-queue-queue";
		Queue queue = new Queue(name);
		client._createQueue(queue);
		Queue result = client.getQueue(name);
		assertThat(result.getName()).isEqualTo(name);
	}
	@Test
	public void testGetNonExistingQueue() {
		String name = "this-queue-does-not-exist";
		assertThat(client.getQueue(name)).isNull();
	}
	@Test
	public void testQueueExists() {
		String name = "test-queue-exist-queue";
		Queue queue = new Queue(name);
		client._createQueue(queue);
		assertThat(client.queueExists(name)).isTrue();
		assertThat(client.queueExists("this-queue-does-not-exist")).isFalse();
	}

	@Test
	public void testRemovingQueueById() {
		String name = "test-removing-queue-queue";
		Queue queue = new Queue(name);
		Queue createdQueue = client._createQueue(queue);
		client.removeQueueById(createdQueue.getId());
	}

	@Test
	public void testRemovingNonExistingQueue() {
		assertThatExceptionOfType(HttpClientErrorException.NotFound.class).isThrownBy(
				() -> client.removeQueueById("-1")
		);
	}

	@Test
	public void testRemoveQueueIfExists() {
		String name = "test-remove-queue-if-exists";
		Queue queue = new Queue(name);
		client._createQueue(queue);

		client.removeQueueIfExists(name);
		assertThat(client.queueExists(name)).isFalse();

		assertThatNoException().isThrownBy(
				() -> client.removeQueueIfExists("this-queue-does-not-exist")
		);
	}

	@Test
	public void createExchangeThatAlreadyExistsResultsInException() {
		Exchange exchange = client._createExchange(new Exchange("test-create-exchange"));

		assertThatExceptionOfType(HttpClientErrorException.Conflict.class).isThrownBy(() -> {
			client._createExchange(new Exchange("test-create-exchange"));
		});
		client.removeExchangeById(exchange.getId());

	}

	@Test
	public void testGetExchange() {
		String name = "test-get-exchange-exchange";
		Exchange exchange = new Exchange(name);
		client._createExchange(exchange);
		Exchange result = client.getExchange(name);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getId()).isNotNull();
	}

	@Test
	public void testGetNonExistingExchange() {
		assertThat(client.getExchange("this-exchange-does-not-exist")).isNull();
	}

	@Test
	public void testExchangeExists() {
		String name = "test-exchange-exists-exchange";
		client._createExchange(new Exchange(name));
		assertThat(client.exchangeExists(name)).isTrue();
		assertThat(client.exchangeExists("this-exchange-does-not-exist")).isFalse();
	}

	@Test
	public void testRemoveExhangeById() {
		String name = "test-remove-exchange-by-id-exchange";
		Exchange exchange = new Exchange(name);
		exchange = client._createExchange(exchange);
		client.removeExchangeById(exchange.getId());
		assertThat(client.exchangeExists(name)).isFalse();
	}

	@Test
	public void removingNonExistingExhangeById() {
		assertThatExceptionOfType(HttpClientErrorException.NotFound.class).isThrownBy(
				() -> client.removeExchangeById("-1")
		);
	}

	@Test
	public void testRemoveExchangeIfExist() {
		String exchangeName = "test-remove-exchange-if-exists-exchange";
		Exchange exchange = new Exchange(exchangeName);
		client._createExchange(exchange);

		client.removeExchangeIfExists(exchangeName);
		assertThat(client.exchangeExists(exchangeName)).isFalse();

		assertThatNoException().isThrownBy(
				() -> client.removeExchangeIfExists("this-exchange-does-not-exist")
		);

	}

	@Test
	public void testCreatingExchangeWithBindingsDirectlyDiscardsTheBindings() {
		Queue queue1 = client._createQueue(new Queue("test-creating-binding-with-exchange-queue-1"));
		Queue queue2 = client._createQueue(new Queue("test-creating-binding-with-exchange-queue-2"));

		Exchange exchange = client._createExchange(
				new Exchange(
						"test-creating-binding-with-exchange-exchange",
						Arrays.asList(
								new Binding("test-bind-key-1", queue1.getName(),new Filter("a = b")),
								new Binding("test-bind-key-2", queue2.getName(),new Filter("a = c"))
						)
				)
		);
		assertThat(exchange.getBindings()).hasSize(0);
	}

	@Test
	public void whatDoesGetGroupMembersActuallyReturn() {
		System.out.println(client.getAllGroups());
	}

	@Test
	public void createAndDeleteServiceProviderFromGroup() {
		String myUser = "my-service-provider";
		client.addMemberToGroup(myUser, SERVICE_PROVIDERS_GROUP_NAME);
		List<String> myUserNames = client.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);

		assertThat(myUserNames).contains(myUser);

		client.removeMemberFromGroup(myUser, SERVICE_PROVIDERS_GROUP_NAME);
		myUserNames = client.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);

		assertThat(myUserNames).doesNotContain(myUser);
	}

	@Test
	public void createAndDeleteAnInterchangeFromGroups() {
		String deleteUser = "carp";
		client.addMemberToGroup(deleteUser, FEDERATED_GROUP_NAME);
		List<String> userNames = client.getGroupMemberNames(FEDERATED_GROUP_NAME);
		assertThat(userNames).contains(deleteUser);

		client.removeMemberFromGroup(deleteUser, FEDERATED_GROUP_NAME);
		userNames = client.getGroupMemberNames(FEDERATED_GROUP_NAME);
		assertThat(userNames).doesNotContain(deleteUser);
	}

	@Test
	public void addRemoteServiceProviderToGroup() {
		String newUser = "service-provider";
		client.addMemberToGroup(newUser, REMOTE_SERVICE_PROVIDERS_GROUP_NAME);

		List<String> userNames = client.getGroupMemberNames(REMOTE_SERVICE_PROVIDERS_GROUP_NAME);

		assertThat(userNames).contains(newUser);
		client.removeMemberFromGroup(newUser,REMOTE_SERVICE_PROVIDERS_GROUP_NAME);
		userNames = client.getGroupMemberNames(REMOTE_SERVICE_PROVIDERS_GROUP_NAME);
		assertThat(userNames).doesNotContain(newUser);
	}

	//TODO what happens if we try to add a member to a non-existing group??
	//TODO what happens if we try to add a member to an existing group twice??

	@Test
	public void readAccessIsAdded() {
		String subscriberName = "king_harald";
		String queueName = "king_harald";

		client.addReadAccess(subscriberName, queueName);

		AclRule queueReadAccessRule = VirtualHostAccessController.createQueueReadAccessRule(subscriberName, queueName);

		VirtualHostAccessController provider = client.getQpidAcl();
		assertThat(provider.containsRule(queueReadAccessRule)).isTrue();

		client.removeReadAccess(subscriberName, queueName);

		provider = client.getQpidAcl();
		assertThat(provider.containsRule(queueReadAccessRule)).isFalse();
	}

	@Test
	public void writeAccessIsAdded() {
		String subscriberName = "catfish";
		String queueName = "catfish";

		client.addWriteAccess(subscriberName, queueName);
		AclRule queueWriteAccessRule = VirtualHostAccessController.createQueueWriteAccessRule(subscriberName, queueName);
		VirtualHostAccessController provider = client.getQpidAcl();
		assertThat(provider.containsRule(queueWriteAccessRule)).isTrue();

		client.removeWriteAccess(subscriberName, queueName);

		provider = client.getQpidAcl();
		assertThat(provider.containsRule(queueWriteAccessRule)).isFalse();
	}

	@Test
	public void testRemovingDirectExchange() {
		client._createExchange(new Exchange("my-exchange", "direct"));
		assertThat(client.exchangeExists("my-exchange")).isTrue();

		client.removeExchangeIfExists("my-exchange");
		assertThat(client.exchangeExists("my-exchange")).isFalse();
	}

	@Test
	public void removeExchangeBeforeBindings() {
		client._createExchange(new Exchange("hammershark", "headers"));
		client._createQueue(new Queue("babyshark", MAX_TTL_8_DAYS));

		client.addBinding("hammershark", new Binding("hammershark", "babyshark", new Filter("originatingCountry = 'NO'")));
		Exchange exchange = client.getExchange("hammershark");
		assertThat(exchange.getBindings()).hasSize(1);

		client.removeExchangeIfExists("hammershark");
		assertThat(client.getQueuePublishingLinks("babyshark")).hasSize(0);
	}

	@Test
	public void removeQueueBeforeBindings() {
		client._createExchange(new Exchange("hammershark1", "headers"));
		Queue queue = client._createQueue(new Queue("babyshark1", MAX_TTL_8_DAYS));

		client.addBinding("hammershark1", new Binding("hammershark1", "babyshark1", new Filter("originatingCountry = 'NO'")));
		assertThat(client.getQueuePublishingLinks("babyshark1")).hasSize(1);

		client.removeQueueById(queue.getId());
		assertThat(client.queueExists("babyshark1")).isFalse();
	}


	@Test
	public void readExchangesFromQpid() throws IOException {
		Exchange exchange1 = new Exchange("test-exchange1", "headers");
		client.createExchange(exchange1);
		Exchange exchange = new Exchange("test-exchange2", "headers");
		client.createExchange(exchange);
		client.createQueue(new Queue("test-queue", MAX_TTL_8_DAYS));
		client.addBinding("test-exchange1", new Binding("test-exchange1", "test-queue", new Filter("originatingCountry = 'NO'")));
		client.addBinding("test-exchange2", new Binding("test-exchange2", "test-queue", new Filter("originatingCountry = 'NO'")));

		assertThat(client.getAllExchanges()).isNotEmpty();
	}


	@Test
	public void readQueuesFromQpid() throws IOException {
		client.createQueue(new Queue("test-queue", MAX_TTL_8_DAYS));

		List<Queue> result = client.getAllQueues();
		assertThat(result).isNotEmpty();
	}

	@Test
	public void localSubscriptionQueueIsBoundToSubscriptionExchange() {
		String queue = "localSubscriptionQueue1";
		String exchange = "subscriptionExchange1";
		String selector = "originatingCountry = 'NO'";

		client.createQueue(new Queue(queue, MAX_TTL_8_DAYS));
		Exchange exchange1 = new Exchange(exchange, "headers");
		client.createExchange(exchange1);

		client.addBinding(exchange, new Binding(exchange, queue, new Filter(selector)));

		QpidDelta delta = client.getQpidDelta();

		assertThat(delta.getDestinationsFromExchangeName(exchange)).contains(queue);
		assertThat(client.getQueuePublishingLinks(queue)).anyMatch( b -> b.getBindingKey().equals(exchange));
	}

	@Test
	public void localSubscriptionQueueIsBoundToCapabilityExchange() {
		String queue = "localSubscriptionQueue2";
		String exchange = "capabilityExchange1";
		String selector = "originatingCountry = 'NO'";

		client.createQueue(new Queue(queue, MAX_TTL_8_DAYS));
		Exchange exchange1 = new Exchange(exchange, "headers");
		client.createExchange(exchange1);

		client.addBinding(exchange, new Binding(exchange, queue, new Filter(selector)));

		QpidDelta delta = client.getQpidDelta();

		assertThat(delta.getDestinationsFromExchangeName(exchange)).contains(queue);
		assertThat(client.getQueuePublishingLinks(queue)).anyMatch(b -> b.getBindingKey().equals(exchange));
	}

	@Test
	public void subscriptionQueueIsBoundToCapabilityExchange() {
		String queue = "subscriptionQueue1";
		String exchange = "capabilityExchange2";
		String selector = "originatingCountry = 'NO'";

		client.createQueue(new Queue(queue, MAX_TTL_8_DAYS));
		Exchange exchange1 = new Exchange(exchange, "headers");
		client.createExchange(exchange1);

		client.addBinding(exchange, new Binding(exchange, queue, new Filter(selector)));

		QpidDelta delta = client.getQpidDelta();

		assertThat(delta.getDestinationsFromExchangeName(exchange)).contains(queue);
		assertThat(client.getQueuePublishingLinks(queue)).anyMatch(b -> b.getBindingKey().equals(exchange));
	}

	@Test
	public void deliveryExchangeIsBoundToCapabilityExchange() {
		String deliveryExchange = "deliveryExchange1";
		String capabilityExchange = "capabilityExchange3";
		String selector = "originatingCountry = 'NO'";

		Exchange exchange = new Exchange(capabilityExchange, "headers");
		client.createExchange(exchange);
		client.createExchange(new Exchange(deliveryExchange, "direct"));

		client.addBinding(deliveryExchange, new Binding(deliveryExchange, capabilityExchange, new Filter(selector)));

		QpidDelta delta = client.getQpidDelta();

		assertThat(delta.getDestinationsFromExchangeName(deliveryExchange)).contains(capabilityExchange);
	}


	@Test
	public void whatDoesAnAddBindingRequestReturn() {
		Queue queue = client._createQueue(new Queue("test-bind-result-queue"));

		Exchange exchange = client._createExchange(new Exchange("test-bind-result-exchange"));

		boolean created = client.addBinding(exchange.getName(),new Binding("my-test-binding-key",queue.getName(),new Filter("a = 'b'")));
		assertThat(created).isTrue();
		created = client.addBinding(exchange.getName(),new Binding("my-test-binding-key",queue.getName(),new Filter("a = 'b'")));
		assertThat(created).isFalse();
	}

}