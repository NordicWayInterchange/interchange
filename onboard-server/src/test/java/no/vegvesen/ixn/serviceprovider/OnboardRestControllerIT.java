package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.RedirectStatusApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.google.common.collect.Iterators;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class OnboardRestControllerIT {


    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private PrivateChannelRepository privateChannelRepository;

    @MockBean
    private CertService certService;

    @Autowired
    private InterchangeNodeProperties nodeProperties;

    @Autowired
    private OnboardRestController restController;

    @Test
    public void testDeletingCapability() {
        DatexApplicationApi app = new DatexApplicationApi("NO-123", "NO-pub", "NO", "1.0", Collections.emptySet(), "SituationPublication");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilitySplitApi datexNO = new CapabilitySplitApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);
        String serviceProviderName = "serviceprovider";

        AddCapabilitiesResponse addedCapability = restController.addCapabilities(serviceProviderName, new AddCapabilitiesRequest(
                serviceProviderName,
                Collections.singleton(datexNO)
        ));
        assertThat(addedCapability).isNotNull();
		ListCapabilitiesResponse serviceProviderCapabilities = restController.listCapabilities(serviceProviderName);
        assertThat(serviceProviderCapabilities.getCapabilities()).hasSize(1);

        //Test that we don't mess up subscriptions and capabilities
        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(0);

        LocalActorCapability saved = serviceProviderCapabilities.getCapabilities().iterator().next();
        //LocalCapability saved = serviceProviderCapabilities.getCapabilities().iterator().next();
        restController.deleteCapability(serviceProviderName, saved.getId());

        //We did four calls to the controller, thus we should have checked the cert 4 times
        verify(certService,times(4)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    public void testGettingCapability() {
        DatexApplicationApi app = new DatexApplicationApi("NO-123", "NO-pub", "NO", "1.0", Collections.emptySet(), "SituationPublication");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilitySplitApi datexNO = new CapabilitySplitApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);
        String serviceProviderName = "serviceprovider";

        AddCapabilitiesResponse addedCapability = restController.addCapabilities(serviceProviderName, new AddCapabilitiesRequest(
                serviceProviderName,
                Collections.singleton(datexNO)
        ));
        assertThat(addedCapability).isNotNull();
        LocalActorCapability capability = addedCapability.getCapabilities().stream().findFirst()
                .orElseThrow(() -> new AssertionError("No capabilities in response"));
        GetCapabilityResponse response = restController.getServiceProviderCapability(serviceProviderName,capability.getId());
        assertThat(response.getId()).isEqualTo(capability.getId());

        verify(certService,times(2)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    public void testAddingIllegalSubscription() {
        String serviceProviderName = "serviceprovider";
        String selector = "";

        AddSubscription addSubscription = new AddSubscription(selector, serviceProviderName);
        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));

        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProviderName, requestApi);

        LocalActorSubscription addedSubscription = response.getSubscriptions().stream()
                .findFirst()
                .get();

        assertThat(response.getSubscriptions()).hasSize(1);
        assertThat(addedSubscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.ILLEGAL);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testDeletingSubscription() {
		LocalDateTime beforeDeleteTime = LocalDateTime.now();
        String serviceProviderName = "serviceprovider";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscription addSubscription = new AddSubscription(selector, serviceProviderName);

        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));
        restController.addSubscriptions(serviceProviderName, requestApi);

        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

		ServiceProvider afterAddSubscription = serviceProviderRepository.findByName(serviceProviderName);
		assertThat(afterAddSubscription.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(beforeDeleteTime));

		LocalActorSubscription subscriptionApi = serviceProviderSubscriptions.getSubscriptions().stream().findFirst().get();
        restController.deleteSubscription(serviceProviderName,subscriptionApi.getId());

		ServiceProvider afterDeletedSubscription = serviceProviderRepository.findByName(serviceProviderName);
		assertThat(afterDeletedSubscription.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(beforeDeleteTime));
        verify(certService,times(3)).checkIfCommonNameMatchesNameInApiObject(anyString());
	}

    @Test
    public void testAddingSubscriptionConsumerCommonNameAsIxnName() {
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        String serviceProvider = "serviceprovider";
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(serviceProvider,Collections.singleton(new AddSubscription(selector, nodeProperties.getName())));
        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProvider,request);
        assertThat(response.getSubscriptions()).hasSize(1);
        LocalActorSubscription subscription = response.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.REQUESTED);
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(serviceProvider);
    }

	@Test
    public void testAddingSubscriptionWithEmptyConsumerCommonName() {
        String serviceProviderName = "serviceprovider";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscription addSubscription = new AddSubscription(selector);

        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));
        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProviderName, requestApi);

        LocalActorSubscription subscription = response.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.REQUESTED);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testAddingSubscriptionWithWrongConsumerCommonName() {
        String serviceProviderName = "serviceprovider";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscription addSubscription = new AddSubscription(selector, "anna");

        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));
        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProviderName, requestApi);

        LocalActorSubscription subscription = response.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.ILLEGAL);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testAddingSubscriptionWhenAnIdenticalWithDifferentStatusAlreadyExists() {
        String selector = "messageType = 'DATEX2' and originatingCountry = 'NO'";
        String serviceproviderName = "serviceprovider";
        ServiceProvider serviceProvider = new ServiceProvider(
                serviceproviderName,
                new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,Collections.emptySet()),
                Collections.singleton(new LocalSubscription(LocalSubscriptionStatus.ILLEGAL, selector,nodeProperties.getName())),
                Collections.emptySet(),
                LocalDateTime.now()
        );
        serviceProviderRepository.save(serviceProvider);

        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                serviceproviderName,
                Collections.singleton(new AddSubscription(selector))
        );
        AddSubscriptionsResponse response = restController.addSubscriptions(serviceproviderName,request);
        assertThat(response.getSubscriptions()).hasSize(1);
        LocalActorSubscription subscription = response.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.ILLEGAL); //the original one should be the one there
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceproviderName);



    }

	@Test
	void testDeletingNonExistingSubscriptionDoesNotModifyLastUpdatedSubscription() {
		String serviceProviderName = "serviceprovider-non-existing-subscription-delete";
        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(
		        serviceProviderName,
                Collections.singleton(new AddSubscription("messageType = 'DATEX2' AND originatingCountry = 'NO'", "my-node"))
        );
         restController.addSubscriptions(serviceProviderName, requestApi);

		ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
		assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);
		ServiceProvider savedSP = serviceProviderRepository.findByName(serviceProviderName);
		Optional<LocalDateTime> subscriptionUpdated = savedSP.getSubscriptionUpdated();
		assertThat(subscriptionUpdated).isPresent();

		try {
			restController.deleteSubscription(serviceProviderName, "-1");
		} catch (NotFoundException ignore) {
		}
		ServiceProvider savedSPAfterDelete = serviceProviderRepository.findByName(serviceProviderName);

		assertThat(savedSPAfterDelete.getSubscriptionUpdated()).isEqualTo(subscriptionUpdated);
        verify(certService,times(3)).checkIfCommonNameMatchesNameInApiObject(anyString());
	}

    @Test
    public void testGetSingleSubscription() {
        Set<AddSubscription> addSubscriptions = new HashSet<>();
        addSubscriptions.add(new AddSubscription("countryCode = 'SE' and messageType = 'DENM'", "king_olav.bouvetinterchange.eu"));
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                "king_olav.bouvetinterchange.eu",
                addSubscriptions
        );
        AddSubscriptionsResponse response = restController.addSubscriptions("king_olav.bouvetinterchange.eu", request);

        Optional<LocalActorSubscription> anySubscription = response.getSubscriptions().stream().findAny();
        LocalActorSubscription subscription = anySubscription.get();
        GetSubscriptionResponse getSubscriptionResponse = restController.getServiceProviderSubscription("king_olav.bouvetinterchange.eu", subscription.getId());
        assertThat(getSubscriptionResponse).isNotNull();
        verify(certService,times(2)).checkIfCommonNameMatchesNameInApiObject(anyString());
        assertThat(subscription.getConsumerCommonName()).isEqualTo("king_olav.bouvetinterchange.eu");
    }


	@Test
    void testAddingLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderName() {
        String serviceProviderName = "service-provider-create-new-queue";
        String selector= "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        restController.addSubscriptions(serviceProviderName, new AddSubscriptionsRequest(serviceProviderName,Collections.singleton(new AddSubscription(selector, serviceProviderName))));

        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

        ServiceProvider savedSP = serviceProviderRepository.findByName(serviceProviderName);
        Set<LocalSubscription> localSubscriptions = savedSP.getSubscriptions();
        assertThat(localSubscriptions).hasSize(1);
        LocalSubscription subscription = localSubscriptions.stream().findFirst().get();

        assertThat(subscription.getConsumerCommonName()).isEqualTo(serviceProviderName);

        ListSubscriptionsResponse subscriptions = restController.listSubscriptions(serviceProviderName);
        Set<LocalActorSubscription> localSubscriptionApis = subscriptions.getSubscriptions();
        assertThat(localSubscriptionApis.size()).isEqualTo(1);
        verify(certService,times(3)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testAddingLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderNameAndGetApiObject() {
        String serviceProviderName = "service-provider-create-new-queue";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscriptionsResponse serviceProviderSubscriptions = restController.addSubscriptions(serviceProviderName, new AddSubscriptionsRequest(serviceProviderName,Collections.singleton(new AddSubscription(selector, serviceProviderName))));
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);
    }

    @Test
    void testFetchingAllCapabilitiesWhenServiceProviderExists() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                        "NPRA",
                        "pub-1",
                        "NO",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(serviceProvider);
        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                        "SPRA",
                        "pub-2",
                        "SE",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                        "DPRA",
                        "pub-3",
                        "DK",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        FetchMatchingCapabilitiesResponse response = restController.fetchMatchingCapabilities(serviceProvider.getName(), null);
        assertThat(response.getCapabilities()).hasSize(3);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testFetchingAllCapabilitiesWhenServiceProviderDoesNotExist() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                        "SPRA",
                        "pub-1",
                        "SE",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                        "DPRA",
                        "pub-2",
                        "DK",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        FetchMatchingCapabilitiesResponse response = restController.fetchMatchingCapabilities(serviceProvider.getName(), "");
        assertThat(response.getCapabilities()).hasSize(2);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testFetchingAllMatchingCapabilities() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                                "NPRA",
                                "pub-1",
                                "NO",
                                "1.0",
                                Collections.singleton("1234"),
                                Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(serviceProvider);
        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                                "SPRA",
                                "pub-2",
                                "SE",
                                "1.0",
                                Collections.singleton("1234"),
                                Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                                "DPRA",
                                "pub-3",
                                "DK",
                                "1.0",
                                Collections.singleton("1234"),
                                Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        String selector = "messageType = 'DENM' and quadTree like '%,1234%' AND originatingCountry = 'NO'";

        FetchMatchingCapabilitiesResponse response = restController.fetchMatchingCapabilities(serviceProvider.getName(), selector);
        assertThat(response.getCapabilities()).hasSize(1);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testFetchingAllMatchingCapabilitiesWhenSelectorIsNull() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                                "NPRA",
                                "pub-1",
                                "NO",
                                "1.0",
                                Collections.singleton("1234"),
                                Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(serviceProvider);
        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                                "SPRA",
                                "pub-2",
                                "SE",
                                "1.0",
                                Collections.singleton("1234"),
                                Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new CapabilitySplit(
                        new DenmApplication(
                                "DPRA",
                                "pub-3",
                                "DK",
                                "1.0",
                                Collections.singleton("1234"),
                                Collections.singleton(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        FetchMatchingCapabilitiesResponse response = restController.fetchMatchingCapabilities(serviceProvider.getName(), null);
        assertThat(response.getCapabilities()).hasSize(3);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    public void testAddingIllegalDelivery() {
        String serviceProviderName = "my-service-provider";
        String selector = "";

        SelectorApi delivery = new SelectorApi(selector);
        AddDeliveriesRequest requestApi = new AddDeliveriesRequest(serviceProviderName, Collections.singleton(delivery));

        AddDeliveriesResponse response = restController.addDeliveries(serviceProviderName, requestApi);

        Delivery addedDelivery = response.getDeliveries().stream()
                .findFirst()
                .get();

        assertThat(response.getDeliveries()).hasSize(1);
        assertThat(addedDelivery.getStatus()).isEqualTo(DeliveryStatus.ILLEGAL);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testAddingMoreThanOneIdenticalDeliveries() {
        String serviceProviderName = "my-service-provider";
        String selector = "messageType = 'DENM'";
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Collections.singleton(
                        new SelectorApi(selector)
                )
        );
        AddDeliveriesResponse response = restController.addDeliveries(serviceProviderName,request);
        assertThat(response.getDeliveries()).hasSize(1);
        assertThat(response.getDeliveries()).allMatch(d -> d.getStatus().equals(DeliveryStatus.REQUESTED));

        //change the delivery status in the database
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        assertThat(serviceProvider.getDeliveries()).hasSize(1);
        serviceProvider.getDeliveries().stream().forEach( d -> d.setStatus(LocalDeliveryStatus.CREATED));
        serviceProviderRepository.save(serviceProvider);

        //now, add the second delivery with original status
        response = restController.addDeliveries(serviceProviderName,request);
        assertThat(response.getDeliveries()).hasSize(1);

        serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        assertThat(serviceProvider.getDeliveries()).hasSize(1);

    }

    @Test
    public void testAddingDuplicateChannels(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel = new PrivateChannelApi("my-channel", serviceProviderName);
        restController.addPrivateChannel(serviceProviderName,clientChannel);

        assertThrows(RuntimeException.class, ()->{
            restController.addPrivateChannel(serviceProviderName,clientChannel);
        });
    }
    @Test
    public void testAddingAndDeletingChannel(){

        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel = new PrivateChannelApi("my-channel", serviceProviderName);
        PrivateChannelApi channelApi = restController.addPrivateChannel(serviceProviderName,clientChannel);
        restController.deletePrivateChannel(serviceProviderName,channelApi.getId().toString());
        assertThat(privateChannelRepository.findAllByStatus(PrivateChannelStatus.TEAR_DOWN).size()==1);
    }
    @Test
    public void testAddingMultipleChannels(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel_1 = new PrivateChannelApi("my-channel", serviceProviderName);
        PrivateChannelApi clientChannel_2 = new PrivateChannelApi("my-channel2", serviceProviderName);
        PrivateChannelApi clientChannel_3 = new PrivateChannelApi("my-channel3", serviceProviderName);
        restController.addPrivateChannel(serviceProviderName,clientChannel_1);
        restController.addPrivateChannel(serviceProviderName,clientChannel_2);
        restController.addPrivateChannel(serviceProviderName, clientChannel_3);
        assertThat(privateChannelRepository.findAll().size() == 3);
    }

}
