package no.vegvesen.ixn.federation;

import jakarta.jms.JMSException;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.*;
import no.vegvesen.ixn.federation.routing.ServiceProviderRouter;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import no.vegvesen.ixn.federation.service.routing.localsubscription.LocalSubscriptionService;
import no.vegvesen.ixn.federation.ssl.TestSSLContextConfig;
import no.vegvesen.ixn.federation.service.routing.localdelivery.LocalDeliveryService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@SpringBootTest(classes = {
		ServiceProviderRouter.class,
		QpidClient.class,
		QpidClientConfig.class,
		InterchangeNodeProperties.class,
		RoutingConfigurerProperties.class,
		LocalDeliveryService.class,
		LocalSubscriptionService.class,
		TestSSLContextConfig.class,
		TestSSLProperties.class,
		OutgoingMatchDiscoveryService.class
})
public class ServiceProviderRouterIT extends QpidDockerBaseIT {

	private static final Logger logger = LoggerFactory.getLogger(ServiceProviderRouterIT.class);

	public static final String HOST_NAME = getDockerHost();

	private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(ServiceProviderRouterIT.class),"my_ca", HOST_NAME, "routing_configurer", "king_gustaf");

    public static final QpidContainer qpidContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Path.of("qpid")
			);

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		qpidContainer.followOutput(new Slf4jLogConsumer(logger));
		registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
		registry.add("routing-configurer.vhost", () -> "localhost");
		registry.add("test.ssl.trust-store", () -> getTrustStorePath(stores));
		registry.add("test.ssl.key-store", () -> getClientStorePath("routing_configurer", stores.clientStores()));
		registry.add("interchange.node-provider.name", () -> HOST_NAME);
	}

	@BeforeAll
	static void setup(){
		qpidContainer.start();
	}

	@MockitoBean
	ServiceProviderRepository serviceProviderRepository;

	@MockitoBean
	PrivateChannelRepository privateChannelRepository;

	@Autowired
	QpidClient client;

 	@Autowired
	ServiceProviderRouter router;

	@MockitoBean
	MatchRepository matchRepository;

	@MockitoBean
	ListenerEndpointRepository listenerEndpointRepository;

	@MockitoBean
	OutgoingMatchRepository outgoingMatchRepository;


	@Test
	public void setUpQueueForPrivateChannels(){
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange", 5671, "queueName");
		PrivateChannel privateChannel = new PrivateChannel(Collections.singleton(new Peer("peer")), PrivateChannelStatus.REQUESTED, "my-channel", endpoint,"service-provider");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(client.queueExists(privateChannel.getEndpoint().getQueueName())).isTrue();
		assertThat(client.getPrivateChannelGroupMember(serviceProvider.getName())).isNotNull();
		for (Peer peer : privateChannel.getPeers()) {
			assertThat(client.getPrivateChannelGroupMember(peer.getName())).isNotNull();
		}

		verify(privateChannelRepository, times(1)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(1)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void tearDownQueueForPrivateChannels(){
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange", 5671, "queueName");
		PrivateChannel privateChannel = new PrivateChannel(Collections.singleton(new Peer("peer")), PrivateChannelStatus.REQUESTED, "my-channel", endpoint,"service-provider");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		privateChannel.setStatus(PrivateChannelStatus.TEAR_DOWN);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(client.queueExists(privateChannel.getEndpoint().getQueueName())).isFalse();
		assertThat(client.getPrivateChannelGroupMember(serviceProvider.getName())).isNull();
		for (Peer peer : privateChannel.getPeers()) {
			assertThat(client.getPrivateChannelGroupMember(peer.getName())).isNull();
		}

		verify(privateChannelRepository, times(2)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyAreServiceProviderInAnotherChannel() {
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange", 5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-2");

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer("peer-1")), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1,"service-provider");
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer("peer-2")), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "service-provider");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel1, privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(List.of(privateChannel2));
		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Collections.emptyList(),Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(),any())).thenReturn(1L,0L);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(client.getPrivateChannelGroupMember(serviceProvider.getName())).isNotNull();

		verify(privateChannelRepository, times(2)).findAllByPeerNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).countByServiceProviderNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyArePeerInAnotherChannel(){
		ServiceProvider serviceProvider1 = new ServiceProvider("service-provider-1");
		ServiceProvider serviceProvider2 = new ServiceProvider("service-provider-2");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-2");

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer("peer-1")), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1,"service-provider-1");
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer("service-provider-1")), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "service-provider-2");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel1));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider2.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel1));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());
		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Collections.singletonList(privateChannel2), Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(),any())).thenReturn(0L,0L);

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());
		assertThat(client.getPrivateChannelGroupMember(serviceProvider1.getName())).isNotNull();

		verify(privateChannelRepository, times(2)).findAllByPeerNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).countByServiceProviderNameAndStatus(any(),any());
		verify(privateChannelRepository, times(3)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(3)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void doNotRemovePeerFromGroupWhenTheyAreServiceProviderInAnotherChannel(){
		ServiceProvider serviceProvider1 = new ServiceProvider("service-1");
		ServiceProvider serviceProvider2 = new ServiceProvider("service-2");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-2");

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer("service-2")), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, "service-1");
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer("service-1")), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "service-2");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel1));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());
		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider2.getName())).thenReturn(Collections.emptyList());
		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);

		privateChannel2.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Collections.singletonList(privateChannel1), Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(), any())).thenReturn(0L, 1L);

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		assertThat(client.getPrivateChannelGroupMember(serviceProvider1.getName())).isNotNull();
		assertThat(client.getPrivateChannelGroupMember(serviceProvider2.getName())).isNotNull();

		verify(privateChannelRepository, times(2)).findAllByPeerNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).countByServiceProviderNameAndStatus(any(),any());
		verify(privateChannelRepository, times(3)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(3)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void doNotRemovePeerFromGroupWhenTheyArePeerInAnotherChannel(){
		ServiceProvider serviceProvider1 = new ServiceProvider("service-1");
		ServiceProvider serviceProvider2 = new ServiceProvider("service-2");

		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-2");
		PrivateChannelEndpoint endpoint3 = new PrivateChannelEndpoint("my-interchange", 5671, "queueName-3");

		PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer("service-2")),PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, "service-1");
		PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer("service-2")), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "service-1");
		PrivateChannel privateChannel3 = new PrivateChannel(Collections.singleton(new Peer("service-1")), PrivateChannelStatus.REQUESTED, "my-channel-3", endpoint3, "service-2");

		when(privateChannelRepository.findAllByServiceProviderName(serviceProvider1.getName())).thenReturn(List.of(privateChannel1, privateChannel2));
		when(privateChannelRepository.findAllByServiceProviderName(serviceProvider2.getName())).thenReturn(List.of(privateChannel3));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		privateChannel1.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByPeerNameAndStatus(any(),any())).thenReturn(Collections.emptyList(),Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(),any())).thenReturn(1L,1L);
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(List.of(privateChannel2));

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());
		assertThat(client.getPrivateChannelGroupMember(serviceProvider2.getName())).isNotNull();

		verify(privateChannelRepository, times(2)).findAllByPeerNameAndStatus(any(),any());
		verify(privateChannelRepository, times(2)).countByServiceProviderNameAndStatus(any(),any());
		verify(privateChannelRepository, times(3)).findAllByServiceProviderName(any());
		verify(privateChannelRepository, times(3)).findAllByStatusAndServiceProviderName(any(), any());
	}

	@Test
	public void addPeerToPrivateChannelAfterCreation() {
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange",5671,"queueName");
		Peer peer = new Peer("peer");
		PrivateChannel privateChannel = new PrivateChannel(new HashSet<>(Arrays.asList(peer)), PrivateChannelStatus.REQUESTED, "my-channel", endpoint, "my-service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Collections.singletonList(privateChannel));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember("peer")).isNotNull();

		Peer newPeer = new Peer("new-peer");
		privateChannel.addPeer(newPeer);

		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.singletonList(privateChannel));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(newPeer.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember("new-peer")).isNotNull();

		verify(privateChannelRepository, times(2)).save(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
		verify(privateChannelRepository, times(0)).findAllByPeerNameAndStatus(any(), any());
		verify(privateChannelRepository, times(0)).countByServiceProviderNameAndStatus(any(), any());
	}

	@Test
	public void removePeerToPrivateChannelAfterCreation() {
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange",5671,"queueName");
		Peer peer1 = new Peer("peer-1");
		Peer peer2 = new Peer("peer-2");
		PrivateChannel privateChannel = new PrivateChannel(new HashSet<>(Arrays.asList(peer1, peer2)), PrivateChannelStatus.REQUESTED, "my-channel", endpoint, "my-service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Collections.singletonList(privateChannel));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember("peer-1")).isNotNull();
		assertThat(client.getPrivateChannelGroupMember("peer-2")).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.singletonList(privateChannel));
		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Collections.singletonList(privateChannel));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(privateChannel.getPeers()).hasSize(1);
		assertThat(client.getPrivateChannelGroupMember("peer-2")).isNull();

		verify(privateChannelRepository, times(2)).save(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
		verify(privateChannelRepository, times(1)).findAllByPeerNameAndStatus(any(), any());
		verify(privateChannelRepository, times(1)).countByServiceProviderNameAndStatus(any(), any());
	}

	@Test
	public void removePeerToPrivateChannelAfterCreationWhenPeerInMultipleChannels() {
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-2");
		Peer peer1 = new Peer("peer");
		Peer peer2 = new Peer("peer");

		PrivateChannel privateChannel1 = new PrivateChannel(new HashSet<>(Arrays.asList(peer1)), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, "my-service-provider");
		PrivateChannel privateChannel2 = new PrivateChannel(new HashSet<>(Arrays.asList(peer2)), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "my-service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel1, privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember("peer")).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider.getName())).thenReturn(Arrays.asList(privateChannel1, privateChannel2));
		when(privateChannelRepository.findAllByPeerNameAndStatus(any(), any())).thenReturn(Arrays.asList(privateChannel1, privateChannel2));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel1.getPeers()).hasSize(1);
		assertThat(privateChannel2.getPeers()).hasSize(0);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember("peer")).isNotNull();

		verify(privateChannelRepository, times(3)).save(any());
		verify(privateChannelRepository, times(2)).findAllByStatusAndServiceProviderName(any(), any());
		verify(privateChannelRepository, times(1)).findAllByPeerNameAndStatus(any(), any());
		verify(privateChannelRepository, times(1)).countByServiceProviderNameAndStatus(any(), any());
	}

	@Test
	public void removePeerToPrivateChannelAfterCreationWhenServiceProviderInOtherChannel() {
		ServiceProvider serviceProvider1 = new ServiceProvider("my-service-provider-1");
		ServiceProvider serviceProvider2 = new ServiceProvider("my-service-provider-2");
		PrivateChannelEndpoint endpoint1 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-1");
		PrivateChannelEndpoint endpoint2 = new PrivateChannelEndpoint("my-interchange",5671,"queueName-2");
		Peer peer1 = new Peer("peer");
		Peer peer2 = new Peer("my-service-provider-1");

		PrivateChannel privateChannel1 = new PrivateChannel(new HashSet<>(Arrays.asList(peer1)), PrivateChannelStatus.REQUESTED, "my-channel-1", endpoint1, "my-service-provider-1");
		PrivateChannel privateChannel2 = new PrivateChannel(new HashSet<>(Arrays.asList(peer2)), PrivateChannelStatus.REQUESTED, "my-channel-2", endpoint2, "my-service-provider-2");

		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel1));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider1.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider1, client.getQpidDelta());

		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer1.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember("my-service-provider-1")).isNotNull();
		assertThat(client.getPrivateChannelGroupMember("peer")).isNotNull();


		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(Arrays.asList(privateChannel2));
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, serviceProvider2.getName())).thenReturn(Collections.emptyList());

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());

		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(peer2.getStatus()).isEqualTo(PeerStatus.CREATED);
		assertThat(client.getPrivateChannelGroupMember("my-service-provider-2")).isNotNull();

		peer2.setStatus(PeerStatus.TEAR_DOWN);

		when(privateChannelRepository.countByServiceProviderNameAndStatus(any(), any())).thenReturn(1L);

		router.syncPrivateChannels(serviceProvider2, client.getQpidDelta());
		assertThat(privateChannel1.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(privateChannel2.getPeers()).hasSize(0);
		assertThat(client.getPrivateChannelGroupMember("my-service-provider-1")).isNotNull();

		verify(privateChannelRepository, times(3)).save(any());
		verify(privateChannelRepository, times(3)).findAllByStatusAndServiceProviderName(any(), any());
		verify(privateChannelRepository, times(1)).findAllByPeerNameAndStatus(any(), any());
		verify(privateChannelRepository, times(1)).countByServiceProviderNameAndStatus(any(), any());
	}


	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException, JMSException {
		String source = "king_gustaf_source";
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.REQUESTED,
				"messageType = 'DATEX2'",
				HOST_NAME,
				Collections.emptySet(),
				Collections.singleton(new LocalEndpoint(
								source,
								qpidContainer.getHost(),
								qpidContainer.getAmqpsPort()
						)
				)
		);

		Capability capability = new Capability(
				new DatexApplication(
						"NO-123",
						"pub-1",
						"NO",
						"1.0",
						List.of("0122"),
						"publicationType",
						"publisherName"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		capability.setStatus(CapabilityStatus.CREATED);
		Capabilities capabilities = new Capabilities(
				Collections.singleton(capability
				)
		);
		String deliverySelector = "messageType = 'DATEX2'";
		LocalDelivery localDelivery = new LocalDelivery(
				UUID.randomUUID().toString(),
				deliverySelector,
				LocalDeliveryStatus.CREATED
		);
		String exchangeName = "myexchange";
		localDelivery.addEndpoint(new LocalDeliveryEndpoint(
				qpidContainer.getHost(),
				qpidContainer.getAmqpsPort(),
				exchangeName
		));
		ServiceProvider king_gustaf = new ServiceProvider(
				"king_gustaf",
				capabilities,
				Set.of(subscription),
				Set.of(localDelivery),
				LocalDateTime.now()
		);

		OutgoingMatch outgoingMatch = new OutgoingMatch(
				localDelivery,
				capability,
				king_gustaf.getName()
		);
		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(outgoingMatch));
		when(serviceProviderRepository.save(any())).thenReturn(king_gustaf);
		router.syncServiceProviders(Arrays.asList(king_gustaf), client.getQpidDelta());

		SSLContext kingGustafSslContext = sslClientContext(stores,"king_gustaf");
		String amqpsUrl = qpidContainer.getAmqpsUrl();

		Set<LocalEndpoint> sinkEndpoints = king_gustaf.getSubscriptions().stream().flatMap(s -> s.getLocalEndpoints().stream()).collect(Collectors.toSet());
		assertThat(sinkEndpoints).hasSize(1);

		LocalEndpoint endpoint = sinkEndpoints.stream().findFirst().get();
		Sink readKingGustafQueue = new Sink(amqpsUrl, endpoint.getSource(), kingGustafSslContext);
		readKingGustafQueue.start();

		Set<LocalDeliveryEndpoint> deliveryEndpoints = king_gustaf.getDeliveries().stream().flatMap(d -> d.getEndpoints().stream()).collect(Collectors.toSet());
		assertThat(deliveryEndpoints).hasSize(1);
		LocalDeliveryEndpoint deliveryEndpoint = deliveryEndpoints.stream().findFirst().get();

		Source writeOnrampQueue = new Source(amqpsUrl, deliveryEndpoint.getTarget(), kingGustafSslContext);
		writeOnrampQueue.start();
		try {
			Sink readDlqueue = new Sink(amqpsUrl, deliveryEndpoint.getTarget(), kingGustafSslContext);
			readDlqueue.start();
			fail("Should not allow king_gustaf to read from queue not granted access on local endpoint");
		} catch (Exception ignore) {
		}
	}

	@Test
	public void subscriberToreDownWillBeRemovedFromSubscribeFederatedInterchangesGroup() {
		String serviceProviderName = "tore-down-service-provider";

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-node");

		ServiceProvider toreDownServiceProvider = new ServiceProvider(
				serviceProviderName,
				Set.of(localSubscription)
		);

		when(serviceProviderRepository.save(any())).thenReturn(toreDownServiceProvider);
		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider), client.getQpidDelta());
		assertThat(client.getServiceProviderMember(toreDownServiceProvider.getName())).isNotNull();
		assertThat(localSubscription.getStatus()).isEqualTo(LocalSubscriptionStatus.CREATED);

		Set<LocalEndpoint> localEndpoints = toreDownServiceProvider.getSubscriptions().stream()
				.flatMap(s -> s.getLocalEndpoints().stream())
				.collect(Collectors.toSet());
		assertThat(localEndpoints).hasSize(1);
		LocalEndpoint endpoint = localEndpoints.stream().findFirst().get();

		assertThat(client.queueExists(endpoint.getSource())).isTrue();

		toreDownServiceProvider.getSubscriptions().forEach(s -> s.setStatus(LocalSubscriptionStatus.TEAR_DOWN));

		when(matchRepository.findAllByLocalSubscriptionId(any(Integer.class))).thenReturn(Collections.emptyList());
		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider), client.getQpidDelta());
		assertThat(toreDownServiceProvider.getSubscriptions()).isEmpty();
		assertThat(client.getServiceProviderMember(toreDownServiceProvider.getName())).isNull();
		assertThat(client.queueExists(endpoint.getSource())).isFalse();
	}

	@Test
	public void serviceProviderShouldBeRemovedWhenCapabilitiesAreRemoved() {
		Capabilities capabilities = new Capabilities(
				Collections.singleton(new Capability(new DatexApplication("NO-123", "NO-pub","NO", "1.0", List.of(), "SituationPublication", "publisherName"), new Metadata(RedirectStatus.OPTIONAL))));
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider",capabilities);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());

		assertThat(client.getServiceProviderMember(serviceProvider.getName())).isNotNull();

		serviceProvider.setCapabilities(new Capabilities());
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getServiceProviderMember(serviceProvider.getName())).isNull();
	}

	@Test
	public void shardedCapabilityGetsEqualNumberOfShardsAsShardCount() {

		DatexApplication application = new DatexApplication("NO-123", "NO-pub","NO", "1.0", Collections.emptyList(), "SituationPublication", "publisherName");
		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Capability cap = new Capability(
				application,
				metadata,
				List.of(
					new CapabilityShard(1, "cap-" + UUID.randomUUID(), MessageValidatingSelectorCreator.makeSelector(new Capability(application, metadata), 1)),
					new CapabilityShard(2, "cap-" + UUID.randomUUID(), MessageValidatingSelectorCreator.makeSelector(new Capability(application, metadata), 2)),
					new CapabilityShard(3, "cap-" + UUID.randomUUID(), MessageValidatingSelectorCreator.makeSelector(new Capability(application, metadata), 3))
				)
		);

		Capabilities capabilities = new Capabilities(
				Collections.singleton(cap));
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider",capabilities);

		router.setUpCapabilityExchanges(serviceProvider, client.getQpidDelta());
		assertThat(cap.getStatus()).isEqualTo(CapabilityStatus.CREATED);
		assertThat(cap.isSharded()).isTrue();
		assertThat(cap.hasShards()).isTrue();
		assertThat(cap.getShards()).hasSize(3);
		for (CapabilityShard shard : cap.getShards()) {
			assertThat(client.exchangeExists(shard.getExchangeName())).isTrue();
			assertThat(shard.getSelector().contains("shardId")).isTrue();
		}
	}

	@Test
	public void nonShardedCapabilityIsSetUp() {

		DatexApplication application = new DatexApplication("NO-123", "NO-pub","NO", "1.0", Collections.emptyList(), "SituationPublication", "publisherName");
		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Capability cap = new Capability(
				application,
				metadata,
				List.of(
					new CapabilityShard(1, "cap-" + UUID.randomUUID(), MessageValidatingSelectorCreator.makeSelector(new Capability(application, metadata), null))
				)
		);

		Capabilities capabilities = new Capabilities(
				Collections.singleton(cap));
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider",capabilities);

		router.setUpCapabilityExchanges(serviceProvider, client.getQpidDelta());
		assertThat(cap.getStatus()).isEqualTo(CapabilityStatus.CREATED);
		assertThat(cap.isSharded()).isFalse();
		assertThat(cap.hasShards()).isTrue();
		assertThat(cap.getShards()).hasSize(1);
		assertThat(client.exchangeExists(cap.getShards().stream().findFirst().get().getExchangeName())).isTrue();
		assertThat(cap.getShards().stream().findFirst().get().getSelector().contains("shardId")).isFalse();
	}

	@Test
	public void doSetUpQueueWhenSubscriptionHasConsumerCommonNameSameAsIxnNameAndServiceProviderName() {
		LocalSubscription sub1 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'NO'",
				"my-node");
		LocalSubscription sub2 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE'",
				"my-service-provider");

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider",Set.of(sub1,sub2));

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getServiceProviderMember(serviceProvider.getName())).isNotNull();

		assertThat(sub1.getLocalEndpoints()).hasSize(1);
		LocalEndpoint endpoint1 = sub1.getLocalEndpoints().stream().findFirst().get();
		assertThat(client.queueExists(endpoint1.getSource())).isTrue();

		assertThat(sub2.getLocalEndpoints()).hasSize(0);
	}

	@Test
	public void serviceProviderShouldBeRemovedFromGroupWhenTheyHaveNoCapabilitiesOrSubscriptions() {
		Capabilities capabilities = new Capabilities(
				Collections.singleton(new Capability(new DatexApplication("NO-123", "NO-pub","NO", "1.0", List.of(), "SituationPublication", "publisherName"), new Metadata(RedirectStatus.OPTIONAL))));
		ServiceProvider serviceProvider = new ServiceProvider("serviceprovider-should-be-removed",capabilities);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getServiceProviderMember(serviceProvider.getName())).isNotNull();

		serviceProvider.setCapabilities(new Capabilities());

		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getServiceProviderMember(serviceProvider.getName())).isNull();
	}




	@Test
	public void tearDownTargetForDeliveryByDeletedDelivery() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		String exchangeName = "my-exchange8";

		CapabilityShard shard = new CapabilityShard(1, "cap-ex4", "publicationId = 'pub-1'");
		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(shard)
		);
		client.createHeadersExchange("cap-ex4");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED, "delivery", false);
		delivery.addEndpoint(new LocalDeliveryEndpoint("my-interchange", 5671, exchangeName));
		delivery.setId(1);
		serviceProvider.addDeliveries(Set.of(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(delivery.getEndpoints()).isEmpty();
	}


	@Test
	public void tearDownDlqForDeliveryByDeletedDelivery() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		String exchangeName = "dlq-exchange";
		String dlqName = "dlq-name";

		CapabilityShard shard = new CapabilityShard(1, "cap-ex40", "publicationId = 'pub-1'");
		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(shard)
		);
		client.createHeadersExchange("cap-ex40");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED, "delivery", false);
		delivery.addEndpoint(new LocalDeliveryEndpoint(
				1,
				"host",
				123,
				exchangeName,
				2,
				3,
				dlqName));

		serviceProvider.addDeliveries(Set.of(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		assertThat(client.queueExists(dlqName)).isFalse();
		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(delivery.getEndpoints()).isEmpty();
	}








	@Test
	public void connectionGetsRemovedWhenCapabilityIsRemoved(){

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint("endpoint-3", "my-interchange", 5671);
		subscription.addLocalEndpoint(endpoint);
		client.createQueue("endpoint-3");

		CapabilityShard shard = new CapabilityShard(1, "cap-ex15", "publicationId = 'pub-1'");
		Capability denmCapability = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(shard)
		);
		client.createHeadersExchange("cap-ex15");

		denmCapability.setStatus(CapabilityStatus.CREATED);

		ServiceProvider mySP = new ServiceProvider("my-sp",Set.of(subscription));
        ServiceProvider otherSP = new ServiceProvider("other-sp", new Capabilities(Collections.singleton(denmCapability)));

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncServiceProviders(Arrays.asList(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(1);

		denmCapability.setStatus(CapabilityStatus.TEAR_DOWN);

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncServiceProviders(Arrays.asList(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(0);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(0);
	}

	@Test
	public void localSubscriptionKeepsConnectionToOneCapabilityAndTearsDownAnother() {

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint("endpoint-4", "my-interchange", 5671);
		subscription.addLocalEndpoint(endpoint);
		client.createQueue("endpoint-4");

		CapabilityShard shard1 = new CapabilityShard(1, "cap-ex16", "publicationId = 'pub-1'");
		Capability denmCapability1 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(shard1)
		);
		client.createHeadersExchange("cap-ex16");

		denmCapability1.setStatus(CapabilityStatus.CREATED);

		CapabilityShard shard2 = new CapabilityShard(1, "cap-ex17", "publicationId = 'pub-2'");
		Capability denmCapability2 = new Capability(
				new DenmApplication(
						"NPRA",
						"pub-2",
						"NO",
						"1.0",
						List.of("1234"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL),
				Collections.singletonList(shard2)
		);
		client.createHeadersExchange("cap-ex17");

		denmCapability2.setStatus(CapabilityStatus.CREATED);

		ServiceProvider mySP = new ServiceProvider("my-sp",Set.of(subscription));
        ServiceProvider otherSP = new ServiceProvider("other-sp", new Capabilities(Set.of(denmCapability1, denmCapability2)));

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncServiceProviders(Arrays.asList(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(2);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(2);

		denmCapability1.setStatus(CapabilityStatus.TEAR_DOWN);

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncServiceProviders(Arrays.asList(mySP, otherSP), client.getQpidDelta());

		assertThat(client.getQueuePublishingLinks(subscription.getLocalEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(subscription.getLocalEndpoints()).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(1);
	}

	@Test
	public void routerPicksUpRequestedLocalSubscription() {
		LocalSubscription localSubscription = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'",
				"a.bouvetinterchange.eu"

		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"serviceProvider",
				new Capabilities(),
				Collections.singleton(
						localSubscription
				),
				Collections.emptySet(),
				LocalDateTime.now()
		);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		assertThat(serviceProvider.getSubscriptions().stream().findFirst().get().getStatus()).isEqualTo(LocalSubscriptionStatus.CREATED);
	}

	@Test
	public void testNoServiceProviderIsAddedToBiConsumerGroupIfBiconsumerIsFalse() {

		ServiceProvider serviceProvider = new ServiceProvider(
				"serviceProvider",
				false,
				new Capabilities(),
				Collections.emptySet(),
				Collections.emptySet(),
				LocalDateTime.now()
		);
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		assertThat(serviceProvider.isBiconsumer()).isFalse();
		router.addOrRemoveServiceProviderToBiConsumerGroup(serviceProvider, client.getQpidDelta());

		assertThat(client.getBiConsumerMember(serviceProvider.getName())).isNull();
	}
    @Test
    public void testNoServiceProviderIsAddedToBiConsumerGroupIfBiconsumerIsNull() {

        ServiceProvider serviceProvider = new ServiceProvider(
                "serviceProvider",
                null,
                new Capabilities(),
                Collections.emptySet(),
                Collections.emptySet(),
                LocalDateTime.now()
        );
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        router.addOrRemoveServiceProviderToBiConsumerGroup(serviceProvider,client.getQpidDelta());

        assertThat(client.getBiConsumerMember(serviceProvider.getName())).isNull();
    }

	@Test
	public void testServiceProviderAddedToBiConsumerGroup() {

		ServiceProvider serviceProvider = new ServiceProvider(
				"serviceProvider",
				true,
				new Capabilities(),
				Collections.emptySet(),
				Collections.emptySet(),
				LocalDateTime.now()
		);
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		assertThat(serviceProvider.isBiconsumer()).isTrue();
		router.addOrRemoveServiceProviderToBiConsumerGroup(serviceProvider,client.getQpidDelta());

		assertThat(client.getBiConsumerMember(serviceProvider.getName()).name()).isEqualTo(serviceProvider.getName());
	}



	@Test
	public void testIllegalLocalSubscriptionGetsRemovedFromServiceProvider() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.ILLEGAL,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void testLocalSubscriptionWithErrorGetsRemovedFromServiceProvider(){
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.ERROR,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void tearDownLocalSubscriptionWithEmptyMatch() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(new ArrayList<>());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void teardownLocalSubscriptionWithRemainingMatch() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		Match match = new Match(
				subscription,
				new Subscription("",SubscriptionStatus.TEAR_DOWN)
		);

		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
	}

	@Test
	public void redirectSubscriptionStatusTearDownEmptyMatchList() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);

		when(matchRepository.findAllByLocalSubscriptionId(1)).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}


	@Test
	public void redirectSubscriptionStatusIllegal() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.ILLEGAL,
				"",
				"sp1"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}


	@Test
	public void bindNonExistingCapabilityExchangeToBiQueue() {
		Queue queue = client.getQueue("bi-datex");
		assertThat(queue).isNotNull();

		ServiceProvider serviceProvider = new ServiceProvider(
				"my-service-provider",
				new Capabilities(
						Set.of(
								new Capability(
										"123-323",
										new DatexApplication(
												"NO12345",
												"NO12345:001",
												"NO",
												"1.0",
												List.of("1234"),
												"type",
									"publisher"
									),
									new Metadata(
											"https://mysite.com",
											RedirectStatus.OPTIONAL,
											0,
											0,
											0
									),
									List.of(
											new CapabilityShard(
														1,
														"this-exchange-does-not-exist-shard-1",
														"publicationId = 'NO12345:001' and shardId = 1"
												)
										)
								)
						)
				),
				Set.of(),
				Set.of(),
				null
		);

		assertThatNoException().isThrownBy( () -> router.bindCapabilityExchangesToBiQueue(serviceProvider,client.getQpidDelta()));
	}

	@Test
	public void testSetupBindingToBiQueue() {
		{
			String biQueueName = "bi-datex";
			Queue queue = client.getQueue(biQueueName);
			assertThat(queue).isNotNull();

			String exchangeName = UUID.randomUUID().toString();
			Exchange headersExchange = client.createHeadersExchange(exchangeName);

			assertThat(headersExchange.isBoundTo(biQueueName)).isFalse();
			ServiceProvider serviceProvider = new ServiceProvider(
					"my-service-provider",
					new Capabilities(
							Set.of(
									new Capability(
											"123-323",
											new DatexApplication(
													"NO12345",
													"NO12345:001",
													"NO",
													"1.0",
													List.of("1234"),
												"type",
												"publisher"
										),
										new Metadata(
												"https://mysite.com",
												RedirectStatus.OPTIONAL,
												0,
												0,
												0
										),
										List.of(
													new CapabilityShard(
															1,
															exchangeName,
															"publicationId = 'NO12345:001' and shardId = 1"
													)
											)
									)
							)
					),
					Set.of(),
					Set.of(),
					null
			);
			router.bindCapabilityExchangesToBiQueue(serviceProvider,client.getQpidDelta());
			Exchange exchange = client.getExchange(exchangeName);
			assertThat(exchange).isNotNull();
			assertThat(exchange.isBoundTo(biQueueName)).isTrue();
		}
	}


}
