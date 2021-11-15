package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.onboard.SelfService;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class OnboardRestControllerIT {


    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private SelfService selfService;

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
        restController.deleteCapability(serviceProviderName, Integer.parseInt(saved.getId()));
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
        GetCapabilityResponse response = restController.getServiceProviderCapability(serviceProviderName,Integer.parseInt(capability.getId()));
        assertThat(response.getId()).isEqualTo(capability.getId());

    }

    @Test
    public void testDeletingSubscription() {
		LocalDateTime beforeDeleteTime = LocalDateTime.now();
        String serviceProviderName = "serviceprovider";
        SelectorApi selectorApi = new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'");
        AddSubscription addSubscription = new AddSubscription(false, selectorApi);

        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));
        restController.addSubscriptions(serviceProviderName, requestApi);

        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

		ServiceProvider afterAddSubscription = serviceProviderRepository.findByName(serviceProviderName);
		assertThat(afterAddSubscription.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(beforeDeleteTime));

		LocalActorSubscription subscriptionApi = serviceProviderSubscriptions.getSubscriptions().stream().findFirst().get();
        restController.deleteSubscription(serviceProviderName,Integer.parseInt(subscriptionApi.getId()));

		ServiceProvider afterDeletedSubscription = serviceProviderRepository.findByName(serviceProviderName);
		assertThat(afterDeletedSubscription.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(beforeDeleteTime));
	}

	@Test
	void testDeletingNonExistingSubscriptionDoesNotModifyLastUpdatedSubscription() {
		String serviceProviderName = "serviceprovider-non-existing-subscription-delete";
        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(
		        serviceProviderName,
                Collections.singleton(new AddSubscription(false, new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'")))
        );
        restController.addSubscriptions(serviceProviderName, requestApi);

		ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
		assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);
		ServiceProvider savedSP = serviceProviderRepository.findByName(serviceProviderName);
		Optional<LocalDateTime> subscriptionUpdated = savedSP.getSubscriptionUpdated();
		assertThat(subscriptionUpdated).isPresent();

		try {
			restController.deleteSubscription(serviceProviderName, -1);
		} catch (NotFoundException ignore) {
		}
		ServiceProvider savedSPAfterDelete = serviceProviderRepository.findByName(serviceProviderName);

		assertThat(savedSPAfterDelete.getSubscriptionUpdated()).isEqualTo(subscriptionUpdated);
	}

	@Test
    void testAddingLocalSubscriptionWithCreateNewQueue() {
        String serviceProviderName = "service-provider-create-new-queue";
        SelectorApi selectorApi = new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'");
        restController.addSubscriptions(serviceProviderName, new AddSubscriptionsRequest(serviceProviderName,Collections.singleton(new AddSubscription(true, selectorApi))));

        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

        ServiceProvider savedSP = serviceProviderRepository.findByName(serviceProviderName);
        Set<LocalSubscription> localSubscriptions = savedSP.getSubscriptions();
        assertThat(localSubscriptions).hasSize(1);
        LocalSubscription subscription = localSubscriptions.stream().findFirst().get();
        //TODO we cannot set createNewQueue through the API.
        //assertThat(subscription.isCreateNewQueue()).isTrue();
        assertThat(subscription.getQueueConsumerUser()).isEqualTo(serviceProviderName);

        ListSubscriptionsResponse subscriptions = restController.listSubscriptions(serviceProviderName);
        Set<LocalActorSubscription> localSubscriptionApis = subscriptions.getSubscriptions();
        assertThat(localSubscriptionApis.size()).isEqualTo(1);
        //TODO same as above, cannot set createNewQueue to true through the API
        //assertThat(localSubscriptionApis.get(0).isCreateNewQueue()).isTrue();
    }

    @Test
    void testAddingLocalSubscriptionWithCreateNewQueueAndGetApiObject() {
        String serviceProviderName = "service-provider-create-new-queue";
        SelectorApi selectorApi = new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'");
        AddSubscriptionsResponse serviceProviderSubscriptions = restController.addSubscriptions(serviceProviderName, new AddSubscriptionsRequest(serviceProviderName,Collections.singleton(new AddSubscription(false, selectorApi))));

    }
}
