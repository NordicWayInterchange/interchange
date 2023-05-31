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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.CollectionType;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.util.List;

import static no.vegvesen.ixn.federation.qpid.QpidClient.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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

		/*
		QpidAcl acl = client.getQpidAcl();
		assertThat(acl.containsRule(QpidAcl.createQeueReadAccessRule(subscriberName,queueName))).isTrue();

		 */

		NewAclRule queueReadAccessRule = VirtualHostAccessControlProvider.createQueueReadAccessRule(subscriberName, queueName);

		VirtualHostAccessControlProvider provider = client.getQpidAcl();
		assertThat(provider.containsRule(queueReadAccessRule)).isTrue();

		client.removeReadAccess(subscriberName, queueName);

		//acl = client.getQpidAcl();
		provider = client.getQpidAcl();

		assertThat(provider.containsRule(queueReadAccessRule)).isFalse();
	}

	@Test
	public void writeAccessIsAdded() {
		String subscriberName = "catfish";
		String queueName = "catfish";

		client.addWriteAccess(subscriberName, queueName);

		NewAclRule queueWriteAccessRule = VirtualHostAccessControlProvider.createQueueWriteAccessRule(subscriberName, queueName);
		VirtualHostAccessControlProvider provider = client.getQpidAcl();
		assertThat(provider.containsRule(queueWriteAccessRule)).isTrue();

		client.removeWriteAccess(subscriberName, queueName);

		provider = client.getQpidAcl();
		assertThat(provider.containsRule(queueWriteAccessRule)).isFalse();
	}

	@Test
	public void testRemovingDirectExchange() {
		client._createDirectExchange("my-exchange");
		assertThat(client.exchangeExists("my-exchange")).isTrue();

		client.removeExchange("my-exchange");
		assertThat(client.exchangeExists("my-exchange")).isFalse();
	}

	@Test
	public void removeExchangeBeforeBindings() {
		client._createTopicExchange("hammershark");
		client._createQueue("babyshark");

		client.bindTopicExchange("originatingCountry = 'NO'", "hammershark", "babyshark");
		assertThat(client.getQueueBindKeys("babyshark")).hasSize(1);

		client.removeExchange("hammershark");
		assertThat(client.getQueueBindKeys("babyshark")).hasSize(0);
	}

	@Test
	public void removeQueueBeforeBindings() {
		client._createTopicExchange("hammershark1");
		client._createQueue("babyshark1");

		client.bindTopicExchange("originatingCountry = 'NO'", "hammershark1", "babyshark1");
		assertThat(client.getQueueBindKeys("babyshark1")).hasSize(1);

		client.removeQueue("babyshark1");
		assertThat(client.queueExists("babyshark1")).isFalse();
	}

	@Test
	public void readBindingFromJson() throws IOException {
		String object = "{\n" +
				"        \"bindingKey\": \"cap-106173e4-f3be-42a1-a50e-264be5ffbe27\",\n" +
				"        \"destination\": \"bi-queue\",\n" +
				"        \"arguments\": {\n" +
				"          \"x-filter-jms-selector\": \"(protocolVersion = 'MAPEM:1.3.1') AND (quadTree like '%,12002031%' OR quadTree like '%,1200201%' OR quadTree like '%,10223310%' OR quadTree like '%,12001022%' OR quadTree like '%,12001020%' OR quadTree like '%,1200013%' OR quadTree like '%,12002300%' OR quadTree like '%,1022303%' OR quadTree like '%,12002302%' OR quadTree like '%,12001200%' OR quadTree like '%,1200212%' OR quadTree like '%,120030012%' OR quadTree like '%,12001202%' OR quadTree like '%,1200210%' OR quadTree like '%,120030010%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,1200023%' OR quadTree like '%,12001032%' OR quadTree like '%,1023211%' OR quadTree like '%,1023213000%' OR quadTree like '%,102302%' OR quadTree like '%,12003002%' OR quadTree like '%,12003000%' OR quadTree like '%,102232%' OR quadTree like '%,12002033%' OR quadTree like '%,102231%' OR quadTree like '%,1022133%' OR quadTree like '%,12001023%' OR quadTree like '%,12003020%' OR quadTree like '%,12001021%' OR quadTree like '%,12002301%' OR quadTree like '%,12002303%' OR quadTree like '%,120012100%' OR quadTree like '%,12001030%' OR quadTree like '%,1200100%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,102322011%' OR quadTree like '%,12001222%' OR quadTree like '%,1200213%' OR quadTree like '%,12001220%' OR quadTree like '%,120003%' OR quadTree like '%,1200211%' OR quadTree like '%,12001012%' OR quadTree like '%,12002310%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,1023212%' OR quadTree like '%,12001010%' OR quadTree like '%,1023210%' OR quadTree like '%,102303%') AND (publisherId = 'NO00005') AND (messageType = 'MAPEM') AND (originatingCountry = 'NO')\"\n" +
				"        },\n" +
				"        \"name\": \"cap-106173e4-f3be-42a1-a50e-264be5ffbe27\",\n" +
				"        \"type\": \"binding\"\n" +
				"      }";

		ObjectMapper mapper = new ObjectMapper();
		Binding result = mapper.readValue(object, Binding.class);
		assertThat(result.getName()).isEqualTo("cap-106173e4-f3be-42a1-a50e-264be5ffbe27");
	}

	@Test
	public void readExchangeFromJson() throws IOException {
		String object = "{\n" +
				"    \"id\": \"f2242367-1ba7-4bfc-8122-c55cc729cdb2\",\n" +
				"    \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"    \"type\": \"headers\",\n" +
				"    \"desiredState\": \"ACTIVE\",\n" +
				"    \"state\": \"ACTIVE\",\n" +
				"    \"durable\": true,\n" +
				"    \"lifetimePolicy\": \"PERMANENT\",\n" +
				"    \"bindings\": [\n" +
				"      {\n" +
				"        \"bindingKey\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"        \"destination\": \"bi-queue\",\n" +
				"        \"arguments\": {\n" +
				"          \"x-filter-jms-selector\": \"(quadTree like '%,12002031%' OR quadTree like '%,1200201%' OR quadTree like '%,10223310%' OR quadTree like '%,12001022%' OR quadTree like '%,12001020%' OR quadTree like '%,1200013%' OR quadTree like '%,12002300%' OR quadTree like '%,1022303%' OR quadTree like '%,12002302%' OR quadTree like '%,12001200%' OR quadTree like '%,1200212%' OR quadTree like '%,120030012%' OR quadTree like '%,12001202%' OR quadTree like '%,1200210%' OR quadTree like '%,120030010%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,1200023%' OR quadTree like '%,12001032%' OR quadTree like '%,1023211%' OR quadTree like '%,1023213000%' OR quadTree like '%,102302%' OR quadTree like '%,12003002%' OR quadTree like '%,12003000%' OR quadTree like '%,102232%' OR quadTree like '%,12002033%' OR quadTree like '%,102231%' OR quadTree like '%,1022133%' OR quadTree like '%,12001023%' OR quadTree like '%,12003020%' OR quadTree like '%,12001021%' OR quadTree like '%,12002301%' OR quadTree like '%,12002303%' OR quadTree like '%,120012100%' OR quadTree like '%,12001030%' OR quadTree like '%,1200100%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,102322011%' OR quadTree like '%,12001222%' OR quadTree like '%,1200213%' OR quadTree like '%,12001220%' OR quadTree like '%,120003%' OR quadTree like '%,1200211%' OR quadTree like '%,12001012%' OR quadTree like '%,12002310%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,1023212%' OR quadTree like '%,12001010%' OR quadTree like '%,1023210%' OR quadTree like '%,102303%') AND (publisherId = 'NO00000') AND (messageType = 'DENM') AND (originatingCountry = 'NO') AND (causeCode = '6' OR causeCode = '8' OR causeCode = '2' OR causeCode = '4' OR causeCode = '18' OR causeCode = '12' OR causeCode = '22' OR causeCode = '10' OR causeCode = '20' OR causeCode = '16' OR causeCode = '14' OR causeCode = '24' OR causeCode = '7' OR causeCode = '9' OR causeCode = '3' OR causeCode = '5' OR causeCode = '19' OR causeCode = '1' OR causeCode = '17' OR causeCode = '23' OR causeCode = '11' OR causeCode = '21' OR causeCode = '15' OR causeCode = '25' OR causeCode = '13') AND (protocolVersion = 'DENM:1.2.1')\"\n" +
				"        },\n" +
				"        \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"        \"type\": \"binding\"\n" +
				"      },\n" +
				"      {\n" +
				"        \"bindingKey\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"        \"destination\": \"sub-0bce6c1d-7867-456e-9b2b-4cc91b13f0e7\",\n" +
				"        \"arguments\": {\n" +
				"          \"x-filter-jms-selector\": \"(messageType = 'DENM' AND originatingCountry = 'NO' AND protocolVersion = 'DENM:1.2.1' AND publisherId = 'NO00000') AND (publicationId = 'NO00000:d8641a5c') AND (quadTree like '%,1022133%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,12003020%' OR quadTree like '%,120030010%' OR quadTree like '%,120012100%' OR quadTree like '%,120030012%' OR quadTree like '%,1023213000%' OR quadTree like '%,1022303%' OR quadTree like '%,12001030%' OR quadTree like '%,12001032%' OR quadTree like '%,1200201%' OR quadTree like '%,12002310%' OR quadTree like '%,12001023%' OR quadTree like '%,12001220%' OR quadTree like '%,12001022%' OR quadTree like '%,12003002%' OR quadTree like '%,12001222%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,12003000%' OR quadTree like '%,12002031%' OR quadTree like '%,12002033%' OR quadTree like '%,12001021%' OR quadTree like '%,12001020%' OR quadTree like '%,1200212%' OR quadTree like '%,1200213%' OR quadTree like '%,1200210%' OR quadTree like '%,1200211%' OR quadTree like '%,1200013%' OR quadTree like '%,12002303%' OR quadTree like '%,120003%' OR quadTree like '%,12002302%' OR quadTree like '%,12001012%' OR quadTree like '%,12002301%' OR quadTree like '%,12002300%' OR quadTree like '%,102322011%' OR quadTree like '%,1023211%' OR quadTree like '%,1023212%' OR quadTree like '%,1023210%' OR quadTree like '%,102231%' OR quadTree like '%,102232%' OR quadTree like '%,12001010%' OR quadTree like '%,1200100%' OR quadTree like '%,1200023%' OR quadTree like '%,12001202%' OR quadTree like '%,10223310%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,12001200%' OR quadTree like '%,102303%' OR quadTree like '%,102302%')\"\n" +
				"        },\n" +
				"        \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"        \"type\": \"binding\"\n" +
				"      }\n" +
				"    ],\n" +
				"    \"durableBindings\": [\n" +
				"      {\n" +
				"        \"bindingKey\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"        \"destination\": \"bi-queue\",\n" +
				"        \"arguments\": {\n" +
				"          \"x-filter-jms-selector\": \"(quadTree like '%,12002031%' OR quadTree like '%,1200201%' OR quadTree like '%,10223310%' OR quadTree like '%,12001022%' OR quadTree like '%,12001020%' OR quadTree like '%,1200013%' OR quadTree like '%,12002300%' OR quadTree like '%,1022303%' OR quadTree like '%,12002302%' OR quadTree like '%,12001200%' OR quadTree like '%,1200212%' OR quadTree like '%,120030012%' OR quadTree like '%,12001202%' OR quadTree like '%,1200210%' OR quadTree like '%,120030010%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,1200023%' OR quadTree like '%,12001032%' OR quadTree like '%,1023211%' OR quadTree like '%,1023213000%' OR quadTree like '%,102302%' OR quadTree like '%,12003002%' OR quadTree like '%,12003000%' OR quadTree like '%,102232%' OR quadTree like '%,12002033%' OR quadTree like '%,102231%' OR quadTree like '%,1022133%' OR quadTree like '%,12001023%' OR quadTree like '%,12003020%' OR quadTree like '%,12001021%' OR quadTree like '%,12002301%' OR quadTree like '%,12002303%' OR quadTree like '%,120012100%' OR quadTree like '%,12001030%' OR quadTree like '%,1200100%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,102322011%' OR quadTree like '%,12001222%' OR quadTree like '%,1200213%' OR quadTree like '%,12001220%' OR quadTree like '%,120003%' OR quadTree like '%,1200211%' OR quadTree like '%,12001012%' OR quadTree like '%,12002310%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,1023212%' OR quadTree like '%,12001010%' OR quadTree like '%,1023210%' OR quadTree like '%,102303%') AND (publisherId = 'NO00000') AND (messageType = 'DENM') AND (originatingCountry = 'NO') AND (causeCode = '6' OR causeCode = '8' OR causeCode = '2' OR causeCode = '4' OR causeCode = '18' OR causeCode = '12' OR causeCode = '22' OR causeCode = '10' OR causeCode = '20' OR causeCode = '16' OR causeCode = '14' OR causeCode = '24' OR causeCode = '7' OR causeCode = '9' OR causeCode = '3' OR causeCode = '5' OR causeCode = '19' OR causeCode = '1' OR causeCode = '17' OR causeCode = '23' OR causeCode = '11' OR causeCode = '21' OR causeCode = '15' OR causeCode = '25' OR causeCode = '13') AND (protocolVersion = 'DENM:1.2.1')\"\n" +
				"        },\n" +
				"        \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"        \"type\": \"binding\"\n" +
				"      },\n" +
				"      {\n" +
				"        \"bindingKey\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"        \"destination\": \"sub-0bce6c1d-7867-456e-9b2b-4cc91b13f0e7\",\n" +
				"        \"arguments\": {\n" +
				"          \"x-filter-jms-selector\": \"(messageType = 'DENM' AND originatingCountry = 'NO' AND protocolVersion = 'DENM:1.2.1' AND publisherId = 'NO00000') AND (publicationId = 'NO00000:d8641a5c') AND (quadTree like '%,1022133%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,12003020%' OR quadTree like '%,120030010%' OR quadTree like '%,120012100%' OR quadTree like '%,120030012%' OR quadTree like '%,1023213000%' OR quadTree like '%,1022303%' OR quadTree like '%,12001030%' OR quadTree like '%,12001032%' OR quadTree like '%,1200201%' OR quadTree like '%,12002310%' OR quadTree like '%,12001023%' OR quadTree like '%,12001220%' OR quadTree like '%,12001022%' OR quadTree like '%,12003002%' OR quadTree like '%,12001222%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,12003000%' OR quadTree like '%,12002031%' OR quadTree like '%,12002033%' OR quadTree like '%,12001021%' OR quadTree like '%,12001020%' OR quadTree like '%,1200212%' OR quadTree like '%,1200213%' OR quadTree like '%,1200210%' OR quadTree like '%,1200211%' OR quadTree like '%,1200013%' OR quadTree like '%,12002303%' OR quadTree like '%,120003%' OR quadTree like '%,12002302%' OR quadTree like '%,12001012%' OR quadTree like '%,12002301%' OR quadTree like '%,12002300%' OR quadTree like '%,102322011%' OR quadTree like '%,1023211%' OR quadTree like '%,1023212%' OR quadTree like '%,1023210%' OR quadTree like '%,102231%' OR quadTree like '%,102232%' OR quadTree like '%,12001010%' OR quadTree like '%,1200100%' OR quadTree like '%,1200023%' OR quadTree like '%,12001202%' OR quadTree like '%,10223310%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,12001200%' OR quadTree like '%,102303%' OR quadTree like '%,102302%')\"\n" +
				"        },\n" +
				"        \"name\": \"cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab\",\n" +
				"        \"type\": \"binding\"\n" +
				"      }\n" +
				"    ],\n" +
				"    \"lastOpenedTime\": 1683778082266,\n" +
				"    \"unroutableMessageBehaviour\": \"DISCARD\",\n" +
				"    \"lastUpdatedBy\": \"bouvet.pilotinterchange.eu\",\n" +
				"    \"lastUpdatedTime\": 1683792055258,\n" +
				"    \"createdBy\": \"bouvet.pilotinterchange.eu\",\n" +
				"    \"createdTime\": 1683026850137,\n" +
				"    \"statistics\": {\n" +
				"      \"bindingCount\": 2,\n" +
				"      \"bytesDropped\": 0,\n" +
				"      \"bytesIn\": 0,\n" +
				"      \"messagesDropped\": 0,\n" +
				"      \"messagesIn\": 0\n" +
				"    }\n" +
				"  }";

		ObjectMapper mapper = new ObjectMapper();
		Exchange result = mapper.readValue(object, Exchange.class);
		assertThat(result.getName()).isEqualTo("cap-20ef68cf-6405-4b86-bde8-63ab8599d8ab");
		assertThat(result.getBindings().size()).isEqualTo(2);
	}

	@Test
	public void readExchangesFromJsonFile() throws IOException {
		File file = new File("src/test/resources/exchanges.json");
		ObjectMapper mapper = new ObjectMapper();
		TypeFactory typeFactory = mapper.getTypeFactory();

		CollectionType collectionType = typeFactory.constructCollectionType(
				List.class, Exchange.class);

		List<Exchange> result = mapper.readValue(file, collectionType);
		assertThat(result.size()).isEqualTo(4);
	}
}