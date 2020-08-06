package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.LocalDataType;
import no.vegvesen.ixn.serviceprovider.model.LocalDataTypeList;
import no.vegvesen.ixn.serviceprovider.model.LocalSubscriptionApi;
import no.vegvesen.ixn.serviceprovider.model.LocalSubscriptionListApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;

import java.time.LocalDateTime;

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
        Datex2DataTypeApi datexNO = new Datex2DataTypeApi("NO");
        String serviceProviderName = "serviceprovider";

        LocalDataType localDataType = restController.addCapabilities(serviceProviderName, datexNO);
        assertThat(localDataType).isNotNull();
        LocalDataTypeList serviceProviderCapabilities = restController.getServiceProviderCapabilities(serviceProviderName);
        assertThat(serviceProviderCapabilities.getDataTypes()).hasSize(1);

        //Test that we don't mess up subscriptions and capabilities
        LocalSubscriptionListApi serviceProviderSubscriptions = restController.getServiceProviderSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscritions()).hasSize(0);

        LocalDataType saved = serviceProviderCapabilities.getDataTypes().get(0);
        restController.deleteCapability(serviceProviderName, saved.getId());

    }
    @Test
    public void testDeletingSubscription() {
		LocalDateTime beforeDeleteTime = LocalDateTime.now();
        String serviceProviderName = "serviceprovider";
        Datex2DataTypeApi datexNO = new Datex2DataTypeApi("NO");
        restController.addSubscriptions(serviceProviderName, datexNO);

        LocalSubscriptionListApi serviceProviderSubscriptions = restController.getServiceProviderSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscritions()).hasSize(1);

		ServiceProvider afterAddSubscription = serviceProviderRepository.findByName(serviceProviderName);
		assertThat(afterAddSubscription.getSubscriptionUpdated()).isAfter(beforeDeleteTime);

		LocalSubscriptionApi subscriptionApi = serviceProviderSubscriptions.getSubscritions().get(0);
        restController.deleteSubscription(serviceProviderName,subscriptionApi.getId());

		ServiceProvider afterDeletedSubscription = serviceProviderRepository.findByName(serviceProviderName);
		assertThat(afterDeletedSubscription.getSubscriptionUpdated()).isAfter(beforeDeleteTime);
	}
}
