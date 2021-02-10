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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        LocalCapability addedCapability = restController.addCapabilities(serviceProviderName, datexNO);
        assertThat(addedCapability).isNotNull();
		LocalCapabilityList serviceProviderCapabilities = restController.getServiceProviderCapabilities(serviceProviderName);
        assertThat(serviceProviderCapabilities.getCapabilities()).hasSize(1);

        //Test that we don't mess up subscriptions and capabilities
        LocalSubscriptionListApi serviceProviderSubscriptions = restController.getServiceProviderSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(0);

        LocalCapability saved = serviceProviderCapabilities.getCapabilities().iterator().next();
        restController.deleteCapability(serviceProviderName, saved.getId());
    }

    @Test
    public void testDeletingSubscription() {
		LocalDateTime beforeDeleteTime = LocalDateTime.now();
        String serviceProviderName = "serviceprovider";
        restController.addSubscriptions(serviceProviderName, new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'", false));

        LocalSubscriptionListApi serviceProviderSubscriptions = restController.getServiceProviderSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

		ServiceProvider afterAddSubscription = serviceProviderRepository.findByName(serviceProviderName);
		assertThat(afterAddSubscription.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(beforeDeleteTime));

		LocalSubscriptionApi subscriptionApi = serviceProviderSubscriptions.getSubscriptions().get(0);
        restController.deleteSubscription(serviceProviderName,subscriptionApi.getId());

		ServiceProvider afterDeletedSubscription = serviceProviderRepository.findByName(serviceProviderName);
		assertThat(afterDeletedSubscription.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(beforeDeleteTime));
	}

	@Test
	void testDeletingNonExistingSubscriptionDoesNotModifyLastUpdatedSubscription() {
		String serviceProviderName = "serviceprovider-non-existing-subscription-delete";
		restController.addSubscriptions(serviceProviderName, new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'", false));

		LocalSubscriptionListApi serviceProviderSubscriptions = restController.getServiceProviderSubscriptions(serviceProviderName);
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
        restController.addSubscriptions(serviceProviderName, new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'", true));

        LocalSubscriptionListApi serviceProviderSubscriptions = restController.getServiceProviderSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

        ServiceProvider savedSP = serviceProviderRepository.findByName(serviceProviderName);
        Set<LocalSubscription> localSubscriptions = savedSP.getSubscriptions();
        assertThat(localSubscriptions).hasSize(1);
        assertThat(localSubscriptions.stream().filter(LocalSubscription::isCreateNewQueue).collect(Collectors.toSet())).hasSize(1);
    }

    @Test
    void testAddingLocalSubscriptionWithCreateNewQueueAndGetApiObject() {
        String serviceProviderName = "service-provider-create-new-queue";
        LocalSubscriptionApi serviceProviderSubscriptions = restController.addSubscriptions(serviceProviderName, new SelectorApi("messageType = 'DATEX2' AND originatingCountry = 'NO'", true));

        assertThat(serviceProviderSubscriptions.isCreateNewQueue()).isTrue();
    }
}
