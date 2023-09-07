package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.Assertions;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;
import java.util.Set;

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
	public void testQueueExistsOnExisitingQueue() {
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
	public void createExchangeThatAlreadyExistsResultsInException() {
		Exchange exchange = client._createExchange(new Exchange("test-create-exchange"));

		assertThatExceptionOfType(HttpClientErrorException.Conflict.class).isThrownBy(() -> {
			client._createExchange(new Exchange("test-create-exchange"));
		});
		//client.removeExchangeById(exchange.getId());
		Assertions.fail();
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

		client.removeExchange("my-exchange");
		assertThat(client.exchangeExists("my-exchange")).isFalse();
	}

	@Test
	public void removeExchangeBeforeBindings() {
		client._createExchange(new Exchange("hammershark", "headers"));
		client._createQueue(new Queue("babyshark", MAX_TTL_8_DAYS));

		client.addBinding("hammershark", new Binding("hammershark", "babyshark", new Filter("originatingCountry = 'NO'")));
		assertThat(client.getQueueBindKeys("babyshark")).hasSize(1);

		client.removeExchange("hammershark");
		assertThat(client.getQueueBindKeys("babyshark")).hasSize(0);
	}

	@Test
	public void removeQueueBeforeBindings() {
		client._createExchange(new Exchange("hammershark1", "headers"));
		Queue queue = client._createQueue(new Queue("babyshark1", MAX_TTL_8_DAYS));

		client.addBinding("hammershark1", new Binding("hammershark1", "babyshark1", new Filter("originatingCountry = 'NO'")));
		assertThat(client.getQueueBindKeys("babyshark1")).hasSize(1);

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
		assertThat(client.getQueueBindKeys(queue)).contains(exchange);
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
		assertThat(client.getQueueBindKeys(queue)).contains(exchange);
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
		assertThat(client.getQueueBindKeys(queue)).contains(exchange);
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
	public void whatDoesACreateExchangeRequestReturn() throws IOException {
		Exchange exchange = new Exchange("kyrre", "headers");
		exchange = client._createExchange(exchange);
		new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(System.out,exchange);
	}

	@Test
	public void whatDoeACreateQueueRequestReturn() throws IOException {
		Queue queue = new Queue("test-create-queue-response");
		queue = client._createQueue(queue);
		new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(System.out,queue);
	}


	//TODO test no such exchange, no such queue
	@Test
	public void whatDoesAnAddBindingRequestReturn() {
		Queue queue = new Queue("test-bind-result-queue");
		queue = client._createQueue(queue);

		Exchange exchange = new Exchange("test-bind-result-exchange");
		exchange = client._createExchange(exchange);

		boolean created = client.addBinding(exchange.getName(),new Binding("my-test-binding-key",queue.getName(),new Filter("a = 'b'")));
		assertThat(created).isTrue();
		created = client.addBinding(exchange.getName(),new Binding("my-test-binding-key",queue.getName(),new Filter("a = 'b'")));
		assertThat(created).isFalse();
	}

}