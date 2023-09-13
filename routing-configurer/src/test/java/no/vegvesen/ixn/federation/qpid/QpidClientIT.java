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
		Queue queue = client.createQueue("torsk", MAX_TTL_8_DAYS);

		assertThatExceptionOfType(HttpClientErrorException.Conflict.class).isThrownBy(() -> {
			client.createQueue("torsk", MAX_TTL_8_DAYS); //create some queue that already exists
		});
		client.removeQueue(queue);
	}
	@Test
	public void testGetQueue() {
		String name = "test-get-queue-queue";
		client.createQueue(name);
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
		client.createQueue(name);
		assertThat(client.queueExists(name)).isTrue();
		assertThat(client.queueExists("this-queue-does-not-exist")).isFalse();
	}

	@Test
	public void createExchangeThatAlreadyExistsResultsInException() {
		Exchange exchange = client.createHeadersExchange("test-create-exchange");

		assertThatExceptionOfType(HttpClientErrorException.Conflict.class).isThrownBy(() -> {
			client.createHeadersExchange("test-create-exchange");
		});
		client.removeExchange(exchange);

	}

	@Test
	public void testGetExchange() {
		client.createHeadersExchange("test-get-exchange-exchange");
		Exchange result = client.getExchange("test-get-exchange-exchange");
		assertThat(result.getName()).isEqualTo("test-get-exchange-exchange");
		assertThat(result.getId()).isNotNull();
	}

	@Test
	public void testGetNonExistingExchange() {
		assertThat(client.getExchange("this-exchange-does-not-exist")).isNull();
	}

	@Test
	public void testExchangeExists() {
		String name = "test-exchange-exists-exchange";
		client.createHeadersExchange(name);
		assertThat(client.exchangeExists(name)).isTrue();
		assertThat(client.exchangeExists("this-exchange-does-not-exist")).isFalse();
	}


	@Test
	public void testGetGroupMember() {
		String groupMember = "test-get-group-member-member";
		client.addMemberToGroup(groupMember,SERVICE_PROVIDERS_GROUP_NAME);

		GroupMember member = client.getGroupMember(groupMember, SERVICE_PROVIDERS_GROUP_NAME);
		assertThat(member).isNotNull();
		assertThat(member.getName()).isEqualTo(groupMember);
	}


	@Test
	public void testGetGroupMemberNonExistingMember() {
		GroupMember groupMember = client.getGroupMember("this-group-member-does-not-exist", SERVICE_PROVIDERS_GROUP_NAME);
		assertThat(groupMember).isNull();
	}

	@Test
	public void testGetGroupMemberNonExistingGroup() {
		GroupMember groupMember = client.getGroupMember("this-member-does-not-exist", "this-group-does-not-exist");
		assertThat(groupMember).isNull();
	}

	@Test
	public void createAndDeleteServiceProviderFromGroup() {
		String myUser = "my-service-provider";
		GroupMember groupMember = client.addMemberToGroup(myUser, SERVICE_PROVIDERS_GROUP_NAME);
		assertThat(groupMember).isNotNull().extracting(GroupMember::getName).isEqualTo(myUser);
		List<String> myUserNames = client.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);

		assertThat(myUserNames).contains(myUser);

		client.removeMemberFromGroup(groupMember, SERVICE_PROVIDERS_GROUP_NAME);
		groupMember = client.getGroupMember(myUser,SERVICE_PROVIDERS_GROUP_NAME);

		assertThat(groupMember).isNull();
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
		Exchange directExchange = client.createDirectExchange("my-exchange");
		assertThat(client.exchangeExists("my-exchange")).isTrue();

		client.removeExchange(directExchange);
		assertThat(client.exchangeExists("my-exchange")).isFalse();
	}

	@Test
	public void removeExchangeBeforeBindings() {
		client.createHeadersExchange("hammershark");
		client.createQueue("babyshark", MAX_TTL_8_DAYS);

		client.addBinding("hammershark", new Binding("hammershark", "babyshark", new Filter("originatingCountry = 'NO'")));
		Exchange exchange = client.getExchange("hammershark");
		assertThat(exchange.getBindings()).hasSize(1);

		client.removeExchange(exchange);
		assertThat(client.getQueuePublishingLinks("babyshark")).hasSize(0);
	}

	@Test
	public void removeQueueBeforeBindings() {
		client.createHeadersExchange("hammershark1");
		Queue queue = client.createQueue("babyshark1", MAX_TTL_8_DAYS);

		client.addBinding("hammershark1", new Binding("hammershark1", "babyshark1", new Filter("originatingCountry = 'NO'")));
		assertThat(client.getQueuePublishingLinks("babyshark1")).hasSize(1);

		client.removeQueue(queue);
		assertThat(client.queueExists("babyshark1")).isFalse();
	}


	@Test
	public void readExchangesFromQpid() throws IOException {
		client.createHeadersExchange("test-exchange1");
		client.createHeadersExchange("test-exchange2");
		client.createQueue("test-queue", MAX_TTL_8_DAYS);
		client.addBinding("test-exchange1", new Binding("test-exchange1", "test-queue", new Filter("originatingCountry = 'NO'")));
		client.addBinding("test-exchange2", new Binding("test-exchange2", "test-queue", new Filter("originatingCountry = 'NO'")));

		assertThat(client.getAllExchanges()).isNotEmpty();
	}


	@Test
	public void readQueuesFromQpid() throws IOException {
		String queueName = "test-read-queues-from-qpid";
		client.createQueue(queueName, MAX_TTL_8_DAYS);

		List<Queue> result = client.getAllQueues();
		assertThat(result.stream().filter( q -> q.getName().equals(queueName)).count()).isEqualTo(1);
	}

	@Test
	public void localSubscriptionQueueIsBoundToSubscriptionExchange() {
		String queue = "localSubscriptionQueue1";
		String exchange = "subscriptionExchange1";
		String selector = "originatingCountry = 'NO'";

		client.createQueue(queue, MAX_TTL_8_DAYS);
		client.createHeadersExchange(exchange);

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

		client.createQueue(queue, MAX_TTL_8_DAYS);
		client.createHeadersExchange(exchange);

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

		client.createQueue(queue, MAX_TTL_8_DAYS);
		client.createHeadersExchange(exchange);

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

		client.createHeadersExchange(capabilityExchange);
		client.createDirectExchange(deliveryExchange);

		client.addBinding(deliveryExchange, new Binding(deliveryExchange, capabilityExchange, new Filter(selector)));

		QpidDelta delta = client.getQpidDelta();

		assertThat(delta.getDestinationsFromExchangeName(deliveryExchange)).contains(capabilityExchange);
	}


	@Test
	public void whatDoesAnAddBindingRequestReturn() {
		Queue queue = client.createQueue("test-bind-result-queue");

		Exchange exchange = client.createHeadersExchange("test-bind-result-exchange");

		boolean created = client.addBinding(exchange.getName(),new Binding("my-test-binding-key",queue.getName(),new Filter("a = 'b'")));
		assertThat(created).isTrue();
		created = client.addBinding(exchange.getName(),new Binding("my-test-binding-key",queue.getName(),new Filter("a = 'b'")));
		assertThat(created).isFalse();
	}

	@Test
	public void testCreateQueueCheckIfItsDurable() {
		Queue queue = client.createQueue("test-create-queue-is-durable");
		assertThat(queue.getDurable()).isNotNull().isTrue();
	}

	@Test
	public void testCreateExchangeCheckIfItsDurable() {
		Exchange exchange = client.createHeadersExchange("test-create-exchange-is-durable-header");
		assertThat(exchange.isDurable()).isTrue();

	}

}