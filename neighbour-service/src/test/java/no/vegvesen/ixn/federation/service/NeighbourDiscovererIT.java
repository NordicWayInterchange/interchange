package no.vegvesen.ixn.federation.service;


import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import javax.net.ssl.SSLContext;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class NeighbourDiscovererIT {

	@MockBean
	SSLContext mockedSSL;

	@MockBean
	DNSFacade mockDnsFacade;

	@MockBean
	NeighbourFacade mockNeighbourFacade;

	@MockBean
	SelfService selfService;

	@Autowired
	NeighbourService neighbourService;

	@Autowired
	NeighbourRepository repository;

	@Autowired
	InterchangeNodeProperties nodeProperties;

	@Test
	public void discovererIsAutowired() {
		assertThat(neighbourService).isNotNull();
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow() {
		assertThat(repository.findAll()).withFailMessage("The test shall start with no neighbours stored. Use @Transactional.").hasSize(0);
		Self self = new Self(nodeProperties.getName());
		self.setLocalSubscriptions(Sets.newLinkedHashSet(getDataType(Datex2DataTypeApi.DATEX_2, "NO")));
		self.setLastUpdatedLocalSubscriptions(LocalDateTime.now());
		when(selfService.fetchSelf()).thenReturn(self);

		Neighbour neighbour1 = new Neighbour();
		neighbour1.setName("neighbour-one");
		Neighbour neighbour2 = new Neighbour();
		neighbour2.setName("neighbour-two");

		Capabilities c1 = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDataType(Datex2DataTypeApi.DATEX_2, "NO")));
		Capabilities c2 = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(getDataType(Datex2DataTypeApi.DATEX_2, "FI")));
		when(mockDnsFacade.lookupNeighbours()).thenReturn(Lists.list(neighbour1, neighbour2));

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

		Self self = selfService.fetchSelf();
		self.setLastUpdatedLocalCapabilities(LocalDateTime.now());

		neighbourService.capabilityExchangeWithNeighbours(selfService.fetchSelf(), mockNeighbourFacade);
		verify(mockNeighbourFacade, times(4)).postCapabilitiesToCapabilities(any(), any() );

		List<Neighbour> toConsumeMessagesFrom = neighbourService.listNeighboursToConsumeMessagesFrom();
		assertThat(toConsumeMessagesFrom).hasSize(1);
	}

	@Test
	public void messageCollectorWillStartAfterCompleteOptimisticControlChannelFlowAndExtraIncomingCapabilityExchange() {
		messageCollectorWillStartAfterCompleteOptimisticControlChannelFlow();

		neighbourService.incomingCapabilities(new CapabilitiesApi("neighbour-one", Sets.newLinkedHashSet(new DatexCapabilityApi("NO"))), selfService.fetchSelf());
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
		when(mockNeighbourFacade.postCapabilitiesToCapabilities(eq(neighbour1), any())).thenReturn(c1);
		when(mockNeighbourFacade.postCapabilitiesToCapabilities(eq(neighbour2), any() )).thenReturn(c2);

		neighbourService.capabilityExchangeWithNeighbours(selfService.fetchSelf(), mockNeighbourFacade);

		verify(mockNeighbourFacade, times(2)).postCapabilitiesToCapabilities(any(), any());
		List<Neighbour> known = repository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
		assertThat(known).hasSize(2);
	}

	private Subscription performSubscriptionRequest(Neighbour neighbour1, Neighbour neighbour2, Capabilities c1) {
		SubscriptionRequest subscriptionRequestResponse = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, c1.getDataTypes().stream().map(dataType -> new Subscription(dataType.toSelector(), SubscriptionStatus.ACCEPTED)).collect(Collectors.toSet()));
		when(mockNeighbourFacade.postSubscriptionRequest(any(), anySet(), any())).thenReturn(subscriptionRequestResponse);

		neighbourService.evaluateAndPostSubscriptionRequest(Lists.newArrayList(neighbour1, neighbour2), selfService.fetchSelf(), mockNeighbourFacade);

		verify(mockNeighbourFacade, times(1)).postSubscriptionRequest(eq(neighbour1), any(), any());
		verify(mockNeighbourFacade, times(0)).postSubscriptionRequest(eq(neighbour2), any(), any());

		Neighbour found1 = repository.findByName(neighbour1.getName());
		assertThat(found1).isNotNull();
		assertThat(found1.getNeighbourRequestedSubscriptions()).isNotNull();
		assertThat(found1.getSubscriptionsForPolling()).hasSize(1);
		return found1.getSubscriptionsForPolling().iterator().next();
	}

	private void performSubscriptionPolling(Neighbour neighbour, Subscription requestedSubscription) {
		when(mockNeighbourFacade.pollSubscriptionStatus(any(), any())).thenReturn(new Subscription(requestedSubscription.getSelector(), SubscriptionStatus.CREATED));
		neighbourService.pollSubscriptions(mockNeighbourFacade);
		Neighbour found1 = repository.findByName(neighbour.getName());
		assertThat(found1).isNotNull();
		assertThat(found1.getOurRequestedSubscriptions()).isNotNull();
		assertThat(found1.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
		assertThat(found1.getOurRequestedSubscriptions().getSubscriptions().iterator().next().getSubscriptionStatus()).isEqualTo(SubscriptionStatus.CREATED);
	}


	static DataType getDataType(String messageType, String originatingCountry) {
		DataType dataType = new DataType();
		dataType.getValues().put(MessageProperty.MESSAGE_TYPE.getName(), messageType);
		dataType.getValues().put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		return dataType;
	}

}
