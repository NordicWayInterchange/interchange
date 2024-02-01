package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.RedirectStatusApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.CouldNotParseIdException;
import no.vegvesen.ixn.federation.exceptions.PrivateChannelException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import jakarta.transaction.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        DatexApplicationApi app = new DatexApplicationApi("NO-123", "NO-pub", "NO", "1.0", Collections.singleton("1200"), "SituationPublication");
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
        DatexApplicationApi app = new DatexApplicationApi("NO-123", "NO-pub", "NO", "1.0", Collections.singleton("1200"), "SituationPublication");
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
    public void testAddingCapabilityWithMissingProperties() {
        DatexApplicationApi app = new DatexApplicationApi("", "NO-pub-1", "NO", "1.0", Collections.singleton("1200"), "SituationPublication");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilitySplitApi datexNO = new CapabilitySplitApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);

        String serviceProviderName = "my-service-provider";
        CapabilityPostException thrown = assertThrows(CapabilityPostException.class, () -> restController.addCapabilities(serviceProviderName,
                new AddCapabilitiesRequest(
                        serviceProviderName,
                        Collections.singleton(datexNO)
                )));

        assertThat(thrown.getMessage()).contains("publisherId");
    }

    @Test
    public void testAddingCapabilityWithPublisherIdMatchingALocalCapability() {
        DatexApplicationApi app = new DatexApplicationApi("NO00000", "NO-pub-1", "NO", "1.0", Collections.singleton("1200"), "SituationPublication");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilitySplitApi datexNO = new CapabilitySplitApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);

        ServiceProvider other = new ServiceProvider("other-service-provider");
        other.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(
                        new CapabilitySplit(
                            new DatexApplication(
                                    "NO00000",
                                    "NO-pub-1",
                                    "NO",
                                    "1.0",
                                    Collections.singleton("1200"),
                                    "SituationPublication"),
                                new Metadata()
                ))));

        serviceProviderRepository.save(other);

        String serviceProviderName = "my-service-provider";

        CapabilityPostException thrown = assertThrows(CapabilityPostException.class, () -> restController.addCapabilities(serviceProviderName,
                new AddCapabilitiesRequest(
                        serviceProviderName,
                        Collections.singleton(datexNO)
                )));

        assertThat(thrown.getMessage()).contains("publicationId");
    }

    @Test
    public void testAddingCapabilityWithPublisherIdMatchingANeighbourCapability() {
        DatexApplicationApi app = new DatexApplicationApi("NO00000", "NO-pub-1", "NO", "1.0", Collections.singleton("1200"), "SituationPublication");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilitySplitApi datexNO = new CapabilitySplitApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);

        Neighbour other = new Neighbour("my-neighbour",
                new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,
                        Collections.singleton(
                                new CapabilitySplit(
                                        new DatexApplication(
                                                "NO00000",
                                                "NO-pub-1",
                                                "NO",
                                                "1.0",
                                                Collections.singleton("1200"),
                                                "SituationPublication"),
                                        new Metadata()
                                ))),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest()
        );

        neighbourRepository.save(other);

        String serviceProviderName = "my-service-provider";

        CapabilityPostException thrown = assertThrows(CapabilityPostException.class, () -> restController.addCapabilities(serviceProviderName,
                new AddCapabilitiesRequest(
                        serviceProviderName,
                        Collections.singleton(datexNO)
                )));

        assertThat(thrown.getMessage()).contains("publicationId");
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
    public void testAddingAndDeletingChannel(){

        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel = new PrivateChannelApi("my-channel");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(List.of(clientChannel));

        AddPrivateChannelResponse response = restController.addPrivateChannel(serviceProviderName, request);

        System.out.println(response.getPrivateChannels().get(0));
        restController.deletePrivateChannel(serviceProviderName,response.getPrivateChannels().get(0).getId().toString());
        assertThat(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.TEAR_DOWN, serviceProviderName).size()).isEqualTo(1);
    }

    @Test
    public void testAddingChannels(){
        String serviceProviderName = "my-service-provider";

        PrivateChannelApi clientChannel_1 = new PrivateChannelApi("my-channel");
        PrivateChannelApi clientChannel_2 = new PrivateChannelApi("my-channel2");
        PrivateChannelApi clientChannel_3 = new PrivateChannelApi("my-channel3");

        restController.addPrivateChannel(serviceProviderName,new AddPrivateChannelRequest(List.of(clientChannel_1, clientChannel_2, clientChannel_3)));

        assertThat(privateChannelRepository.findAll()).hasSize(3);
        PrivateChannelException thrown = assertThrows(PrivateChannelException.class, () -> restController.addPrivateChannel(serviceProviderName, null));
        assertThat(thrown.getMessage()).isEqualTo("Private channel can not be null");
    }
    @Test
    public void testAddingChannelWithServiceProviderAsPeerName(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel = new PrivateChannelApi(serviceProviderName);

        PrivateChannelException thrown = assertThrows(PrivateChannelException.class, () -> restController.addPrivateChannel(serviceProviderName, new AddPrivateChannelRequest(List.of(clientChannel))));
        assertThat(thrown.getMessage()).isEqualTo("Can't add private channel with serviceProviderName as peerName");
    }
    @Test
    public void testDeletingNonExistentChannel(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel = new PrivateChannelApi("my-channel");

        restController.addPrivateChannel(serviceProviderName,new AddPrivateChannelRequest(List.of(clientChannel)));

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> restController.deletePrivateChannel(serviceProviderName, "99"));
        assertThat(thrown.getMessage()).isEqualTo("The private channel to delete is not in the Service Provider private channels. Cannot delete private channel that don't exist.");
        assertThat(privateChannelRepository.findAll()).hasSize(1);
    }
    @Test
    public void testDeletingInvalidChannelId(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel = new PrivateChannelApi("my-channel");

        restController.addPrivateChannel(serviceProviderName, new AddPrivateChannelRequest(List.of(clientChannel)));

        CouldNotParseIdException thrown = assertThrows(CouldNotParseIdException.class, () -> restController.deletePrivateChannel(serviceProviderName, "notAnId"));
        assertThat(thrown.getMessage()).contains("invalid");
    }

    @Test
    public void testGettingPrivateChannels(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel_1 = new PrivateChannelApi("my-channel");
        PrivateChannelApi clientChannel_2 = new PrivateChannelApi("my-channel2");
        PrivateChannelApi clientChannel_3 = new PrivateChannelApi("my-channel3");
        restController.addPrivateChannel(serviceProviderName,new AddPrivateChannelRequest(List.of(clientChannel_1, clientChannel_2, clientChannel_3)));
        ListPrivateChannelsResponse response = restController.getPrivateChannels(serviceProviderName);
        assertThat(response.getPrivateChannels().size()).isEqualTo(3);
    }

    @Test
    public void testGettingChannel(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel_1 = new PrivateChannelApi("my-channel");
        PrivateChannelApi clientChannel_2 = new PrivateChannelApi("my-channel2");
        PrivateChannelApi clientChannel_3 = new PrivateChannelApi("my-channel3");

        AddPrivateChannelResponse privateChannels = restController.addPrivateChannel(serviceProviderName,new AddPrivateChannelRequest(List.of(clientChannel_1, clientChannel_2, clientChannel_3)));
        GetPrivateChannelResponse channelResponse = restController.getPrivateChannel(serviceProviderName, privateChannels.getPrivateChannels().get(0).getId().toString());

        assertThat(channelResponse).isNotNull();
    }
    @Test
    public void testGettingNonExistentChannel(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel_1 = new PrivateChannelApi("my-channel");
        restController.addPrivateChannel(serviceProviderName, new AddPrivateChannelRequest(List.of(clientChannel_1)));

       NotFoundException thrown = assertThrows(NotFoundException.class, () -> restController.getPrivateChannel(serviceProviderName, "99"));
       assertThat(thrown.getMessage()).contains("Could not find");
    }
    @Test
    public void testGettingChannelWithInvalidId(){
        String serviceProviderName = "my-service-provider";
        PrivateChannelApi clientChannel_1 = new PrivateChannelApi("my-channel");
        restController.addPrivateChannel(serviceProviderName, new AddPrivateChannelRequest(List.of(clientChannel_1)));

        CouldNotParseIdException thrown = assertThrows(CouldNotParseIdException.class, () -> restController.getPrivateChannel(serviceProviderName, "notAnId"));
        assertThat(thrown.getMessage()).contains("invalid");

    }
    @Test
    public void testGetPeerPrivateChannels(){
        String serviceProviderName_1 = "my-service-provider";
        String serviceProviderName_2 = "my-service-provider2";

        PrivateChannelApi clientChannel_1 = new PrivateChannelApi("my-channel");
        PrivateChannelApi clientChannel_2 = new PrivateChannelApi(serviceProviderName_1);

        restController.addPrivateChannel(serviceProviderName_1,new AddPrivateChannelRequest(List.of(clientChannel_1)));
        restController.addPrivateChannel(serviceProviderName_2, new AddPrivateChannelRequest(List.of(clientChannel_2)));

        ListPeerPrivateChannels response_1 = restController.getPeerPrivateChannels(serviceProviderName_1);
        ListPeerPrivateChannels response_2 = restController.getPeerPrivateChannels(serviceProviderName_2);
        assertThat(response_1.getPrivateChannels().size()).isEqualTo(1);
        assertThat(response_2.getPrivateChannels().size()).isEqualTo(0);
    }

    @Test
    public void testListDeliveries(){
        String serviceProviderName = "my-service-provider";
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Set.of(
                        new SelectorApi("messageType = 'DENM'")
                )
        );
        restController.addDeliveries(serviceProviderName, request);
        ListDeliveriesResponse response = restController.listDeliveries(serviceProviderName);

        assertThat(response.getDeliveries().size()).isEqualTo(1);
        verify(certService,times(2)).checkIfCommonNameMatchesNameInApiObject(anyString());

    }

    @Test
    public void testGettingDelivery(){
        String serviceProviderName = "my-service-provider";
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Set.of(
                        new SelectorApi("messageType = 'DENM'")
                )
        );
        AddDeliveriesResponse addDeliveriesResponse = restController.addDeliveries(serviceProviderName, request);

        String deliveryId = addDeliveriesResponse.getDeliveries().stream().findFirst().get().getId();
        GetDeliveryResponse getDeliveryResponse = restController.getDelivery(serviceProviderName, deliveryId);

        assertThat(getDeliveryResponse).isNotNull();
        // Non-existent delivery
        assertThrows(NotFoundException.class, () -> {
           restController.getDelivery(serviceProviderName, "999");
        });
    }


    @Autowired
    WebApplicationContext context;
    @Test
    public void genSwagger() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andDo((result -> {
                    Files.deleteIfExists(Paths.get("target/swagger/swagger.json"));
                    Files.createDirectories(Paths.get("target/swagger"));
                    try(FileWriter fileWriter = new FileWriter("target/swagger/swagger.json")){
                        fileWriter.write(result.getResponse().getContentAsString());
                    }

                }));
    }

}
