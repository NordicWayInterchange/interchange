package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import no.vegvesen.ixn.federation.service.routing.localdelivery.LocalDeliveryService;
import no.vegvesen.ixn.federation.service.routing.localsubscription.LocalSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceProviderRouterPrivateChannelTest {

	private static final String SERVICE_PROVIDER = "service-provider";

	@Mock
	private ServiceProviderRepository serviceProviderRepository;

	@Mock
	private PrivateChannelRepository privateChannelRepository;

	@Mock
	private QpidClient qpidClient;

	@Mock
	private LocalSubscriptionService localSubscriptionService;

	@Mock
	private InterchangeNodeProperties nodeProperties;

	@Mock
	private LocalDeliveryService localDeliveryService;

	@Mock
	private OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

	private ServiceProviderRouter router;

	@BeforeEach
	void setUp() {
		router = new ServiceProviderRouter(
				serviceProviderRepository,
				privateChannelRepository,
				qpidClient,
				localSubscriptionService,
				nodeProperties,
				localDeliveryService,
				outgoingMatchDiscoveryService
		);

		when(privateChannelRepository.findAllByPeerNameAndStatus(anyString(), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(Collections.emptyList());
		when(privateChannelRepository.countByServiceProviderNameAndStatus(anyString(), eq(PrivateChannelStatus.CREATED)))
				.thenReturn(0L);
		when(privateChannelRepository.findAllByStatusAndServiceProviderName(eq(PrivateChannelStatus.CREATED), anyString()))
				.thenReturn(Collections.emptyList());
		when(privateChannelRepository.save(any(PrivateChannel.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(qpidClient.getQpidAcl()).thenAnswer(invocation -> freshAcl());
		when(qpidClient.addPrivateChannelMemberToGroup(anyString()))
				.thenAnswer(invocation -> new PrivateChannelMember(invocation.getArgument(0)));
	}

	private static VirtualHostAccessController freshAcl() {
		List<AclRule> rules = new ArrayList<>();
		rules.add(new AclRule("ALL", "ALL", "DENY-LOG", "ALL", Map.of()));
		return new VirtualHostAccessController("acl-id", rules);
	}

	private static PrivateChannel channel(PrivateChannelStatus status, String queueName, Set<Peer> peers) {
		PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint("my-interchange", 5671, queueName);
		return new PrivateChannel(peers, status, endpoint, SERVICE_PROVIDER);
	}

	private static ServiceProvider serviceProvider() {
		return new ServiceProvider(SERVICE_PROVIDER);
	}

	private void stubChannels(PrivateChannel... channels) {
		when(privateChannelRepository.findAllByServiceProviderName(SERVICE_PROVIDER))
				.thenReturn(new ArrayList<>(Arrays.asList(channels)));
	}

	@Test
	void requestedChannelWithNoPeersCreatesQueueAndOnlyGrantsServiceProviderAccess() {
		PrivateChannel channel = channel(PrivateChannelStatus.REQUESTED, "queue-1", Collections.emptySet());
		stubChannels(channel);

		when(qpidClient.createNonDestructiveQueue("queue-1")).thenReturn(new Queue("queue-1"));
		when(qpidClient.addPrivateChannelMemberToGroup(SERVICE_PROVIDER)).thenReturn(new PrivateChannelMember(SERVICE_PROVIDER));

		QpidDelta delta = new QpidDelta(List.of(), List.of());

		router.syncPrivateChannels(serviceProvider(), delta);

		assertThat(channel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		verify(qpidClient).createNonDestructiveQueue("queue-1");
		verify(qpidClient, times(1)).addPrivateChannelMemberToGroup(anyString());
		verify(qpidClient).addPrivateChannelMemberToGroup(SERVICE_PROVIDER);
		verify(qpidClient).postQpidAcl(any());
	}
}
