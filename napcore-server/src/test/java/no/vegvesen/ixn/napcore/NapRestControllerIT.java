package no.vegvesen.ixn.napcore;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.DeliveryPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.matcher.ParseException;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.napcore.model.*;
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
        String actorCommonName = "actor";
        assertThrows(SubscriptionRequestException.class, () -> napRestController.addSubscription(actorCommonName, null));
    }

    @Test
    public void testAddingSubscriptionWithInvalidSelectorSetsStatusToIllegal(){
        String actorCommonName = "actor";
        Subscription subscription = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("1=1"));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ILLEGAL);
    }

    // Should probably be SubscriptionRequestException and not NullPointerException
    @Test
    public void testAddingSubscriptionWithNullSelectorThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NullPointerException.class, () -> napRestController.addSubscription(actorCommonName, new SubscriptionRequest()));
    }

    @Test
    public void testAddingSubscriptionWithValidSelectorReturnsValidSubscriptionAndCreatesServiceProvider(){
        String actorCommonName = "actor";
        Subscription subscription = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='NO'"));
        System.out.println(subscription);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        assertThat(napRestController.getSubscriptions(actorCommonName)).hasSize(1);
    }

    @Test
    public void testGetSubscriptionsReturnsValidSubscriptions(){
        String actorCommonName = "actor";
        SubscriptionRequest request1 = new SubscriptionRequest("originatingCountry='NO'");
        SubscriptionRequest request2 = new SubscriptionRequest("originatingCountry='SE'");
        napRestController.addSubscription(actorCommonName, request1);
        napRestController.addSubscription(actorCommonName, request2);

        List<Subscription> subscriptionList = napRestController.getSubscriptions(actorCommonName);
        assertThat(subscriptionList).hasSize(2);
        subscriptionList.forEach(a->{
            assertThat(a.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
        });
    }

    @Test
    public void testGetSubscriptionReturnsValidSubscription(){
        String actorCommonName = "actor";
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'");
        Subscription subscription = napRestController.addSubscription(actorCommonName, request);
        Subscription response = napRestController.getSubscription(actorCommonName, subscription.getId().toString());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
    }

    @Test
    public void testGetSubscriptionThrowsErrorWhenItDoesNotExist(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.getSubscription(actorCommonName, "25"));
    }

    @Test
    public void testGetSubscriptionWithInvalidIdThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.getSubscription(actorCommonName, "notAnId"));
    }

    @Test
    public void testSubscriptionIsDeletedCorrectly(){
        String actorCommonName = "actor";
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'");
        Subscription subscription = napRestController.addSubscription("actor", request);
        napRestController.deleteSubscription(actorCommonName, subscription.getId().toString());
        assertThat(napRestController.getSubscriptions(actorCommonName).stream().findFirst().get().getStatus()).isEqualTo(SubscriptionStatus.NOT_VALID);
    }

    @Test
    public void testDeleteNonExistentSubscriptionThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.deleteSubscription(actorCommonName, "1"));
    }

    // This throws NumberFormatException, should it not be NotFoundException?
    @Test
    public void testDeleteSubscriptionWithInvalidIdThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NumberFormatException.class, () -> napRestController.deleteSubscription(actorCommonName, "notAnId"));
    }

    @Test
    public void testAddingDeliveryWithValidSelectorGivesRequestedDelivery(){
        String actorCommonName = "actor";
        DeliveryRequest deliveryRequest = new DeliveryRequest("originatingCountry='NO'");
        Delivery response = napRestController.addDelivery(actorCommonName, deliveryRequest);
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
    }

    @Test
    public void testAddingDeliveryWithInvalidSelectorGivesInvalidDelivery(){
        String actorCommonName = "actor";
        DeliveryRequest deliveryRequest = new DeliveryRequest("1=1");
        Delivery response = napRestController.addDelivery(actorCommonName, deliveryRequest);
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.ILLEGAL);
    }

    @Test
    public void testAddingNullDeliveryThrowsException(){
        String actorCommonName = "actor";
        assertThrows(DeliveryPostException.class, () -> napRestController.addDelivery(actorCommonName, null));
    }

    @Test
    public void testAddingNullSelectorInDeliveryThrowsException(){
        String actorCommonName = "actor";
        assertThrows(DeliveryPostException.class, () -> napRestController.addDelivery(actorCommonName, new DeliveryRequest()));
    }
}
