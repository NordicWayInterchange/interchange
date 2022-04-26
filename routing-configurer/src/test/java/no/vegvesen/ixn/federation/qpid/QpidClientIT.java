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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static no.vegvesen.ixn.federation.qpid.QpidClient.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This is a test for some of the managing of Qpid through the HTTP(S) interface. This test uses a different name for the hostname for the qpid container. We use "testhost", but
 * the actual hostname would normally end up as something like "localhost".
 */
@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {QpidClientIT.Initializer.class})
@Testcontainers
public class QpidClientIT extends QpidDockerBaseIT {


	@Container
	private static KeysContainer keyContainer = getKeyContainer(QpidClientIT.class,"my_ca", "testhost", "routing_configurer");

	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", keyContainer.getKeyFolderOnHost(), "testhost.p12", "password", "truststore.jks", "password","localhost")
            .dependsOn(keyContainer);

	private static Logger logger = LoggerFactory.getLogger(QpidClientIT.class);

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			String httpsUrl = qpidContainer.getHttpsUrl();
			logger.info("server url: " + httpsUrl);
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
		client._createQueue("torsk");

		assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
			client._createQueue("torsk"); //create some queue that already exists
			//
		});
		client.removeQueue("torsk");
	}

	@Test
	public void queueNotCreatedQueueDoesNotExist() {
		assertThat(client.queueExists("mackrel")).isFalse();
	}

	@Test
	public void setupAndTearDownQueue() {
		//Set up a new queue
		client.createQueue("crab");
		assertThat(client.queueExists("crab")).isTrue();

		//Delete the queue
		client.removeQueue("crab");
		assertThat(client.queueExists("crab")).isFalse();
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

	@Test
	public void readAccessIsAdded() {
		String subscriberName = "king_harald";
		String queueName = "king_harald";

		client.addReadAccess(subscriberName, queueName);

		QpidAcl acl = client.getQpidAcl();
		assertThat(acl.containsRule(QpidAcl.createQeueReadAccessRule(subscriberName,queueName))).isTrue();


		client.removeReadAccess(subscriberName, queueName);

		acl = client.getQpidAcl();
		assertThat(acl.containsRule(QpidAcl.createQeueReadAccessRule(subscriberName,queueName))).isFalse();
	}

	@Test
	public void writeAccessIsAdded() {
		String subscriberName = "catfish";
		String queueName = "catfish";

		client.addWriteAccess(subscriberName, queueName);
		QpidAcl acl = client.getQpidAcl();
		assertThat(acl.containsRule(QpidAcl.createQueueWriteAccessRule(subscriberName,queueName))).isTrue();

		client.removeWriteAccess(subscriberName, queueName);

		acl = client.getQpidAcl();
		assertThat(acl.containsRule(QpidAcl.createQueueWriteAccessRule(subscriberName,queueName))).isFalse();
	}

	@Test
	public void testRemovingDirectExchange() {
		client._createDirectExchange("my-exchange");
		assertThat(client.exchangeExists("my-exchange")).isTrue();

		client.removeDirectExchange("my-exchange");
		assertThat(client.exchangeExists("my-exchange")).isFalse();
	}
}