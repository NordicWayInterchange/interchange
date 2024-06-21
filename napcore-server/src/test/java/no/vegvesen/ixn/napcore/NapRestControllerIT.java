package no.vegvesen.ixn.napcore;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.matcher.ParseException;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import no.vegvesen.ixn.napcore.model.SubscriptionStatus;
import no.vegvesen.ixn.napcore.properties.NapCoreProperties;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(initializers={PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class NapRestControllerIT {


    @Autowired
    private  ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private  NeighbourRepository neighbourRepository;

    @MockBean
    private  CertService certService;

    @MockBean
    private CertSigner certSigner;


    @Autowired
    private  NapCoreProperties napCoreProperties;

    @Autowired
    private NapRestController napRestController;

    @Test
    public void objectsAreAutowired(){
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(neighbourRepository).isNotNull();
        assertThat(napCoreProperties).isNotNull();
        assertThat(napRestController).isNotNull();
    }

    @Test
    public void testAddSubscriptionWithNullObjectThrowsException() {
        assertThrows(SubscriptionRequestException.class, () -> napRestController.addSubscription("actor", null));
    }

    @Test
    public void testAddingSubscriptionWithInvalidSelectorSetsStatusToIllegal(){
        String actorCommonName = "actor";
        Subscription subscription = napRestController.addSubscription("actor", new SubscriptionRequest("1=1"));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ILLEGAL);
    }

    @Test
    public void testAddingSubscriptionWithValidSelectorReturnsValidSubscriptionAndCreatesServiceProvider(){
        Subscription subscription = napRestController.addSubscription("actor", new SubscriptionRequest("originatingCountry='NO'"));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        assertThat(napRestController.getSubscriptions("actor")).hasSize(1);
    }

    @Test
    public void testGetSubscriptionsReturnsValidSubscriptions(){
        SubscriptionRequest request1 = new SubscriptionRequest("originatingCountry='NO'");
        SubscriptionRequest request2 = new SubscriptionRequest("originatingCountry='SE'");
        napRestController.addSubscription("actor", request1);
        napRestController.addSubscription("actor", request2);

        List<Subscription> subscriptionList = napRestController.getSubscriptions("actor");
        assertThat(subscriptionList).hasSize(2);
        subscriptionList.forEach(a->{
            assertThat(a.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
        });
    }

    @Test
    public void testGetSubscriptionReturnsValidSubscription(){
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'");
        Subscription subscription = napRestController.addSubscription("actor", request);
        Subscription response = napRestController.getSubscription("actor", subscription.getId().toString());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
    }

    @Test
    public void testGetSubscriptionThrowsErrorWhenItDoesNotExist(){
        assertThrows(NotFoundException.class, () -> napRestController.getSubscription("actor", "25"));
    }

    @Test
    public void testGetSubscriptionWithInvalidIdThrowsException(){
        assertThrows(NotFoundException.class, () -> napRestController.getSubscription("actor", "notAnId"));
    }

    @Test
    public void testSubscriptionIsDeletedCorrectly(){
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'");
        Subscription subscription = napRestController.addSubscription("actor", request);
        napRestController.deleteSubscription("actor", subscription.getId().toString());
        assertThat(napRestController.getSubscriptions("actor").stream().findFirst().get().getStatus()).isEqualTo(SubscriptionStatus.NOT_VALID);
    }

    @Test
    public void testDeleteNonExistentSubscriptionThrowsException(){
        assertThrows(NotFoundException.class, () -> napRestController.deleteSubscription("actor", "1"));
    }

    // This throws NumberFormatException, should it not be NotFoundException?
    @Test
    public void testDeleteSubscriptionWithInvalidIdThrowsException(){
        assertThrows(NumberFormatException.class, () -> napRestController.deleteSubscription("actor", "notAnId"));
    }
}
