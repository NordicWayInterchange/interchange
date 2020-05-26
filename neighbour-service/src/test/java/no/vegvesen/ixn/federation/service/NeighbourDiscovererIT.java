package no.vegvesen.ixn.federation.service;


import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourRESTFacade;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import sun.net.www.http.HttpClient;

import javax.net.ssl.SSLContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class NeighbourDiscovererIT {

	@MockBean
	RestTemplate restTemplate;

	@MockBean
	SSLContext mockedSSL;

	@MockBean
	HttpClient mockedHttpClient;

	@MockBean
	DNSFacade mockDnsFacade;

	@MockBean
	NeighbourRESTFacade mockNeighbourRESTFacade;

	@Autowired
	SelfRepository selfRepository;

	@Autowired
	NeighbourService neighbourService;

	@Autowired
	NeighbourRepository repository;

	@Value("${interchange.node-provider.name}")
	String nodeName;

	@Test
	public void discovererIsAutowired() {
		assertThat(neighbourService).isNotNull();
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow() {
		assertThat(repository.findAll()).withFailMessage("The test shall start with no neighbours stored. Use @Transactional.").hasSize(0);
		Self self = neighbourService.getSelf();
		self.setLocalSubscriptions(Sets.newLinkedHashSet(getDataType(Datex2DataTypeApi.DATEX_2, "NO")));
		neighbourService.saveSelf(self);

		Neighbour neighbour1 = new Neighbour();
		neighbour1.setName("neighbour-one");
		Neighbour neighbour2 = new Neighbour();
		neighbour2.setName("neighbour-two");

		Capabilities c1 = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDataType(Datex2DataTypeApi.DATEX_2, "NO")));
		Capabilities c2 = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDataType(Datex2DataTypeApi.DATEX_2, "FI")));
		when(mockDnsFacade.getNeighbours()).thenReturn(Lists.list(neighbour1, neighbour2));

		checkForNewNeighbours();
		performCapabilityExchangeAndVerifyNeighbourRestFacadeCalls(neighbour1, neighbour2, c1, c2);
		Subscription requestedSubscription = performSubscriptionRequest(neighbour1, neighbour2, c1);
		performSubscriptionPolling(neighbour1, requestedSubscription);

		List<Neighbour> toConsumeMessagesFrom = neighbourService.listNeighboursToConsumeMessagesFrom();
		assertThat(toConsumeMessagesFrom).hasSize(1);
		assertThat(toConsumeMessagesFrom).contains(neighbour1);
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlowAndExtraCapabilityExchange() {
		messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow();

		Self self = neighbourService.getSelf();
		self.setLastUpdatedLocalCapabilities(LocalDateTime.now());

		neighbourService.capabilityExchangeWithNeighbours();
		verify(mockNeighbourRESTFacade, times(4)).postCapabilitiesToCapabilities(any(), any());

		List<Neighbour> toConsumeMessagesFrom = neighbourService.listNeighboursToConsumeMessagesFrom();
		assertThat(toConsumeMessagesFrom).hasSize(1);
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlowAndExtraIncomingCapabilityExchange() {
		messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow();

		neighbourService.incomingCapabilities(new CapabilityApi("neighbour-one", Sets.newLinkedHashSet(new Datex2DataTypeApi("NO"))));
		List<Neighbour> toConsumeMessagesFrom = neighbourService.listNeighboursToConsumeMessagesFrom();
		assertThat(toConsumeMessagesFrom).hasSize(1);
	}

	private void checkForNewNeighbours() {
		neighbourService.checkForNewNeighbours();
		List<Neighbour> unknown = repository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.UNKNOWN);
		assertThat(unknown).hasSize(2);
		assertThat(repository.findAll()).hasSize(2);
	}

	private void performCapabilityExchangeAndVerifyNeighbourRestFacadeCalls(Neighbour neighbour1, Neighbour neighbour2, Capabilities c1, Capabilities c2) {
		when(mockNeighbourRESTFacade.postCapabilitiesToCapabilities(any(), eq(neighbour1))).thenReturn(c1);
		when(mockNeighbourRESTFacade.postCapabilitiesToCapabilities(any(), eq(neighbour2))).thenReturn(c2);

		neighbourService.capabilityExchangeWithNeighbours();

		verify(mockNeighbourRESTFacade, times(2)).postCapabilitiesToCapabilities(any(), any());
		List<Neighbour> known = repository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
		assertThat(known).hasSize(2);
	}

	private Subscription performSubscriptionRequest(Neighbour neighbour1, Neighbour neighbour2, Capabilities c1) {
		SubscriptionRequest subscriptionRequestResponse = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, c1.getDataTypes().stream().map(dataType -> new Subscription(dataType.toSelector(), SubscriptionStatus.ACCEPTED)).collect(Collectors.toSet()));
		when(mockNeighbourRESTFacade.postSubscriptionRequest(any(), any(), anySet())).thenReturn(subscriptionRequestResponse);

		neighbourService.evaluateAndPostSubscriptionRequest(Lists.newArrayList(neighbour1, neighbour2));

		verify(mockNeighbourRESTFacade, times(1)).postSubscriptionRequest(any(), eq(neighbour1), any());
		verify(mockNeighbourRESTFacade, times(0)).postSubscriptionRequest(any(), eq(neighbour2), any());

		Neighbour found1 = repository.findByName(neighbour1.getName());
		assertThat(found1).isNotNull();
		assertThat(found1.getSubscriptionRequest()).isNotNull();
		assertThat(found1.getSubscriptionsForPolling()).hasSize(1);
		return found1.getSubscriptionsForPolling().iterator().next();
	}

	private void performSubscriptionPolling(Neighbour neighbour, Subscription requestedSubscription) {
		when(mockNeighbourRESTFacade.pollSubscriptionStatus(any(), any())).thenReturn(new Subscription(requestedSubscription.getSelector(), SubscriptionStatus.CREATED));
		neighbourService.pollSubscriptions();
		Neighbour found1 = repository.findByName(neighbour.getName());
		assertThat(found1).isNotNull();
		assertThat(found1.getFedIn()).isNotNull();
		assertThat(found1.getFedIn().getSubscriptions()).hasSize(1);
		assertThat(found1.getFedIn().getSubscriptions().iterator().next().getSubscriptionStatus()).isEqualTo(SubscriptionStatus.CREATED);
	}


	private DataType getDataType(String messageType, String originatingCountry) {
		DataType dataType = new DataType();
		dataType.getValues().put(MessageProperty.MESSAGE_TYPE.getName(), messageType);
		dataType.getValues().put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		return dataType;
	}

}
