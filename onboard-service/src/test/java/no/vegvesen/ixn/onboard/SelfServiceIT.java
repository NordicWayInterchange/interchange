package no.vegvesen.ixn.onboard;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.properties.MessageProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
class SelfServiceIT {
	@Autowired
	ServiceProviderRepository serviceProviderRepository;

	@Autowired
	SelfService selfService;

	@Test
	void serviceProviderGetsNewSubscriptionCreatedUpdatesSelfLastSubscriptionUpdate() {
		String serviceProviderName = "SelfServiceIT-service-provider";
		ServiceProvider serviceProviderBefore = serviceProviderRepository.save(new ServiceProvider(serviceProviderName));
		assertThat(serviceProviderBefore.getSubscriptionUpdated()).isNull();

		Self selfBeforeUpdate = selfService.fetchSelf();
		assertThat(selfBeforeUpdate.getLastUpdatedLocalSubscriptions()).isNull();

		serviceProviderBefore.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, new DataType(null, MessageProperty.MESSAGE_TYPE.getName(), "DATEX2")));
		LocalDateTime subscriptionUpdatedRequested = serviceProviderBefore.getSubscriptionUpdated();
		assertThat(subscriptionUpdatedRequested).isNotNull();

		ServiceProvider serviceProviderRequested = serviceProviderRepository.save(serviceProviderBefore);
		LocalDateTime subscriptionUpdatedRequestedSaved = serviceProviderRequested.getSubscriptionUpdated();
		assertThat(subscriptionUpdatedRequestedSaved).isNotNull().isEqualTo(subscriptionUpdatedRequested);

		Set<LocalSubscription> subscriptions = serviceProviderRequested.getSubscriptions();
		LocalSubscription requestedSubscription = subscriptions.iterator().next();
		LocalSubscription createdSubscription = requestedSubscription.withStatus(LocalSubscriptionStatus.CREATED);
		subscriptions.remove(requestedSubscription);
		subscriptions.add(createdSubscription);
		serviceProviderRequested.updateSubscriptions(subscriptions);
		ServiceProvider serviceProviderCreated = serviceProviderRepository.save(serviceProviderRequested);
		assertThat(serviceProviderCreated.getSubscriptionUpdated()).isNotNull().isAfter(subscriptionUpdatedRequested);

		Self selfAfterUpdate = selfService.fetchSelf();
		assertThat(selfAfterUpdate.getLastUpdatedLocalSubscriptions()).isNotNull().isEqualTo(serviceProviderCreated.getSubscriptionUpdated());
	}
}