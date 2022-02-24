package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class OnboardRestControllerIT {


    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private NeighbourRepository neighbourRepository;

    @MockBean
    private CertService certService;

    @Autowired
    private OnboardRestController restController;

    @Test
    public void testDeletingCapability() {
        DatexCapabilityApi datexNO = new DatexCapabilityApi("NO");
        String serviceProviderName = "serviceprovider";

        //LocalCapability addedCapability = restController.addCapabilities(serviceProviderName, datexNO);
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
        DatexCapabilityApi datexNO = new DatexCapabilityApi("NO");
        String serviceProviderName = "serviceprovider";

        //LocalCapability addedCapability = restController.addCapabilities(serviceProviderName, datexNO);
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
    public void testDeletingSubscription() {
		LocalDateTime beforeDeleteTime = LocalDateTime.now();
        String serviceProviderName = "serviceprovider";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscription addSubscription = new AddSubscription(selector);

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
	void testDeletingNonExistingSubscriptionDoesNotModifyLastUpdatedSubscription() {
		String serviceProviderName = "serviceprovider-non-existing-subscription-delete";
        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(
		        serviceProviderName,
                Collections.singleton(new AddSubscription("messageType = 'DATEX2' AND originatingCountry = 'NO'"))
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
        addSubscriptions.add(new AddSubscription("countryCode = 'SE' and messageType = 'DENM'"));
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
    }


	@Test
    void testAddingLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderName() {
        String serviceProviderName = "service-provider-create-new-queue";
        String selector= "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        restController.addSubscriptions(serviceProviderName, new AddSubscriptionsRequest(serviceProviderName,Collections.singleton(new AddSubscription(selector))));

        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

        ServiceProvider savedSP = serviceProviderRepository.findByName(serviceProviderName);
        Set<LocalSubscription> localSubscriptions = savedSP.getSubscriptions();
        assertThat(localSubscriptions).hasSize(1);
        LocalSubscription subscription = localSubscriptions.stream().findFirst().get();
        /*
        TODO this no longer makes sense

        assertThat(subscription.getConsumerCommonName()).isEqualTo(serviceProviderName);

         */

        ListSubscriptionsResponse subscriptions = restController.listSubscriptions(serviceProviderName);
        Set<LocalActorSubscription> localSubscriptionApis = subscriptions.getSubscriptions();
        assertThat(localSubscriptionApis.size()).isEqualTo(1);
        verify(certService,times(3)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testAddingLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderNameAndGetApiObject() {
        String serviceProviderName = "service-provider-create-new-queue";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscriptionsResponse serviceProviderSubscriptions = restController.addSubscriptions(serviceProviderName, new AddSubscriptionsRequest(serviceProviderName,Collections.singleton(new AddSubscription(selector))));
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testFetchingAllCapabilitiesWhenServiceProviderExists() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new DenmCapability(
                        "NPRA",
                        "NO",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("6")
                ))));
        serviceProviderRepository.save(serviceProvider);
        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new DenmCapability(
                        "SPRA",
                        "SE",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("6")
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new DenmCapability(
                        "DPRA",
                        "DK",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("6")
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        FetchCapabilitiesResponse response = restController.fetchCapabilities("service-provider");
        assertThat(response.getCapabilities()).hasSize(2);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testFetchingAllCapabilitiesWhenServiceProviderDoesNotExist() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new DenmCapability(
                        "SPRA",
                        "SE",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("6")
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new DenmCapability(
                        "DPRA",
                        "DK",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("6")
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        FetchCapabilitiesResponse response = restController.fetchCapabilities("service-provider");
        assertThat(response.getCapabilities()).hasSize(2);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        verify(certService,times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }
}
